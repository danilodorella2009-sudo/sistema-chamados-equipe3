package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.Impacto;
import br.com.equipe3.chamados.model.Prioridade;
import br.com.equipe3.chamados.model.Urgencia;
import br.com.equipe3.chamados.repository.SlaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Teste unitário da matriz Impacto x Urgência e do cálculo de prazo (não precisa de banco). */
class PrioridadeServiceTest {

    private final SlaRepository slaRepository = Mockito.mock(SlaRepository.class);
    private final PrioridadeService service = new PrioridadeService(slaRepository);

    @Test
    void matrizDePrioridade() {
        assertEquals(Prioridade.BAIXA, service.calcular(Impacto.BAIXO, Urgencia.BAIXA));
        assertEquals(Prioridade.BAIXA, service.calcular(Impacto.BAIXO, Urgencia.MEDIA));
        assertEquals(Prioridade.MEDIA, service.calcular(Impacto.MEDIO, Urgencia.MEDIA));
        assertEquals(Prioridade.ALTA, service.calcular(Impacto.MEDIO, Urgencia.ALTA));   // chamado 5 (médio-alta)
        assertEquals(Prioridade.ALTA, service.calcular(Impacto.ALTO, Urgencia.ALTA));    // chamado 1
        assertEquals(Prioridade.CRITICA, service.calcular(Impacto.ALTO, Urgencia.CRITICA));
        assertEquals(Prioridade.CRITICA, service.calcular(Impacto.CRITICO, Urgencia.CRITICA)); // chamado 6
    }

    @Test
    void prazoUsaHorasDoSla() {
        Mockito.when(slaRepository.findById(Mockito.any())).thenReturn(Optional.empty()); // usa valores padrão
        LocalDateTime abertura = LocalDateTime.of(2026, 1, 1, 8, 0);
        assertEquals(abertura.plusHours(2), service.calcularPrazo(abertura, Prioridade.CRITICA));
        assertEquals(abertura.plusHours(8), service.calcularPrazo(abertura, Prioridade.ALTA));
        assertEquals(abertura.plusHours(24), service.calcularPrazo(abertura, Prioridade.MEDIA));
        assertEquals(abertura.plusHours(48), service.calcularPrazo(abertura, Prioridade.BAIXA));
    }
}
