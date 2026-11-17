package br.com.equipe3.chamados.model;

public enum AcaoHistorico {
    ABERTURA("Abertura"),
    CLASSIFICACAO("Classificação"),
    EDICAO("Edição"),
    ATRIBUICAO("Atribuição"),
    RECUSA_DESIGNACAO("Recusa de transferência"),
    ESCALONAMENTO("Escalonamento"),
    ESCALONAMENTO_AUTOMATICO("Escalonamento automático"),
    RETRANSFERENCIA("Retransferência"),
    ASSUMIDO_NIVEL_SUPERIOR("Assumido por nível superior"),
    COMENTARIO("Comentário"),
    AJUSTE_PRAZO("Ajuste de prazo (teste)"),
    SLA_VENCIDO("SLA vencido – alerta geral"),
    MUDANCA_STATUS("Mudança de status"),
    SUPORTE_REMOTO("Suporte remoto"),
    SOLUCAO("Solução"),
    VALIDACAO_APROVADA("Validação aprovada"),
    VALIDACAO_REJEITADA("Validação rejeitada"),
    ENCERRAMENTO("Encerramento");

    private final String descricao;
    AcaoHistorico(String descricao) { this.descricao = descricao; }
    public String getDescricao() { return descricao; }
}
