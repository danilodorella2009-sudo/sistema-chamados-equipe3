package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.Chamado;
import br.com.equipe3.chamados.model.Usuario;
import br.com.equipe3.chamados.repository.ChamadoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Alerta de prazo (SLA) próximo de acabar.
 *  - PROXIMO: chamado pendente que já consumiu app.sla.alerta-percentual % do SLA da sua prioridade
 *             (padrão 75%: Crítica 2h -> alerta faltando 30min; Alta 8h -> 2h; Média 24h -> 6h; Baixa 48h -> 12h).
 *  - VENCIDO: chamado pendente com o prazo já estourado.
 *  - OK:      dentro do prazo, longe de vencer.
 * Chamados solucionados, fechados ou cancelados não geram alerta.
 */
@Service
@Transactional(readOnly = true)
public class SlaAlertaService {

    public enum Nivel { OK, PROXIMO, VENCIDO }

    private final ChamadoRepository chamadoRepository;
    private final PrioridadeService prioridadeService;
    private final int percentualAlerta;

    public SlaAlertaService(ChamadoRepository chamadoRepository, PrioridadeService prioridadeService,
                            @Value("${app.sla.alerta-percentual:75}") int percentualAlerta) {
        this.chamadoRepository = chamadoRepository;
        this.prioridadeService = prioridadeService;
        this.percentualAlerta = percentualAlerta;
    }

    /** null quando o chamado não está pendente (não faz sentido alertar). */
    public Nivel nivel(Chamado c) {
        if (!ChamadoService.PENDENTES.contains(c.getStatus())) return null;
        LocalDateTime agora = LocalDateTime.now();
        if (!agora.isBefore(c.getPrazoSla())) return Nivel.VENCIDO;
        return percentualConsumido(c) >= percentualAlerta ? Nivel.PROXIMO : Nivel.OK;
    }

    public boolean isEmAlerta(Chamado c) {
        Nivel n = nivel(c);
        return n == Nivel.PROXIMO || n == Nivel.VENCIDO;
    }

    /**
     * Percentual do SLA já consumido (0 a 100+), medido pelo tempo que falta em relação ao SLA da prioridade.
     * Ex.: prioridade Alta (8h) com 2h restantes = 75% consumido.
     */
    public int percentualConsumido(Chamado c) {
        long slaMin = prioridadeService.horasSla(c.getPrioridade()) * 60L;
        long restante = Duration.between(LocalDateTime.now(), c.getPrazoSla()).toMinutes();
        if (slaMin <= 0) return 100;
        return (int) Math.max(0, (slaMin - restante) * 100 / slaMin);
    }

    /** "vence em 1h 20min" ou "vencido há 15min". */
    public String textoRestante(Chamado c) {
        long min = Duration.between(LocalDateTime.now(), c.getPrazoSla()).toMinutes();
        if (min < 0) return "vencido há " + formatar(-min);
        return "vence em " + formatar(Math.max(min, 1));
    }

    /**
     * Chamados em alerta que interessam ao usuário:
     *  - VENCIDO: TODOS os técnicos (N1, N2, N3) e o supervisor são avisados, de qualquer nível;
     *  - PROXIMO: supervisor vê todos; técnico vê os do seu nível, os de níveis abaixo e os que estão com ele.
     */
    public List<Chamado> alertasPara(Usuario u) {
        if (u == null || !(u.isTecnico() || u.isSupervisor())) return List.of();
        return chamadoRepository.findAll().stream()
                .filter(this::isEmAlerta)
                .filter(c -> u.isSupervisor() || nivel(c) == Nivel.VENCIDO
                        || (u.getGrupo() != null && c.getGrupoAtual().getNivel() <= u.getGrupo().getNivel())
                        || (c.getTecnicoResponsavel() != null && c.getTecnicoResponsavel().getId().equals(u.getId()))
                        || (c.getTecnicoDesignado() != null && c.getTecnicoDesignado().getId().equals(u.getId())))
                .sorted(Comparator.comparing(Chamado::getPrazoSla))
                .toList();
    }

    /** Todos os chamados pendentes com SLA vencido (faixa vermelha para toda a equipe). */
    public List<Chamado> vencidos() {
        return chamadoRepository.findAll().stream()
                .filter(c -> nivel(c) == Nivel.VENCIDO)
                .sorted(Comparator.comparing(Chamado::getPrazoSla))
                .toList();
    }

    public int getPercentualAlerta() { return percentualAlerta; }

    private static String formatar(long minutos) {
        long d = minutos / 1440, h = (minutos % 1440) / 60, m = minutos % 60;
        if (d > 0) return d + "d " + h + "h";
        if (h > 0) return h + "h " + m + "min";
        return m + "min";
    }
}
