package br.com.equipe3.chamados.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ArtigoForm {
    private Long id;
    @NotBlank(message = "Informe o título") @Size(max = 150)
    private String titulo;
    @NotBlank(message = "Descreva o problema / dúvida")
    private String problema;
    @NotBlank(message = "Descreva a solução / procedimento")
    private String solucao;
    @Size(max = 255)
    private String palavrasChave;
    private Long categoriaId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getProblema() { return problema; }
    public void setProblema(String problema) { this.problema = problema; }
    public String getSolucao() { return solucao; }
    public void setSolucao(String solucao) { this.solucao = solucao; }
    public String getPalavrasChave() { return palavrasChave; }
    public void setPalavrasChave(String palavrasChave) { this.palavrasChave = palavrasChave; }
    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }
}
