package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);

    /** Técnicos ativos de todos os níveis (usado para mostrar quem atende em cada N). */
    List<Usuario> findByPerfilNomeAndAtivoTrueOrderByNomeAsc(String perfil);
}
