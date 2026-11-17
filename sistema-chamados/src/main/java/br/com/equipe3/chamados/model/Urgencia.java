package br.com.equipe3.chamados.model;

/** Urgência: quão rápido o problema precisa ser resolvido. */
public enum Urgencia {
    BAIXA("Baixa", 1), MEDIA("Média", 2), ALTA("Alta", 3), CRITICA("Crítica", 4);

    private final String descricao;
    private final int peso;
    Urgencia(String descricao, int peso) { this.descricao = descricao; this.peso = peso; }
    public String getDescricao() { return descricao; }
    public int getPeso() { return peso; }
}
