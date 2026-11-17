package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.model.*;
import br.com.equipe3.chamados.repository.ArtigoConhecimentoRepository;
import br.com.equipe3.chamados.repository.CategoriaRepository;
import br.com.equipe3.chamados.repository.GrupoRepository;
import br.com.equipe3.chamados.repository.UsuarioRepository;
import br.com.equipe3.chamados.service.ChamadoService;
import br.com.equipe3.chamados.service.EncaminhamentoService;
import br.com.equipe3.chamados.service.PrioridadeService;
import br.com.equipe3.chamados.service.SlaAlertaService;
import br.com.equipe3.chamados.service.RegraNegocioException;
import br.com.equipe3.chamados.web.form.NovoChamadoForm;
import jakarta.validation.Valid;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
@RequestMapping("/chamados")
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final CategoriaRepository categoriaRepository;
    private final GrupoRepository grupoRepository;
    private final ArtigoConhecimentoRepository artigoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrioridadeService prioridadeService;
    private final EncaminhamentoService encaminhamentoService;
    private final SlaAlertaService slaAlertaService;

    public ChamadoController(ChamadoService chamadoService, CategoriaRepository categoriaRepository,
                             GrupoRepository grupoRepository, ArtigoConhecimentoRepository artigoRepository,
                             UsuarioRepository usuarioRepository, PrioridadeService prioridadeService,
                             EncaminhamentoService encaminhamentoService, SlaAlertaService slaAlertaService) {
        this.chamadoService = chamadoService;
        this.categoriaRepository = categoriaRepository;
        this.grupoRepository = grupoRepository;
        this.artigoRepository = artigoRepository;
        this.usuarioRepository = usuarioRepository;
        this.prioridadeService = prioridadeService;
        this.encaminhamentoService = encaminhamentoService;
        this.slaAlertaService = slaAlertaService;
    }

    // ------------------------------------------------------------ listas

    @GetMapping("/meus")
    public String meus(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, Model model) {
        model.addAttribute("titulo", "Meus chamados");
        model.addAttribute("chamados", chamadoService.meusChamados(u));
        return "chamados/lista";
    }

    @GetMapping("/fila")
    public String fila(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, @RequestParam(required = false) String grupo, Model model) {
        Grupo padrao = u.getGrupo() != null ? u.getGrupo() : grupoRepository.findBySigla("N1").orElseThrow();
        // Técnico vê a fila do próprio nível e dos níveis abaixo (pode assumir deles); supervisor vê todas.
        List<Grupo> visiveis = grupoRepository.findAllByOrderByNivelAsc().stream()
                .filter(x -> u.isSupervisor() || x.getNivel() <= padrao.getNivel())
                .toList();
        Grupo g = visiveis.stream().filter(x -> x.getSigla().equals(grupo)).findFirst().orElse(padrao);
        model.addAttribute("titulo", "Fila do " + g.getSigla() + " – " + g.getNome());
        model.addAttribute("chamados", chamadoService.filaDoGrupo(g));
        model.addAttribute("grupos", visiveis);
        model.addAttribute("grupoFila", g.getSigla());
        return "chamados/lista";
    }

    @GetMapping("/todos")
    public String todos(@RequestParam(required = false) StatusChamado status,
                        @RequestParam(required = false) Prioridade prioridade,
                        @RequestParam(required = false) String grupo, Model model) {
        Stream<Chamado> s = chamadoService.todos().stream();
        if (status != null) s = s.filter(c -> c.getStatus() == status);
        if (prioridade != null) s = s.filter(c -> c.getPrioridade() == prioridade);
        if (StringUtils.hasText(grupo)) s = s.filter(c -> c.getGrupoAtual().getSigla().equals(grupo));
        model.addAttribute("titulo", "Todos os chamados");
        model.addAttribute("chamados", s.toList());
        model.addAttribute("filtros", true);
        model.addAttribute("grupos", grupoRepository.findAllByOrderByNivelAsc());
        model.addAttribute("statusList", StatusChamado.values());
        model.addAttribute("prioridades", Prioridade.values());
        return "chamados/lista";
    }

    @GetMapping("/alertas")
    public String alertas(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, Model model) {
        model.addAttribute("titulo", "Alertas de prazo (SLA)");
        model.addAttribute("chamados", slaAlertaService.alertasPara(u));
        model.addAttribute("paginaAlertas", true);
        return "chamados/lista";
    }

    // ------------------------------------------------------------ abertura

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new NovoChamadoForm());
        return formNovo(model);
    }

    @PostMapping
    public String criar(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, @Valid @ModelAttribute("form") NovoChamadoForm form,
                        BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) return formNovo(model);
        Chamado c;
        try {
            c = chamadoService.abrir(u, form.getTitulo(), form.getDescricao(), form.getTipo(),
                    form.getCategoriaId(), form.getImpacto(), form.getUrgencia());
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage()); // ex.: fora do horário – mantém o que foi digitado
            return formNovo(model);
        }
        ra.addFlashAttribute("sucesso", "Chamado #" + c.getId() + " aberto com prioridade "
                + c.getPrioridade().getDescricao() + " e encaminhado ao " + c.getGrupoAtual().getSigla() + ".");
        return "redirect:/chamados/" + c.getId();
    }

    private String formNovo(Model model) {
        model.addAttribute("tipos", TipoChamado.values());
        model.addAttribute("impactos", Impacto.values());
        model.addAttribute("urgencias", Urgencia.values());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByIdAsc());
        model.addAttribute("artigos", artigoRepository.findAllByOrderByTituloAsc());
        model.addAttribute("enc", encaminhamentoService);
        model.addAttribute("tecnicosPorNivel", tecnicosPorNivel());
        model.addAttribute("simulacao", simulacao());
        return "chamados/novo";
    }

    // ------------------------------------------------------------ edição

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                             Model model, RedirectAttributes ra) {
        Chamado c = chamadoService.buscar(id, u);
        if (!chamadoService.podeEditar(c, u)) {
            ra.addFlashAttribute("erro", "Você não pode editar este chamado.");
            return "redirect:/chamados/" + id;
        }
        model.addAttribute("c", c);
        return "chamados/editar";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                         @RequestParam String titulo, @RequestParam String descricao, RedirectAttributes ra) {
        try {
            chamadoService.editar(id, u, titulo, descricao);
            ra.addFlashAttribute("sucesso", "Chamado editado. A alteração foi registrada no histórico.");
            return "redirect:/chamados/" + id;
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chamados/" + id + "/editar";
        }
    }

    // ------------------------------------------------------------ detalhe

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, Model model) {
        Chamado c = chamadoService.buscar(id, u);
        model.addAttribute("c", c);
        model.addAttribute("grupos", grupoRepository.findAllByOrderByNivelAsc());
        model.addAttribute("tecnicosPorNivel", tecnicosPorNivel());
        model.addAttribute("grupoOrigem", chamadoService.grupoDeOrigem(c).orElse(null));
        model.addAttribute("podeEditar", chamadoService.podeEditar(c, u));
        model.addAttribute("podeAssumir", chamadoService.podeAssumir(c, u));
        model.addAttribute("motivoNaoAssumir", chamadoService.motivoNaoPodeAssumir(c, u));
        model.addAttribute("tecnicos", usuarioRepository.findByPerfilNomeAndAtivoTrueOrderByNomeAsc(Perfil.TECNICO));
        model.addAttribute("statusPermitidos", chamadoService.statusPermitidos(c, u));
        model.addAttribute("puxarDeNivelInferior", chamadoService.assumiriaDeNivelInferior(c, u));
        model.addAttribute("ultimaEdicao", chamadoService.ultimaEdicao(c).orElse(null));
        model.addAttribute("tipos", TipoChamado.values());
        model.addAttribute("impactos", Impacto.values());
        model.addAttribute("urgencias", Urgencia.values());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByIdAsc());
        model.addAttribute("artigos", artigoRepository.findAllByOrderByTituloAsc());
        boolean equipeDoGrupo = u.isSupervisor()
                || (u.isTecnico() && u.getGrupo() != null && u.getGrupo().getId().equals(c.getGrupoAtual().getId()));
        boolean responsavel = u.isSupervisor()
                || (c.getTecnicoResponsavel() != null && c.getTecnicoResponsavel().getId().equals(u.getId()));
        model.addAttribute("equipeDoGrupo", equipeDoGrupo);
        model.addAttribute("responsavel", responsavel);
        model.addAttribute("dono", c.getSolicitante().getId().equals(u.getId()));
        return "chamados/detalhe";
    }

    /**
     * Resultado do cálculo para todas as combinações, usado pela prévia ao vivo da tela de abertura.
     * Chave "TIPO|IMPACTO|URGENCIA" -> {pontos, prioridade, nivel, motivo}. A regra fica só no Java.
     */
    private Map<String, Map<String, Object>> simulacao() {
        Map<String, Map<String, Object>> m = new java.util.LinkedHashMap<>();
        for (TipoChamado t : TipoChamado.values())
            for (Impacto i : Impacto.values())
                for (Urgencia u : Urgencia.values()) {
                    Prioridade p = prioridadeService.calcular(i, u);
                    m.put(t + "|" + i + "|" + u, Map.of(
                            "pontos", encaminhamentoService.pontos(i, u),
                            "prioridade", p.getDescricao(),
                            "cor", p.getCor(),
                            "sla", prioridadeService.horasSla(p),
                            "nivel", encaminhamentoService.calcularNivel(t, i, u),
                            "motivo", encaminhamentoService.explicar(t, i, u)));
                }
        return m;
    }

    /** Ex.: {"N1" -> "Ana Souza, Bruno Costa", "N2" -> "Carla Mendes, Gustavo Nunes", ...} */
    private Map<String, String> tecnicosPorNivel() {
        return usuarioRepository.findByPerfilNomeAndAtivoTrueOrderByNomeAsc(Perfil.TECNICO).stream()
                .filter(t -> t.getGrupo() != null)
                .collect(Collectors.groupingBy(t -> t.getGrupo().getSigla(),
                        Collectors.mapping(Usuario::getNome, Collectors.joining(", "))));
    }

    // ------------------------------------------------------------ ações

    @PostMapping("/{id}/assumir")
    public String assumir(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, RedirectAttributes ra) {
        return executar(id, ra, "Chamado assumido.", () -> chamadoService.assumir(id, u));
    }

    @PostMapping("/{id}/reclassificar")
    public String reclassificar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                @RequestParam TipoChamado tipo, @RequestParam Long categoriaId,
                                @RequestParam Impacto impacto, @RequestParam Urgencia urgencia, RedirectAttributes ra) {
        return executar(id, ra, "Classificação atualizada e prioridade recalculada.",
                () -> chamadoService.reclassificar(id, u, tipo, categoriaId, impacto, urgencia));
    }

    @PostMapping("/{id}/transferir")
    public String transferir(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                             @RequestParam String grupoDestino, @RequestParam String justificativa,
                             @RequestParam(required = false) Long tecnicoDestinoId, RedirectAttributes ra) {
        return executar(id, ra, "Chamado transferido para " + grupoDestino
                        + (tecnicoDestinoId != null ? " (aguardando o aceite do técnico escolhido)." : " (fila do nível)."),
                () -> chamadoService.transferir(id, u, grupoDestino, justificativa, tecnicoDestinoId));
    }

    @PostMapping("/{id}/recusar-designacao")
    public String recusarDesignacao(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                    @RequestParam String justificativa, RedirectAttributes ra) {
        return executar(id, ra, "Transferência recusada. O chamado ficou na fila do nível.",
                () -> chamadoService.recusarDesignacao(id, u, justificativa));
    }

    @PostMapping("/{id}/retransferir")
    public String retransferir(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                               @RequestParam String justificativa, RedirectAttributes ra) {
        return executar(id, ra, "Chamado retransferido para o nível de origem.",
                () -> chamadoService.retransferir(id, u, justificativa));
    }

    @PostMapping("/{id}/status")
    public String atualizarStatus(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                  @RequestParam StatusChamado novoStatus, @RequestParam String justificativa,
                                  @RequestParam(required = false) String referenciaVersao, RedirectAttributes ra) {
        return executar(id, ra, "Status atualizado para \"" + novoStatus.getDescricao() + "\".",
                () -> chamadoService.atualizarStatus(id, u, novoStatus, justificativa, referenciaVersao));
    }

    @PostMapping("/{id}/comentar")
    public String comentar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                           @RequestParam String texto, RedirectAttributes ra) {
        return executar(id, ra, "Comentário registrado.", () -> chamadoService.comentar(id, u, texto));
    }

    @PostMapping("/{id}/remoto/solicitar")
    public String remotoSolicitar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                  @RequestParam(required = false) String ferramenta, RedirectAttributes ra) {
        return executar(id, ra, "Acesso remoto solicitado. Aguardando autorização do usuário.",
                () -> chamadoService.solicitarSuporteRemoto(id, u, ferramenta));
    }

    @PostMapping("/{id}/remoto/autorizar")
    public String remotoAutorizar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, RedirectAttributes ra) {
        return executar(id, ra, "Acesso remoto autorizado.", () -> chamadoService.autorizarSuporteRemoto(id, u));
    }

    @PostMapping("/{id}/remoto/recusar")
    public String remotoRecusar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                @RequestParam(required = false) String motivo, RedirectAttributes ra) {
        return executar(id, ra, "Acesso remoto recusado. O técnico seguirá o atendimento por outro meio.",
                () -> chamadoService.recusarSuporteRemoto(id, u, motivo));
    }

    @PostMapping("/{id}/remoto/diagnostico")
    public String remotoDiagnostico(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                                    @RequestParam String diagnostico, RedirectAttributes ra) {
        return executar(id, ra, "Diagnóstico da sessão remota registrado.",
                () -> chamadoService.registrarDiagnosticoRemoto(id, u, diagnostico));
    }

    @PostMapping("/{id}/solucionar")
    public String solucionar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                             @RequestParam String solucao, @RequestParam(required = false) String referenciaVersao,
                             @RequestParam(required = false) Long artigoId, RedirectAttributes ra) {
        return executar(id, ra, "Solução registrada. Aguardando validação do solicitante.",
                () -> chamadoService.solucionar(id, u, solucao, referenciaVersao, artigoId));
    }

    @PostMapping("/{id}/validar")
    public String validar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                          @RequestParam boolean aprovado, @RequestParam(required = false) String comentario,
                          RedirectAttributes ra) {
        return executar(id, ra, aprovado ? "Solução aprovada. Chamado encerrado." : "Solução reprovada. Chamado reaberto.",
                () -> chamadoService.validar(id, u, aprovado, comentario));
    }

    @PostMapping("/{id}/encerrar")
    public String encerrar(@PathVariable Long id, @ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                           @RequestParam(required = false) String motivo, RedirectAttributes ra) {
        return executar(id, ra, "Chamado encerrado.", () -> chamadoService.encerrarPeloSupervisor(id, u, motivo));
    }

    private String executar(Long id, RedirectAttributes ra, String msgSucesso, Runnable acao) {
        try {
            acao.run();
            ra.addFlashAttribute("sucesso", msgSucesso);
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        } catch (OptimisticLockingFailureException e) {
            // Duas pessoas agiram no mesmo chamado ao mesmo tempo: só a primeira foi gravada.
            ra.addFlashAttribute("erro", "Outra pessoa acabou de alterar este chamado (por exemplo, assumiu antes de você). "
                    + "A tela foi atualizada; confira a situação atual.");
        }
        return "redirect:/chamados/" + id;
    }
}
