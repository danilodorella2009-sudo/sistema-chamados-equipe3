package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Chamado;
import br.com.equipe3.chamados.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    List<Chamado> findBySolicitanteIdOrderByIdDesc(Long solicitanteId);

    List<Chamado> findByGrupoAtualIdAndStatusIn(Long grupoId, Collection<StatusChamado> status);

    List<Chamado> findAllByOrderByIdDesc();

    /** Chamados no nível informado, ainda não solucionados e com SLA vencido. */
    List<Chamado> findByGrupoAtualSiglaAndStatusInAndPrazoSlaBefore(
            String sigla, Collection<StatusChamado> status, LocalDateTime agora);

    // ----- Consultas do dashboard -----
    @Query("select c.status, count(c) from Chamado c group by c.status")
    List<Object[]> contarPorStatus();

    @Query("select c.prioridade, count(c) from Chamado c group by c.prioridade")
    List<Object[]> contarPorPrioridade();

    @Query("select c.tipo, count(c) from Chamado c group by c.tipo")
    List<Object[]> contarPorTipo();

    @Query("select c.categoria.nome, count(c) from Chamado c group by c.categoria.nome order by count(c) desc")
    List<Object[]> contarPorCategoria();

    @Query("select c.grupoAtual.sigla, count(c) from Chamado c group by c.grupoAtual.sigla order by c.grupoAtual.sigla")
    List<Object[]> contarPorNivel();
}
