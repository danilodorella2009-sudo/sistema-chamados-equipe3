package br.com.equipe3.chamados.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Registro imutável de tudo que acontece com o chamado (quem, quando, o quê,
 * de qual nível para qual nível). Nunca é alterado nem excluído pela aplicação.
 */
@Entity
@Table(name = "historico_chamado")
public class HistoricoChamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chamado_id")
    private Chamado chamado;

    /** Nulo quando a ação foi feita pelo sistema (ex.: escalonamento automático). */
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AcaoHistorico acao;

    @ManyToOne
    @JoinColumn(name = "grupo_origem_id")
    private Grupo grupoOrigem;

    @ManyToOne
    @JoinColumn(name = "grupo_destino_id")
    private Grupo grupoDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior", length = 20)
    private StatusChamado statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", length = 20)
    private StatusChamado statusNovo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    public Long getId() { return id; }
    public Chamado getChamado() { return chamado; }
    public void setChamado(Chamado chamado) { this.chamado = chamado; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
    public AcaoHistorico getAcao() { return acao; }
    public void setAcao(AcaoHistorico acao) { this.acao = acao; }
    public Grupo getGrupoOrigem() { return grupoOrigem; }
    public void setGrupoOrigem(Grupo grupoOrigem) { this.grupoOrigem = grupoOrigem; }
    public Grupo getGrupoDestino() { return grupoDestino; }
    public void setGrupoDestino(Grupo grupoDestino) { this.grupoDestino = grupoDestino; }
    public StatusChamado getStatusAnterior() { return statusAnterior; }
    public void setStatusAnterior(StatusChamado statusAnterior) { this.statusAnterior = statusAnterior; }
    public StatusChamado getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusChamado statusNovo) { this.statusNovo = statusNovo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
