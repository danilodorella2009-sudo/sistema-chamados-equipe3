package br.com.equipe3.chamados.model;

import jakarta.persistence.*;

/** Prazo de solução (em horas) por prioridade. */
@Entity
@Table(name = "sla")
public class Sla {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Prioridade prioridade;

    @Column(name = "horas_solucao", nullable = false)
    private Integer horasSolucao;

    public Prioridade getPrioridade() { return prioridade; }
    public void setPrioridade(Prioridade prioridade) { this.prioridade = prioridade; }
    public Integer getHorasSolucao() { return horasSolucao; }
    public void setHorasSolucao(Integer horasSolucao) { this.horasSolucao = horasSolucao; }
}
