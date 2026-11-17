package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.*;
import br.com.equipe3.chamados.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Map;
import java.util.List;
import java.util.Optional;

/**
 * Regras do fluxo:
 * Abertura -> Classificação -> Impacto/Urgência -> Prioridade -> N1 -> N2 -> N3 -> Solução -> Validação -> Encerramento.
 * Toda ação gera um registro em historico_chamado.
 */
@Service
@Transactional
public class ChamadoService {

    public static final EnumSet<StatusChamado> PENDENTES =
            EnumSet.of(StatusChamado.ABERTO, StatusChamado.EM_ATENDIMENTO, StatusChamado.AGUARDANDO_USUARIO);

    /** Mudanças de status permitidas no "Atualizar status" (quem pode: técnicos do nível atual e supervisor). */
    private static final Map<StatusChamado, EnumSet<StatusChamado>> TRANSICOES = Map.of(
            StatusChamado.ABERTO, EnumSet.of(StatusChamado.EM_ATENDIMENTO, StatusChamado.CANCELADO),
            StatusChamado.EM_ATENDIMENTO, EnumSet.of(StatusChamado.ABERTO, StatusChamado.AGUARDANDO_USUARIO,
                    StatusChamado.SOLUCIONADO, StatusChamado.CANCELADO),
            StatusChamado.AGUARDANDO_USUARIO, EnumSet.of(StatusChamado.EM_ATENDIMENTO, StatusChamado.ABERTO,
                    StatusChamado.SOLUCIONADO, StatusChamado.CANCELADO),
            StatusChamado.SOLUCIONADO, EnumSet.of(StatusChamado.EM_ATENDIMENTO, StatusChamado.FECHADO),
            StatusChamado.FECHADO, EnumSet.of(StatusChamado.EM_ATENDIMENTO),   // reabrir: só supervisor
            StatusChamado.CANCELADO, EnumSet.of(StatusChamado.ABERTO));        // reativar: só supervisor

    private final ChamadoRepository chamadoRepository;
    private final HistoricoChamadoRepository historicoRepository;
    private final GrupoRepository grupoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ArtigoConhecimentoRepository artigoRepository;
    private final PrioridadeService prioridadeService;
    private final EncaminhamentoService encaminhamentoService;
    private final HorarioFuncionamentoService horarioService;
    private final UsuarioRepository usuarioRepository;

    public ChamadoService(ChamadoRepository chamadoRepository, HistoricoChamadoRepository historicoRepository,
                          GrupoRepository grupoRepository, CategoriaRepository categoriaRepository,
                          ArtigoConhecimentoRepository artigoRepository, PrioridadeService prioridadeService,
                          EncaminhamentoService encaminhamentoService, HorarioFuncionamentoService horarioService,
                          UsuarioRepository usuarioRepository) {
        this.chamadoRepository = chamadoRepository;
        this.historicoRepository = historicoRepository;
        this.grupoRepository = grupoRepository;
        this.categoriaRepository = categoriaRepository;
        this.artigoRepository = artigoRepository;
        this.prioridadeService = prioridadeService;
        this.encaminhamentoService = encaminhamentoService;
        this.horarioService = horarioService;
        this.usuarioRepository = usuarioRepository;
    }

    // ------------------------------------------------------------------ consultas

    @Transactional(readOnly = true)
    public Chamado buscar(Long id, Usuario usuario) {
        Chamado c = chamadoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Chamado #" + id + " não encontrado."));
        if (usuario.isSolicitante() && !c.getSolicitante().getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Você só pode visualizar os seus próprios chamados.");
        }
        c.getHistorico().size(); // carrega o histórico
        return c;
    }

    @Transactional(readOnly = true)
    public List<Chamado> meusChamados(Usuario solicitante) {
        return chamadoRepository.findBySolicitanteIdOrderByIdDesc(solicitante.getId());
    }

    @Transactional(readOnly = true)
    public List<Chamado> filaDoGrupo(Grupo grupo) {
        // Ordena pela prioridade (Crítica primeiro) e depois pelo mais antigo.
        // A ordenação é feita em Java porque o enum é gravado como texto no banco.
        return chamadoRepository.findByGrupoAtualIdAndStatusIn(grupo.getId(), PENDENTES).stream()
                .sorted(Comparator.comparing(Chamado::getPrioridade).reversed()
                        .thenComparing(Chamado::getDataAbertura))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Chamado> todos() {
        return chamadoRepository.findAllByOrderByIdDesc();
    }

    // ------------------------------------------------------------------ abertura e classificação

    public Chamado abrir(Usuario solicitante, String titulo, String descricao, TipoChamado tipo,
                         Long categoriaId, Impacto impacto, Urgencia urgencia) {
        horarioService.exigirAberto(); // só recebe chamados das 07:00 às 20:00
        Chamado c = new Chamado();
        c.setTitulo(titulo);
        c.setDescricao(descricao);
        c.setSolicitante(solicitante);
        c.setDataAbertura(LocalDateTime.now());
        aplicarClassificacao(c, tipo, categoriaId, impacto, urgencia);

        // Cálculo de encaminhamento: tipo + impacto + urgência definem o nível responsável.
        Grupo destino = grupo(encaminhamentoService.calcularNivel(tipo, impacto, urgencia));
        c.setGrupoAtual(destino);
        c.setStatus(StatusChamado.ABERTO);
        chamadoRepository.save(c);

        registrar(c, solicitante, AcaoHistorico.ABERTURA, null, destino, null, StatusChamado.ABERTO,
                "Chamado aberto pelo solicitante.");
        registrar(c, null, AcaoHistorico.CLASSIFICACAO, null, destino, null, null,
                String.format("Classificação automática: tipo %s, categoria %s, impacto %s, urgência %s => prioridade %s (SLA %dh). Encaminhado para %s: %s.",
                        tipo.getDescricao(), c.getCategoria().getNome(), impacto.getDescricao(), urgencia.getDescricao(),
                        c.getPrioridade().getDescricao(), prioridadeService.horasSla(c.getPrioridade()), destino.getSigla(),
                        encaminhamentoService.explicar(tipo, impacto, urgencia)));
        return c;
    }

    // ------------------------------------------------------------------ edição

    /**
     * Quem pode editar título/descrição: o solicitante dono do chamado, os técnicos do nível atual e o supervisor.
     * Chamado FECHADO não pode ser editado.
     */
    @Transactional(readOnly = true)
    public boolean podeEditar(Chamado c, Usuario u) {
        if (c.getStatus().isFinal()) return false;
        if (u.isSupervisor()) return true;
        if (u.isSolicitante()) return c.getSolicitante().getId().equals(u.getId());
        return u.isTecnico() && u.getGrupo() != null && u.getGrupo().getId().equals(c.getGrupoAtual().getId());
    }

    /** Edita título/descrição e grava no histórico quem, quando e o que mudou (com o texto anterior). */
    public void editar(Long id, Usuario usuario, String titulo, String descricao) {
        Chamado c = buscar(id, usuario);
        if (c.getStatus().isFinal()) {
            throw new RegraNegocioException("Chamado " + c.getStatus().getDescricao().toLowerCase() + " não pode ser editado.");
        }
        if (!podeEditar(c, usuario)) {
            throw new RegraNegocioException("Você não tem permissão para editar este chamado.");
        }
        if (!StringUtils.hasText(titulo) || !StringUtils.hasText(descricao)) {
            throw new RegraNegocioException("Título e descrição são obrigatórios.");
        }
        titulo = titulo.trim();
        descricao = descricao.trim();
        if (titulo.length() > 150) throw new RegraNegocioException("O título pode ter no máximo 150 caracteres.");

        boolean mudouTitulo = !titulo.equals(c.getTitulo());
        boolean mudouDescricao = !descricao.equals(c.getDescricao());
        if (!mudouTitulo && !mudouDescricao) throw new RegraNegocioException("Nenhuma alteração foi feita.");

        StringBuilder desc = new StringBuilder("Chamado editado por " + usuario.getNome()
                + " (" + usuario.getPerfil().getNome() + ").");
        if (mudouTitulo) desc.append("\nTítulo: \"").append(c.getTitulo()).append("\" → \"").append(titulo).append("\"");
        if (mudouDescricao) desc.append("\nDescrição alterada. Texto anterior:\n").append(c.getDescricao());

        c.setTitulo(titulo);
        c.setDescricao(descricao);
        registrar(c, usuario, AcaoHistorico.EDICAO, null, null, null, null, desc.toString());
    }

    /** Última edição registrada no histórico (para exibir "editado em ... por ..."). */
    @Transactional(readOnly = true)
    public Optional<HistoricoChamado> ultimaEdicao(Chamado c) {
        List<HistoricoChamado> h = c.getHistorico();
        for (int i = h.size() - 1; i >= 0; i--) {
            if (h.get(i).getAcao() == AcaoHistorico.EDICAO) return Optional.of(h.get(i));
        }
        return Optional.empty();
    }

    /** Técnico revisa a classificação informada pelo usuário; prioridade e prazo são recalculados. */
    public void reclassificar(Long id, Usuario tecnico, TipoChamado tipo, Long categoriaId, Impacto impacto, Urgencia urgencia) {
        Chamado c = buscar(id, tecnico);
        exigirEquipeDoGrupo(c, tecnico);
        exigirPendente(c);
        String antes = String.format("%s / %s / %s x %s = %s", c.getTipo().getDescricao(), c.getCategoria().getNome(),
                c.getImpacto().getDescricao(), c.getUrgencia().getDescricao(), c.getPrioridade().getDescricao());
        aplicarClassificacao(c, tipo, categoriaId, impacto, urgencia);
        String depois = String.format("%s / %s / %s x %s = %s", c.getTipo().getDescricao(), c.getCategoria().getNome(),
                c.getImpacto().getDescricao(), c.getUrgencia().getDescricao(), c.getPrioridade().getDescricao());
        registrar(c, tecnico, AcaoHistorico.CLASSIFICACAO, null, null, null, null,
                "Reclassificado: [" + antes + "] -> [" + depois + "]. Novo prazo SLA recalculado a partir da abertura.");
    }

    private void aplicarClassificacao(Chamado c, TipoChamado tipo, Long categoriaId, Impacto impacto, Urgencia urgencia) {
        c.setTipo(tipo);
        c.setCategoria(categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RegraNegocioException("Categoria inválida.")));
        c.setImpacto(impacto);
        c.setUrgencia(urgencia);
        c.setPrioridade(prioridadeService.calcular(impacto, urgencia));
        c.setPrazoSla(prioridadeService.calcularPrazo(c.getDataAbertura(), c.getPrioridade()));
        if (c.getPrazoSla().isAfter(LocalDateTime.now())) c.setSlaVencidoNotificadoEm(null);
    }

    // ------------------------------------------------------------------ atendimento

    /**
     * Quem pode assumir: técnico do mesmo nível do chamado OU de nível superior
     * (N2 assume do N1; N3 assume do N1 e do N2). Supervisor assume qualquer um.
     * Nível inferior nunca assume chamado de nível superior.
     */
    @Transactional(readOnly = true)
    public boolean podeAssumir(Chamado c, Usuario u) {
        return motivoNaoPodeAssumir(c, u) == null;
    }

    /**
     * null = pode assumir. Regras:
     *  - dois técnicos nunca ficam com o mesmo chamado: se já tem responsável, só o supervisor redistribui;
     *  - se o chamado foi transferido para um técnico específico, só ele (ou o supervisor) aceita;
     *  - técnico assume do mesmo nível ou de níveis abaixo.
     */
    @Transactional(readOnly = true)
    public String motivoNaoPodeAssumir(Chamado c, Usuario u) {
        if (!PENDENTES.contains(c.getStatus())) return "O chamado não está aberto para atendimento.";
        if (c.getTecnicoResponsavel() != null && c.getTecnicoResponsavel().getId().equals(u.getId())) {
            return "Você já é o responsável por este chamado.";
        }
        if (u.isSupervisor()) return null;
        if (!u.isTecnico() || u.getGrupo() == null) return "Somente técnicos podem assumir chamados.";
        if (c.getTecnicoResponsavel() != null) {
            return "Este chamado já foi assumido por " + c.getTecnicoResponsavel().getNome()
                    + ". Dois técnicos não podem ficar com o mesmo chamado.";
        }
        if (c.getTecnicoDesignado() != null && !c.getTecnicoDesignado().getId().equals(u.getId())) {
            return "Este chamado foi transferido para " + c.getTecnicoDesignado().getNome()
                    + ". Somente ele(a) pode aceitar.";
        }
        if (u.getGrupo().getNivel() < c.getGrupoAtual().getNivel()) {
            return "Técnicos do " + u.getGrupo().getSigla() + " não podem assumir chamados do "
                    + c.getGrupoAtual().getSigla() + ". Só é permitido assumir chamados do mesmo nível ou de níveis abaixo.";
        }
        return null;
    }

    /** true quando o técnico é de nível acima do chamado (vai "puxar" o chamado para o nível dele). */
    @Transactional(readOnly = true)
    public boolean assumiriaDeNivelInferior(Chamado c, Usuario u) {
        return u.isTecnico() && u.getGrupo() != null && u.getGrupo().getNivel() > c.getGrupoAtual().getNivel();
    }

    public void assumir(Long id, Usuario tecnico) {
        Chamado c = buscar(id, tecnico);
        exigirPendente(c);
        String motivo = motivoNaoPodeAssumir(c, tecnico);
        if (motivo != null) throw new RegraNegocioException(motivo);
        StatusChamado anterior = c.getStatus();
        boolean eraDesignado = c.getTecnicoDesignado() != null && c.getTecnicoDesignado().getId().equals(tecnico.getId());
        // Nível superior assumindo chamado de nível inferior: o chamado sobe para o nível do técnico.
        if (assumiriaDeNivelInferior(c, tecnico)) {
            Grupo origem = c.getGrupoAtual();
            Usuario responsavelAnterior = c.getTecnicoResponsavel();
            c.setGrupoAtual(tecnico.getGrupo());
            registrar(c, tecnico, AcaoHistorico.ASSUMIDO_NIVEL_SUPERIOR, origem, tecnico.getGrupo(), null, null,
                    "Chamado puxado do " + origem.getSigla() + " para o " + tecnico.getGrupo().getSigla()
                            + " por " + tecnico.getNome() + " (técnico de nível superior)."
                            + (responsavelAnterior != null ? " Estava com " + responsavelAnterior.getNome() + "." : ""));
        }
        Usuario anteriorResponsavel = c.getTecnicoResponsavel();
        c.setTecnicoResponsavel(tecnico);
        c.setTecnicoDesignado(null);
        c.setStatus(StatusChamado.EM_ATENDIMENTO);
        registrar(c, tecnico, AcaoHistorico.ATRIBUICAO, null, null, anterior, StatusChamado.EM_ATENDIMENTO,
                (eraDesignado ? "Transferência aceita: chamado assumido por " : "Chamado assumido por ")
                        + tecnico.getNome() + " (" + c.getGrupoAtual().getSigla() + ")."
                        + (anteriorResponsavel != null ? " Redistribuído pelo supervisor (estava com "
                                + anteriorResponsavel.getNome() + ")." : ""));
    }

    /**
     * Transferência manual entre níveis. Subir de nível = escalonamento; descer = devolução.
     * Justificativa é obrigatória para manter a rastreabilidade.
     */
    public void transferir(Long id, Usuario tecnico, String siglaDestino, String justificativa) {
        transferir(id, tecnico, siglaDestino, justificativa, null);
    }

    /**
     * @param tecnicoDestinoId técnico escolhido para receber o chamado (null = qualquer técnico do nível, pela fila).
     *                         Quando informado, somente esse técnico pode aceitar o chamado.
     */
    public void transferir(Long id, Usuario tecnico, String siglaDestino, String justificativa, Long tecnicoDestinoId) {
        Chamado c = buscar(id, tecnico);
        exigirEquipeDoGrupo(c, tecnico);
        exigirPendente(c);
        if (!StringUtils.hasText(justificativa)) {
            throw new RegraNegocioException("Informe a justificativa da transferência/escalonamento.");
        }
        Grupo origem = c.getGrupoAtual();
        Grupo destino = grupo(siglaDestino);
        Usuario escolhido = null;
        if (tecnicoDestinoId != null) {
            escolhido = usuarioRepository.findById(tecnicoDestinoId)
                    .orElseThrow(() -> new RegraNegocioException("Técnico de destino não encontrado."));
            if (!escolhido.isTecnico() || !escolhido.isAtivo() || escolhido.getGrupo() == null
                    || !escolhido.getGrupo().getId().equals(destino.getId())) {
                throw new RegraNegocioException(escolhido.getNome() + " não é técnico do " + destino.getSigla() + ".");
            }
            if (escolhido.getId().equals(tecnico.getId())) {
                throw new RegraNegocioException("Você não pode transferir o chamado para você mesmo.");
            }
        }
        if (origem.getId().equals(destino.getId()) && escolhido == null) {
            throw new RegraNegocioException("O chamado já está no " + destino.getSigla()
                    + ". Para passar a outro técnico do mesmo nível, escolha o técnico.");
        }
        String tipoMov = destino.getNivel() > origem.getNivel() ? "Escalonado"
                : destino.getNivel() < origem.getNivel() ? "Devolvido" : "Transferido";
        mover(c, tecnico, destino, AcaoHistorico.ESCALONAMENTO,
                tipoMov + " de " + origem.getSigla() + " para " + destino.getSigla()
                        + (escolhido != null ? ", direcionado para " + escolhido.getNome() + " (aguardando aceite)"
                                             : ", fila do nível (qualquer técnico)")
                        + ". Justificativa: " + justificativa);
        c.setTecnicoDesignado(escolhido);
    }

    /** O técnico escolhido recusa a transferência: o chamado fica na fila do nível para qualquer técnico. */
    public void recusarDesignacao(Long id, Usuario tecnico, String justificativa) {
        Chamado c = buscar(id, tecnico);
        if (c.getTecnicoDesignado() == null || !c.getTecnicoDesignado().getId().equals(tecnico.getId())) {
            throw new RegraNegocioException("Este chamado não foi transferido para você.");
        }
        if (!StringUtils.hasText(justificativa) || justificativa.trim().length() < 10) {
            throw new RegraNegocioException("Informe a justificativa da recusa (mínimo de 10 caracteres).");
        }
        c.setTecnicoDesignado(null);
        registrar(c, tecnico, AcaoHistorico.RECUSA_DESIGNACAO, null, null, null, null,
                tecnico.getNome() + " recusou a transferência. O chamado ficou na fila do "
                        + c.getGrupoAtual().getSigla() + " para qualquer técnico. Justificativa: " + justificativa.trim());
    }

    /**
     * Retransferência: devolve o chamado para o nível de onde ele veio na última transferência.
     * Exige justificativa (mínimo de 10 caracteres) para manter a rastreabilidade.
     */
    public void retransferir(Long id, Usuario tecnico, String justificativa) {
        Chamado c = buscar(id, tecnico);
        exigirEquipeDoGrupo(c, tecnico);
        exigirPendente(c);
        if (!StringUtils.hasText(justificativa) || justificativa.trim().length() < 10) {
            throw new RegraNegocioException("Informe a justificativa da retransferência (mínimo de 10 caracteres).");
        }
        Grupo origem = grupoDeOrigem(c)
                .orElseThrow(() -> new RegraNegocioException("Este chamado não foi transferido para o nível atual; não há para onde retransferir."));
        Grupo atual = c.getGrupoAtual();
        mover(c, tecnico, origem, AcaoHistorico.RETRANSFERENCIA,
                "Retransferido de " + atual.getSigla() + " de volta para " + origem.getSigla()
                        + ". Justificativa: " + justificativa.trim());
    }

    /** Nível de onde o chamado veio na última transferência que o trouxe para o nível atual. */
    @Transactional(readOnly = true)
    public Optional<Grupo> grupoDeOrigem(Chamado c) {
        List<HistoricoChamado> h = c.getHistorico();
        for (int i = h.size() - 1; i >= 0; i--) {
            HistoricoChamado e = h.get(i);
            boolean transferencia = e.getAcao() == AcaoHistorico.ESCALONAMENTO
                    || e.getAcao() == AcaoHistorico.ESCALONAMENTO_AUTOMATICO
                    || e.getAcao() == AcaoHistorico.RETRANSFERENCIA
                    || e.getAcao() == AcaoHistorico.ASSUMIDO_NIVEL_SUPERIOR;
            if (transferencia && e.getGrupoOrigem() != null && e.getGrupoDestino() != null
                    && e.getGrupoDestino().getId().equals(c.getGrupoAtual().getId())) {
                return Optional.of(e.getGrupoOrigem());
            }
        }
        return Optional.empty();
    }

    /** Usado pelo job de escalonamento automático (usuário = sistema). */
    public void escalarAutomaticamente(Chamado c, String siglaDestino, String motivo) {
        mover(c, null, grupo(siglaDestino), AcaoHistorico.ESCALONAMENTO_AUTOMATICO, motivo);
    }

    private void mover(Chamado c, Usuario ator, Grupo destino, AcaoHistorico acao, String descricao) {
        Grupo origem = c.getGrupoAtual();
        StatusChamado anterior = c.getStatus();
        c.setGrupoAtual(destino);
        c.setTecnicoResponsavel(null);           // entra na fila do novo nível
        c.setTecnicoDesignado(null);
        c.setStatus(StatusChamado.ABERTO);
        registrar(c, ator, acao, origem, destino, anterior, StatusChamado.ABERTO, descricao);
    }

    public void comentar(Long id, Usuario usuario, String texto) {
        if (!StringUtils.hasText(texto)) throw new RegraNegocioException("O comentário não pode ser vazio.");
        Chamado c = buscar(id, usuario);
        if (c.getStatus().isFinal()) {
            throw new RegraNegocioException("Chamado " + c.getStatus().getDescricao().toLowerCase() + " não aceita comentários.");
        }
        registrar(c, usuario, AcaoHistorico.COMENTARIO, null, null, null, null, texto);
        // Solicitante respondeu enquanto o técnico aguardava: volta automaticamente para atendimento.
        if (c.getStatus() == StatusChamado.AGUARDANDO_USUARIO && c.getSolicitante().getId().equals(usuario.getId())) {
            c.setStatus(StatusChamado.EM_ATENDIMENTO);
            registrar(c, null, AcaoHistorico.MUDANCA_STATUS, null, null, StatusChamado.AGUARDANDO_USUARIO,
                    StatusChamado.EM_ATENDIMENTO, "Solicitante respondeu: chamado voltou para Em Atendimento.");
        }
    }

    // ------------------------------------------------------------------ suporte remoto (simulação)

    /** 1) Técnico solicita sessão remota. */
    public void solicitarSuporteRemoto(Long id, Usuario tecnico, String ferramenta) {
        Chamado c = buscar(id, tecnico);
        exigirResponsavel(c, tecnico);
        c.setSuporteRemoto(true);
        c.setRemotoAutorizado(false);
        c.setRemotoFerramenta(StringUtils.hasText(ferramenta) ? ferramenta : "AnyDesk");
        registrar(c, tecnico, AcaoHistorico.SUPORTE_REMOTO, null, null, null, null,
                "Técnico solicitou acesso remoto via " + c.getRemotoFerramenta() + ". Aguardando autorização do solicitante.");
    }

    /** 2) Solicitante autoriza o acesso. */
    public void autorizarSuporteRemoto(Long id, Usuario solicitante) {
        Chamado c = pedidoRemotoPendente(id, solicitante);
        c.setRemotoAutorizado(true);
        c.setRemotoAutorizadoEm(LocalDateTime.now());
        registrar(c, solicitante, AcaoHistorico.SUPORTE_REMOTO, null, null, null, null,
                "Solicitante AUTORIZOU a sessão de suporte remoto (" + c.getRemotoFerramenta() + ").");
    }

    /** 2b) Solicitante recusa o acesso: a solicitação é cancelada e o técnico segue por outro meio. */
    public void recusarSuporteRemoto(Long id, Usuario solicitante, String motivo) {
        Chamado c = pedidoRemotoPendente(id, solicitante);
        c.setSuporteRemoto(false);
        c.setRemotoFerramenta(null);
        registrar(c, solicitante, AcaoHistorico.SUPORTE_REMOTO, null, null, null, null,
                "Solicitante RECUSOU a sessão de suporte remoto."
                        + (StringUtils.hasText(motivo) ? " Motivo: " + motivo : ""));
    }

    private Chamado pedidoRemotoPendente(Long id, Usuario solicitante) {
        Chamado c = buscar(id, solicitante);
        if (!c.getSolicitante().getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Somente o solicitante pode responder ao pedido de acesso remoto.");
        }
        if (!c.isSuporteRemoto() || c.isRemotoAutorizado() || c.getStatus() != StatusChamado.EM_ATENDIMENTO) {
            throw new RegraNegocioException("Não há pedido de acesso remoto aguardando sua resposta.");
        }
        return c;
    }

    /** 3) Técnico registra o diagnóstico realizado na sessão. */
    public void registrarDiagnosticoRemoto(Long id, Usuario tecnico, String diagnostico) {
        Chamado c = buscar(id, tecnico);
        exigirResponsavel(c, tecnico);
        if (!c.isRemotoAutorizado()) {
            throw new RegraNegocioException("A sessão remota só pode ser registrada após autorização do solicitante.");
        }
        if (!StringUtils.hasText(diagnostico)) throw new RegraNegocioException("Descreva o diagnóstico da sessão remota.");
        String log = "[" + LocalDateTime.now().withNano(0) + "] " + diagnostico;
        c.setRemotoDiagnostico(c.getRemotoDiagnostico() == null ? log : c.getRemotoDiagnostico() + "\n" + log);
        registrar(c, tecnico, AcaoHistorico.SUPORTE_REMOTO, null, null, null, null, "Diagnóstico da sessão remota: " + diagnostico);
    }

    // ------------------------------------------------------------------ solução, validação e encerramento

    public void solucionar(Long id, Usuario tecnico, String solucao, String referenciaVersao, Long artigoId) {
        Chamado c = buscar(id, tecnico);
        exigirResponsavel(c, tecnico);
        aplicarSolucao(c, tecnico, solucao, referenciaVersao, artigoId);
    }

    private void aplicarSolucao(Chamado c, Usuario tecnico, String solucao, String referenciaVersao, Long artigoId) {
        if (!StringUtils.hasText(solucao)) throw new RegraNegocioException("Descreva a solução aplicada.");
        StatusChamado anterior = c.getStatus();
        // Regra de encerramento do N3: correção de desenvolvimento precisa da referência de versão/release/commit/hotfix.
        if (c.getGrupoAtual().isN3() && !StringUtils.hasText(referenciaVersao)) {
            throw new RegraNegocioException("Chamados resolvidos no N3 exigem a Referência de Versão / Release / Commit / Hotfix.");
        }
        c.setSolucao(solucao);
        c.setReferenciaVersao(StringUtils.hasText(referenciaVersao) ? referenciaVersao.trim() : null);
        if (artigoId != null) {
            c.setArtigoUtilizado(artigoRepository.findById(artigoId).orElse(null));
        }
        c.setDataSolucao(LocalDateTime.now());
        c.setTecnicoDesignado(null);
        c.setStatus(StatusChamado.SOLUCIONADO);
        String desc = "Solução registrada: " + solucao
                + (c.getReferenciaVersao() != null ? " | Versão/Release: " + c.getReferenciaVersao() : "")
                + (c.getArtigoUtilizado() != null ? " | Artigo KB #" + c.getArtigoUtilizado().getId() : "")
                + ". Aguardando validação do solicitante.";
        registrar(c, tecnico, AcaoHistorico.SOLUCAO, null, null, anterior, StatusChamado.SOLUCIONADO, desc);
    }

    /** Solicitante valida: aprovado => FECHADO; reprovado => volta para EM_ATENDIMENTO no mesmo nível. */
    public void validar(Long id, Usuario solicitante, boolean aprovado, String comentario) {
        Chamado c = buscar(id, solicitante);
        if (!c.getSolicitante().getId().equals(solicitante.getId())) {
            throw new RegraNegocioException("Somente o solicitante pode validar a solução.");
        }
        if (c.getStatus() != StatusChamado.SOLUCIONADO) throw new RegraNegocioException("O chamado não está aguardando validação.");
        String obs = StringUtils.hasText(comentario) ? " Comentário: " + comentario : "";
        if (aprovado) {
            registrar(c, solicitante, AcaoHistorico.VALIDACAO_APROVADA, null, null, null, null, "Solicitante confirmou a solução." + obs);
            fechar(c, solicitante, "Chamado encerrado após validação do solicitante.");
        } else {
            if (!StringUtils.hasText(comentario)) throw new RegraNegocioException("Informe o motivo da reprovação.");
            c.setStatus(StatusChamado.EM_ATENDIMENTO);
            c.setDataSolucao(null);
            registrar(c, solicitante, AcaoHistorico.VALIDACAO_REJEITADA, null, null, StatusChamado.SOLUCIONADO,
                    StatusChamado.EM_ATENDIMENTO, "Solicitante reprovou a solução." + obs);
        }
    }

    /** Supervisor pode encerrar um chamado solucionado (ex.: solicitante não respondeu à validação). */
    public void encerrarPeloSupervisor(Long id, Usuario supervisor, String motivo) {
        if (!supervisor.isSupervisor()) throw new RegraNegocioException("Apenas o supervisor pode encerrar diretamente.");
        Chamado c = buscar(id, supervisor);
        if (c.getStatus() != StatusChamado.SOLUCIONADO) throw new RegraNegocioException("Só é possível encerrar chamados solucionados.");
        fechar(c, supervisor, "Encerrado pelo supervisor. " + (motivo == null ? "" : motivo));
    }

    private void fechar(Chamado c, Usuario ator, String descricao) {
        c.setStatus(StatusChamado.FECHADO);
        c.setDataFechamento(LocalDateTime.now());
        registrar(c, ator, AcaoHistorico.ENCERRAMENTO, null, null, StatusChamado.SOLUCIONADO, StatusChamado.FECHADO, descricao);
    }

    // ------------------------------------------------------------------ atualizar status (manual)

    /** Status para os quais o usuário pode mudar este chamado agora (usado no menu da tela). */
    @Transactional(readOnly = true)
    public List<StatusChamado> statusPermitidos(Chamado c, Usuario u) {
        boolean equipe = u.isSupervisor() || (u.isTecnico() && u.getGrupo() != null
                && u.getGrupo().getId().equals(c.getGrupoAtual().getId()));
        if (!equipe) return List.of();
        return TRANSICOES.getOrDefault(c.getStatus(), EnumSet.noneOf(StatusChamado.class)).stream()
                .filter(novo -> u.isSupervisor() || !c.getStatus().isFinal()) // reabrir fechado/cancelado: só supervisor
                .sorted()
                .toList();
    }

    /**
     * Atualização manual do status com justificativa obrigatória. Exemplos:
     * Em andamento, Aguardando usuário, Concluído (Solucionado), Fechado, Cancelado, Reabrir.
     */
    public void atualizarStatus(Long id, Usuario ator, StatusChamado novo, String justificativa, String referenciaVersao) {
        Chamado c = buscar(id, ator);
        StatusChamado atual = c.getStatus();
        if (!statusPermitidos(c, ator).contains(novo)) {
            throw new RegraNegocioException("Não é permitido mudar de \"" + atual.getDescricao() + "\" para \""
                    + novo.getDescricao() + "\"" + (ator.isSupervisor() ? "." : " (ou você não tem permissão para isso)."));
        }
        if (!StringUtils.hasText(justificativa)) {
            throw new RegraNegocioException(novo == StatusChamado.SOLUCIONADO
                    ? "Para concluir, descreva a solução aplicada."
                    : "Informe a justificativa da mudança de status.");
        }
        justificativa = justificativa.trim();

        switch (novo) {
            case SOLUCIONADO -> {           // concluir = registrar solução (mesmas regras, inclusive release no N3)
                if (c.getTecnicoResponsavel() == null) c.setTecnicoResponsavel(ator);
                aplicarSolucao(c, ator, justificativa, referenciaVersao, null);
                return;
            }
            case EM_ATENDIMENTO -> {        // em andamento / reabrir
                if (atual == StatusChamado.ABERTO) {   // pegar da fila = assumir: mesmas regras
                    String motivo = motivoNaoPodeAssumir(c, ator);
                    if (motivo != null) throw new RegraNegocioException(motivo);
                }
                if (c.getTecnicoResponsavel() == null || atual == StatusChamado.ABERTO) c.setTecnicoResponsavel(ator);
                c.setTecnicoDesignado(null);
                if (atual == StatusChamado.SOLUCIONADO || atual.isFinal()) {
                    c.setDataSolucao(null);
                    c.setDataFechamento(null);
                }
            }
            case ABERTO -> {                // volta para a fila do nível, sem responsável
                c.setTecnicoResponsavel(null);
                c.setDataFechamento(null);
            }
            case FECHADO, CANCELADO -> {
                c.setDataFechamento(LocalDateTime.now());
                c.setTecnicoDesignado(null);
            }
            default -> { }
        }
        c.setStatus(novo);
        String verbo = switch (novo) {
            case EM_ATENDIMENTO -> atual == StatusChamado.SOLUCIONADO || atual.isFinal() ? "Chamado reaberto" : "Chamado em andamento";
            case AGUARDANDO_USUARIO -> "Aguardando retorno do usuário";
            case FECHADO -> "Chamado fechado" + (atual == StatusChamado.SOLUCIONADO ? " sem validação do solicitante" : "");
            case CANCELADO -> "Chamado cancelado";
            case ABERTO -> atual == StatusChamado.CANCELADO ? "Chamado reativado" : "Chamado devolvido para a fila do " + c.getGrupoAtual().getSigla();
            default -> "Status atualizado";
        };
        registrar(c, ator, novo == StatusChamado.FECHADO ? AcaoHistorico.ENCERRAMENTO : AcaoHistorico.MUDANCA_STATUS,
                null, null, atual, novo, verbo + " por " + ator.getNome() + ". Justificativa: " + justificativa);
    }

    // ------------------------------------------------------------------ SLA vencido: alerta para toda a equipe

    /**
     * Procura chamados pendentes cujo prazo acabou de vencer e ainda não foram avisados, e registra no
     * histórico o alerta enviado a TODOS os técnicos (N1, N2, N3) e ao supervisor. Roda a cada minuto
     * (SlaVencimentoJob) e também logo após um ajuste de prazo de teste.
     * @return quantos chamados geraram alerta agora
     */
    public int registrarVencimentosSla() {
        LocalDateTime agora = LocalDateTime.now();
        List<Chamado> vencidos = chamadoRepository.findAll().stream()
                .filter(c -> PENDENTES.contains(c.getStatus()))
                .filter(c -> !agora.isBefore(c.getPrazoSla()))
                .filter(c -> c.getSlaVencidoNotificadoEm() == null)
                .toList();
        if (vencidos.isEmpty()) return 0;
        String equipe = equipeParaAlerta();
        for (Chamado c : vencidos) {
            c.setSlaVencidoNotificadoEm(agora);
            registrar(c, null, AcaoHistorico.SLA_VENCIDO, null, null, null, null,
                    "Prazo do SLA (" + c.getPrioridade().getDescricao() + ") vencido em " + c.getPrazoSla().withNano(0)
                            + " com o chamado no " + c.getGrupoAtual().getSigla()
                            + (c.getTecnicoResponsavel() != null ? " (responsável: " + c.getTecnicoResponsavel().getNome() + ")" : " (sem responsável)")
                            + ". Alerta enviado a toda a equipe: " + equipe + ".");
        }
        return vencidos.size();
    }

    /** "Ana Souza (N1), Bruno Costa (N1), ..., Fernanda Lima (supervisor)" */
    private String equipeParaAlerta() {
        List<String> nomes = new java.util.ArrayList<>();
        usuarioRepository.findByPerfilNomeAndAtivoTrueOrderByNomeAsc(Perfil.TECNICO).stream()
                .sorted(Comparator.comparing((Usuario u) -> u.getGrupo() == null ? 9 : u.getGrupo().getNivel())
                        .thenComparing(Usuario::getNome))
                .forEach(u -> nomes.add(u.getNome() + " (" + (u.getGrupo() != null ? u.getGrupo().getSigla() : "-") + ")"));
        usuarioRepository.findByPerfilNomeAndAtivoTrueOrderByNomeAsc(Perfil.ADMINISTRADOR_SUPERVISOR)
                .forEach(u -> nomes.add(u.getNome() + " (supervisor)"));
        return String.join(", ", nomes);
    }

    // ------------------------------------------------------------------ teste de prazos (apresentação)

    /** Supervisor ajusta o prazo para "agora + minutos" (negativo = já vencido) para demonstrar o alerta. */
    public void ajustarPrazoParaTeste(Long id, Usuario supervisor, int minutos) {
        if (!supervisor.isSupervisor()) throw new RegraNegocioException("Apenas o supervisor pode ajustar prazos de teste.");
        Chamado c = buscar(id, supervisor);
        if (!PENDENTES.contains(c.getStatus())) throw new RegraNegocioException("Só chamados pendentes têm prazo em andamento.");
        LocalDateTime anterior = c.getPrazoSla();
        // Em teste o prazo pode ficar antes da abertura (ex.: "vencido há 10 min" num chamado recém-aberto).
        LocalDateTime novo = LocalDateTime.now().plusMinutes(minutos).withSecond(0).withNano(0);
        c.setPrazoSla(novo);
        c.setSlaVencidoNotificadoEm(null);   // novo prazo: pode gerar novo alerta de vencimento
        registrar(c, supervisor, AcaoHistorico.AJUSTE_PRAZO, null, null, null, null,
                "Prazo ajustado para TESTE de alerta pelo supervisor: de " + anterior.withNano(0) + " para " + novo
                        + (minutos >= 0 ? " (vence em " + minutos + " min)." : " (vencido há " + (-minutos) + " min)."));
        registrarVencimentosSla();           // se já ficou vencido, avisa a equipe na hora (sem esperar o minuto)
    }

    /** Volta o prazo ao valor calculado pelo SLA (abertura + horas da prioridade). */
    public void restaurarPrazo(Long id, Usuario supervisor) {
        if (!supervisor.isSupervisor()) throw new RegraNegocioException("Apenas o supervisor pode restaurar prazos.");
        Chamado c = buscar(id, supervisor);
        LocalDateTime anterior = c.getPrazoSla();
        c.setPrazoSla(prioridadeService.calcularPrazo(c.getDataAbertura(), c.getPrioridade()));
        c.setSlaVencidoNotificadoEm(null);
        registrar(c, supervisor, AcaoHistorico.AJUSTE_PRAZO, null, null, null, null,
                "Prazo restaurado pelo SLA original: de " + anterior.withNano(0) + " para " + c.getPrazoSla().withNano(0) + ".");
    }

    // ------------------------------------------------------------------ apoio

    private void exigirEquipeDoGrupo(Chamado c, Usuario u) {
        if (u.isSupervisor()) return;
        if (!u.isTecnico() || u.getGrupo() == null || !u.getGrupo().getId().equals(c.getGrupoAtual().getId())) {
            throw new RegraNegocioException("Somente técnicos do " + c.getGrupoAtual().getSigla() + " (ou o supervisor) podem executar esta ação.");
        }
    }

    private void exigirResponsavel(Chamado c, Usuario u) {
        if (c.getStatus() != StatusChamado.EM_ATENDIMENTO) {
            throw new RegraNegocioException("O chamado precisa estar Em Atendimento (assuma o chamado primeiro).");
        }
        if (u.isSupervisor()) return;
        if (c.getTecnicoResponsavel() == null || !c.getTecnicoResponsavel().getId().equals(u.getId())) {
            throw new RegraNegocioException("Somente o técnico responsável pode executar esta ação.");
        }
    }

    private void exigirPendente(Chamado c) {
        if (!PENDENTES.contains(c.getStatus())) {
            throw new RegraNegocioException("Ação permitida apenas para chamados Abertos ou Em Atendimento.");
        }
    }

    private Grupo grupo(String sigla) {
        return grupoRepository.findBySigla(sigla)
                .orElseThrow(() -> new RegraNegocioException("Grupo " + sigla + " não encontrado."));
    }

    private void registrar(Chamado c, Usuario usuario, AcaoHistorico acao, Grupo origem, Grupo destino,
                           StatusChamado statusAnterior, StatusChamado statusNovo, String descricao) {
        HistoricoChamado h = new HistoricoChamado();
        h.setChamado(c);
        h.setUsuario(usuario);
        h.setDataHora(LocalDateTime.now());
        h.setAcao(acao);
        h.setGrupoOrigem(origem);
        h.setGrupoDestino(destino);
        h.setStatusAnterior(statusAnterior);
        h.setStatusNovo(statusNovo);
        h.setDescricao(descricao);
        historicoRepository.save(h);
        c.getHistorico().add(h);
    }
}
