package br.com.equipe3.chamados.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Verifica a cada minuto se algum prazo de SLA venceu e avisa toda a equipe (N1, N2, N3 e supervisor). */
@Component
public class SlaVencimentoJob {

    private static final Logger log = LoggerFactory.getLogger(SlaVencimentoJob.class);
    private final ChamadoService chamadoService;

    public SlaVencimentoJob(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @Scheduled(fixedDelayString = "${app.sla.verificacao-vencimento-ms:60000}", initialDelay = 10000)
    public void executar() {
        int n = chamadoService.registrarVencimentosSla();
        if (n > 0) log.warn("SLA vencido em {} chamado(s): alerta enviado a todos os técnicos e ao supervisor.", n);
    }
}
