package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.Impacto;
import br.com.equipe3.chamados.model.Prioridade;
import br.com.equipe3.chamados.model.Urgencia;
import br.com.equipe3.chamados.repository.SlaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Matriz Impacto x Urgência.
 * Pesos: Baixo/Baixa=1, Médio/Média=2, Alto/Alta=3, Crítico/Crítica=4.
 * Soma 2-3 = BAIXA | 4 = MEDIA | 5-6 = ALTA | 7-8 = CRITICA.
 *
 *                 Urg. Baixa  Média   Alta    Crítica
 *   Imp. Baixo    BAIXA       BAIXA   MEDIA   ALTA
 *   Imp. Médio    BAIXA       MEDIA   ALTA    ALTA
 *   Imp. Alto     MEDIA       ALTA    ALTA    CRITICA
 *   Imp. Crítico  ALTA        ALTA    CRITICA CRITICA
 */
@Service
public class PrioridadeService {

    private final SlaRepository slaRepository;

    public PrioridadeService(SlaRepository slaRepository) {
        this.slaRepository = slaRepository;
    }

    public Prioridade calcular(Impacto impacto, Urgencia urgencia) {
        int soma = impacto.getPeso() + urgencia.getPeso();
        if (soma >= 7) return Prioridade.CRITICA;
        if (soma >= 5) return Prioridade.ALTA;
        if (soma == 4) return Prioridade.MEDIA;
        return Prioridade.BAIXA;
    }

    /** Horas de SLA cadastradas na tabela sla (fallback para os valores padrão). */
    public int horasSla(Prioridade prioridade) {
        return slaRepository.findById(prioridade)
                .map(s -> s.getHorasSolucao())
                .orElse(switch (prioridade) {
                    case BAIXA -> 48;
                    case MEDIA -> 24;
                    case ALTA -> 8;
                    case CRITICA -> 2;
                });
    }

    public LocalDateTime calcularPrazo(LocalDateTime abertura, Prioridade prioridade) {
        return abertura.plusHours(horasSla(prioridade));
    }
}
