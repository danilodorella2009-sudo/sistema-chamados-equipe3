package br.com.equipe3.chamados.model;

import jakarta.persistence.*;

/** Perfil de acesso: SOLICITANTE, TECNICO ou ADMINISTRADOR_SUPERVISOR. */
@Entity
@Table(name = "perfil")
public class Perfil {
    public static final String SOLICITANTE = "SOLICITANTE";
    public static final String TECNICO = "TECNICO";
    public static final String ADMINISTRADOR_SUPERVISOR = "ADMINISTRADOR_SUPERVISOR";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nome;

    @Column(length = 200)
    private String descricao;

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}
