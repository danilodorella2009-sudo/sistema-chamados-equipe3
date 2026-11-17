package br.com.equipe3.chamados.model;

public enum StatusChamado {
    ABERTO("Aberto", "primary", "Devolver para a fila (Aberto)"),
    EM_ATENDIMENTO("Em Atendimento", "warning", "Em andamento (Em Atendimento)"),
    AGUARDANDO_USUARIO("Aguardando Usuário", "info", "Aguardando retorno do usuário"),
    SOLUCIONADO("Solucionado", "success", "Concluído (Solucionado)"),
    FECHADO("Fechado", "dark", "Fechar chamado"),
    CANCELADO("Cancelado", "secondary", "Cancelar chamado");

    private final String descricao;
    private final String cor;
    /** Texto usado no menu "Atualizar status". */
    private final String rotuloAcao;

    StatusChamado(String descricao, String cor, String rotuloAcao) {
        this.descricao = descricao; this.cor = cor; this.rotuloAcao = rotuloAcao;
    }
    public String getDescricao() { return descricao; }
    public String getCor() { return cor; }
    public String getRotuloAcao() { return rotuloAcao; }

    /** Encerrado = não está mais em atendimento (solucionado, fechado ou cancelado). */
    public boolean isFinal() { return this == FECHADO || this == CANCELADO; }
}
