package br.com.equipe3.chamados.model;

public enum Prioridade {
    BAIXA("Baixa", "secondary"),
    MEDIA("Média", "info"),
    ALTA("Alta", "warning"),
    CRITICA("Crítica", "danger");

    private final String descricao;
    private final String cor;
    Prioridade(String descricao, String cor) { this.descricao = descricao; this.cor = cor; }
    public String getDescricao() { return descricao; }
    /** Classe de cor do Bootstrap usada nas telas. */
    public String getCor() { return cor; }
}
