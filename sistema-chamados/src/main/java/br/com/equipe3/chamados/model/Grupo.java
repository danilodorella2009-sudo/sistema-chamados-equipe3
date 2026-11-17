package br.com.equipe3.chamados.model;

import jakarta.persistence.*;

/** Grupo / nível de suporte: N1, N2, N3. */
@Entity
@Table(name = "grupo")
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 5)
    private String sigla;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(length = 255)
    private String descricao;

    /** 1, 2 ou 3 – usado para saber se é escalonamento (subida) ou devolução. */
    @Column(nullable = false)
    private Integer nivel;

    public boolean isN3() { return nivel != null && nivel == 3; }

    public Long getId() { return id; }
    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Integer getNivel() { return nivel; }
    public void setNivel(Integer nivel) { this.nivel = nivel; }
}
