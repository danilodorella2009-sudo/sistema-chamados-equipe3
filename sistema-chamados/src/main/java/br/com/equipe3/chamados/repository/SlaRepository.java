package br.com.equipe3.chamados.repository;

import br.com.equipe3.chamados.model.Sla;
import br.com.equipe3.chamados.model.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaRepository extends JpaRepository<Sla, Prioridade> {
}
