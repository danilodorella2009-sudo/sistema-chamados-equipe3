package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    java.util.List<Categoria> findAllByOrderByIdAsc();
}
