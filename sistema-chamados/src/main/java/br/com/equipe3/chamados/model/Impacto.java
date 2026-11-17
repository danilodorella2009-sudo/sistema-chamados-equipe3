package br.com.equipe3.chamados.model;

/** Impacto: quantos clientes/usuários/processos são afetados. */
public enum Impacto {
    BAIXO("Baixo", 1), MEDIO("Médio", 2), ALTO("Alto", 3), CRITICO("Crítico", 4);

    private final String descricao;
    private final int peso;
    Impacto(String descricao, int peso) { this.descricao = descricao; this.peso = peso; }
    public String getDescricao() { return descricao; }
    public int getPeso() { return peso; }
}
