package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.Chamado;
import br.com.equipe3.chamados.model.StatusChamado;
import br.com.equipe3.chamados.repository.ChamadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Regra AUTOMÁTICA de escalonamento: chamado parado no N1 com SLA vencido
 * (ainda Aberto ou Em Atendimento) é escalado para o N2.
 */
@Component
public class EscalonamentoAutomaticoJob {

    private static final Logger log = LoggerFactory.getLogger(EscalonamentoAutomaticoJob.class);

    private final ChamadoRepository chamadoRepository;
    private final ChamadoService chamadoService;

    @Value("${app.escalonamento-automatico.habilitado:true}")
    private boolean habilitado;

    public EscalonamentoAutomaticoJob(ChamadoRepository chamadoRepository, ChamadoService chamadoService) {
        this.chamadoRepository = chamadoRepository;
        this.chamadoService = chamadoService;
    }

    @Scheduled(fixedDelayString = "${app.escalonamento-automatico.intervalo-ms:300000}", initialDelay = 60000)
    @Transactional
    public void executar() {
        if (!habilitado) return;
        List<Chamado> vencidos = chamadoRepository.findByGrupoAtualSiglaAndStatusInAndPrazoSlaBefore(
                "N1", java.util.EnumSet.of(StatusChamado.ABERTO, StatusChamado.EM_ATENDIMENTO), LocalDateTime.now());
        for (Chamado c : vencidos) {
            chamadoService.escalarAutomaticamente(c, "N2",
                    "Escalonamento automático N1 -> N2: SLA de " + c.getPrioridade().getDescricao() + " vencido sem solução no N1.");
            log.info("Chamado #{} escalado automaticamente para N2 (SLA vencido).", c.getId());
        }
    }
}
