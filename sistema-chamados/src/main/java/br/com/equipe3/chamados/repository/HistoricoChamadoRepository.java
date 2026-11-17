package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.AcaoHistorico;
import br.com.equipe3.chamados.model.HistoricoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface HistoricoChamadoRepository extends JpaRepository<HistoricoChamado, Long> {
    long countByAcaoIn(Collection<AcaoHistorico> acoes);
}
