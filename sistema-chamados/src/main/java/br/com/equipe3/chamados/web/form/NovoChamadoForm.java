package br.com.equipe3.chamados.web.form;

import br.com.equipe3.chamados.model.Impacto;
import br.com.equipe3.chamados.model.TipoChamado;
import br.com.equipe3.chamados.model.Urgencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NovoChamadoForm {
    @NotBlank(message = "Informe o título") @Size(max = 150)
    private String titulo;
    @NotBlank(message = "Descreva o problema ou solicitação")
    private String descricao;
    @NotNull(message = "Selecione o tipo")
    private TipoChamado tipo;
    @NotNull(message = "Selecione a categoria")
    private Long categoriaId;
    @NotNull(message = "Selecione o impacto")
    private Impacto impacto;
    @NotNull(message = "Selecione a urgência")
    private Urgencia urgencia;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public TipoChamado getTipo() { return tipo; }
    public void setTipo(TipoChamado tipo) { this.tipo = tipo; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
    public Impacto getImpacto() { return impacto; }
    public void setImpacto(Impacto impacto) { this.impacto = impacto; }
    public Urgencia getUrgencia() { return urgencia; }
    public void setUrgencia(Urgencia urgencia) { this.urgencia = urgencia; }
}
