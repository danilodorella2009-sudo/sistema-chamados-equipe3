package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    java.util.Optional<Perfil> findByNome(String nome);
}
