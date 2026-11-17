package br.com.equipe3.chamados.model;

public enum TipoChamado {
    INCIDENTE("Incidente"),
    BUG("Bug"),
    SOLICITACAO_DE_MELHORIA("Solicitação de Melhoria"),
    SOLICITACAO_DE_SERVICO("Solicitação de Serviço"),
    DUVIDA("Dúvida");

    private final String descricao;
    TipoChamado(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
