package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.ArtigoConhecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ArtigoConhecimentoRepository extends JpaRepository<ArtigoConhecimento, Long> {
    List<ArtigoConhecimento> findAllByOrderByTituloAsc();

    @Query("""
           select a from ArtigoConhecimento a
           where lower(a.titulo) like lower(concat('%', :termo, '%'))
              or lower(a.palavrasChave) like lower(concat('%', :termo, '%'))
              or lower(a.problema) like lower(concat('%', :termo, '%'))
           order by a.titulo
           """)
    List<ArtigoConhecimento> buscar(@Param("termo") String termo);
}
