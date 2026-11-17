package br.com.equipe3.chamados.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamado")
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    // ---------- Classificação ----------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoChamado tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Impacto impacto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Urgencia urgencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    // ---------- Atendimento ----------
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusChamado status = StatusChamado.ABERTO;

    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitante_id")
    private Usuario solicitante;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_atual_id")
    private Grupo grupoAtual;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnicoResponsavel;

    /** Técnico escolhido na transferência: só ele pode aceitar (assumir) o chamado. Nulo = qualquer um do nível. */
    @ManyToOne
    @JoinColumn(name = "tecnico_designado_id")
    private Usuario tecnicoDesignado;

    /**
     * Controle de concorrência (optimistic locking): se duas pessoas tentarem assumir/alterar o mesmo
     * chamado ao mesmo tempo, só a primeira grava; a segunda recebe erro em vez de sobrescrever.
     */
    @Version
    @Column(nullable = false)
    private Long versao = 0L;

    @Column(name = "data_abertura", nullable = false)
    private LocalDateTime dataAbertura = LocalDateTime.now();

    @Column(name = "prazo_sla", nullable = false)
    private LocalDateTime prazoSla;

    @Column(name = "data_solucao")
    private LocalDateTime dataSolucao;

    /** Quando o alerta de SLA vencido foi enviado a toda a equipe (evita avisar duas vezes). */
    @Column(name = "sla_vencido_notificado_em")
    private LocalDateTime slaVencidoNotificadoEm;

    @Column(name = "data_fechamento")
    private LocalDateTime dataFechamento;

    // ---------- Solução ----------
    @Column(columnDefinition = "TEXT")
    private String solucao;

    /** Release / versão / commit / hotfix da correção (obrigatório quando resolvido no N3). */
    @Column(name = "referencia_versao", length = 150)
    private String referenciaVersao;

    @ManyToOne
    @JoinColumn(name = "artigo_id")
    private ArtigoConhecimento artigoUtilizado;

    // ---------- Suporte remoto (simulação) ----------
    @Column(name = "suporte_remoto", nullable = false)
    private boolean suporteRemoto = false;

    @Column(name = "remoto_autorizado", nullable = false)
    private boolean remotoAutorizado = false;

    @Column(name = "remoto_autorizado_em")
    private LocalDateTime remotoAutorizadoEm;

    @Column(name = "remoto_ferramenta", length = 60)
    private String remotoFerramenta;

    @Column(name = "remoto_diagnostico", columnDefinition = "TEXT")
    private String remotoDiagnostico;

    @OneToMany(mappedBy = "chamado")
    @OrderBy("dataHora ASC, id ASC")
    private List<HistoricoChamado> historico = new ArrayList<>();

    /** SLA vencido: ainda não solucionado e prazo já passou, ou solucionado depois do prazo. */
    public boolean isSlaVencido() {
        LocalDateTime referencia = dataSolucao != null ? dataSolucao
                : (dataFechamento != null ? dataFechamento : LocalDateTime.now());
        return prazoSla != null && referencia.isAfter(prazoSla);
    }

    public boolean isEncerrado() {
        return status == StatusChamado.SOLUCIONADO || status.isFinal();
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public TipoChamado getTipo() { return tipo; }
    public void setTipo(TipoChamado tipo) { this.tipo = tipo; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public Impacto getImpacto() { return impacto; }
    public void setImpacto(Impacto impacto) { this.impacto = impacto; }
    public Urgencia getUrgencia() { return urgencia; }
    public void setUrgencia(Urgencia urgencia) { this.urgencia = urgencia; }
    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public StatusChamado getStatus() { return status; }
    public void setStatus(StatusChamado status) { this.status = status; }
    public Usuario getSolicitante() { return solicitante; }
    public void setSolicitante(Usuario solicitante) { this.solicitante = solicitante; }
    public Grupo getGrupoAtual() { return grupoAtual; }
    public void setGrupoAtual(Grupo grupoAtual) { this.grupoAtual = grupoAtual; }
    public Usuario getTecnicoDesignado() { return tecnicoDesignado; }
    public void setTecnicoDesignado(Usuario tecnicoDesignado) { this.tecnicoDesignado = tecnicoDesignado; }
    public Long getVersao() { return versao; }
    public Usuario getTecnicoResponsavel() { return tecnicoResponsavel; }
    public void setTecnicoResponsavel(Usuario tecnicoResponsavel) { this.tecnicoResponsavel = tecnicoResponsavel; }
    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public LocalDateTime getPrazoSla() { return prazoSla; }
    public void setPrazoSla(LocalDateTime prazoSla) { this.prazoSla = prazoSla; }
    public LocalDateTime getSlaVencidoNotificadoEm() { return slaVencidoNotificadoEm; }
    public void setSlaVencidoNotificadoEm(LocalDateTime v) { this.slaVencidoNotificadoEm = v; }
    public LocalDateTime getDataSolucao() { return dataSolucao; }
    public void setDataSolucao(LocalDateTime dataSolucao) { this.dataSolucao = dataSolucao; }
    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }
    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }
    public String getReferenciaVersao() { return referenciaVersao; }
    public void setReferenciaVersao(String referenciaVersao) { this.referenciaVersao = referenciaVersao; }
    public ArtigoConhecimento getArtigoUtilizado() { return artigoUtilizado; }
    public void setArtigoUtilizado(ArtigoConhecimento artigoUtilizado) { this.artigoUtilizado = artigoUtilizado; }
    public boolean isSuporteRemoto() { return suporteRemoto; }
    public void setSuporteRemoto(boolean suporteRemoto) { this.suporteRemoto = suporteRemoto; }
    public boolean isRemotoAutorizado() { return remotoAutorizado; }
    public void setRemotoAutorizado(boolean remotoAutorizado) { this.remotoAutorizado = remotoAutorizado; }
    public LocalDateTime getRemotoAutorizadoEm() { return remotoAutorizadoEm; }
    public void setRemotoAutorizadoEm(LocalDateTime remotoAutorizadoEm) { this.remotoAutorizadoEm = remotoAutorizadoEm; }
    public String getRemotoFerramenta() { return remotoFerramenta; }
    public void setRemotoFerramenta(String remotoFerramenta) { this.remotoFerramenta = remotoFerramenta; }
    public String getRemotoDiagnostico() { return remotoDiagnostico; }
    public void setRemotoDiagnostico(String remotoDiagnostico) { this.remotoDiagnostico = remotoDiagnostico; }
    public List<HistoricoChamado> getHistorico() { return historico; }
}
