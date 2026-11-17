package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    java.util.Optional<Grupo> findBySigla(String sigla);
    java.util.List<Grupo> findAllByOrderByNivelAsc();
}
