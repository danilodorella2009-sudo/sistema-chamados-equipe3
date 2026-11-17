package br.com.equipe3.chamados.service;

/** Violação de regra do fluxo de atendimento (mensagem exibida ao usuário). */
public class RegraNegocioException extends RuntimeException {
    public RegraNegocioException(String mensagem) { super(mensagem); }
}
