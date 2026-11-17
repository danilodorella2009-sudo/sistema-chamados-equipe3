package br.com.equipe3.chamados.service;

import br.com.equipe3.chamados.model.*;
import br.com.equipe3.chamados.repository.ChamadoRepository;
import br.com.equipe3.chamados.repository.GrupoRepository;
import br.com.equipe3.chamados.repository.HistoricoChamadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final ChamadoRepository chamadoRepository;
    private final GrupoRepository grupoRepository;
    private final HistoricoChamadoRepository historicoRepository;

    public DashboardService(ChamadoRepository chamadoRepository, GrupoRepository grupoRepository,
                            HistoricoChamadoRepository historicoRepository) {
        this.chamadoRepository = chamadoRepository;
        this.grupoRepository = grupoRepository;
        this.historicoRepository = historicoRepository;
    }

    public Map<StatusChamado, Long> porStatus() {
        return preencher(StatusChamado.values(), chamadoRepository.contarPorStatus());
    }

    public Map<Prioridade, Long> porPrioridade() {
        return preencher(Prioridade.values(), chamadoRepository.contarPorPrioridade());
    }

    public Map<TipoChamado, Long> porTipo() {
        return preencher(TipoChamado.values(), chamadoRepository.contarPorTipo());
    }

    public Map<String, Long> porCategoria() {
        Map<String, Long> m = new LinkedHashMap<>();
        chamadoRepository.contarPorCategoria().forEach(r -> m.put((String) r[0], (Long) r[1]));
        return m;
    }

    public Map<String, Long> porNivel() {
        Map<String, Long> m = new LinkedHashMap<>();
        grupoRepository.findAllByOrderByNivelAsc().forEach(g -> m.put(g.getSigla(), 0L));
        chamadoRepository.contarPorNivel().forEach(r -> m.put((String) r[0], (Long) r[1]));
        return m;
    }

    /** Indicadores de desempenho do suporte (SLA, tempo médio, escalonamentos, resolução no N1). */
    public Map<String, String> indicadoresSla() {
        List<Chamado> todos = chamadoRepository.findAll();
        long total = todos.size();
        long pendentesVencidos = todos.stream()
                .filter(c -> ChamadoService.PENDENTES.contains(c.getStatus()) && c.isSlaVencido()).count();
        List<Chamado> resolvidos = todos.stream().filter(c -> c.getDataSolucao() != null).toList();
        long noPrazo = resolvidos.stream().filter(c -> !c.isSlaVencido()).count();
        double mediaHoras = resolvidos.stream()
                .mapToLong(c -> Duration.between(c.getDataAbertura(), c.getDataSolucao()).toMinutes())
                .average().orElse(0) / 60.0;
        long resolvidosN1 = resolvidos.stream().filter(c -> c.getGrupoAtual().getNivel() == 1).count();
        long escalonamentos = historicoRepository.countByAcaoIn(
                List.of(AcaoHistorico.ESCALONAMENTO, AcaoHistorico.ESCALONAMENTO_AUTOMATICO,
                        AcaoHistorico.ASSUMIDO_NIVEL_SUPERIOR));

        Map<String, String> m = new LinkedHashMap<>();
        m.put("Total de chamados", String.valueOf(total));
        m.put("Pendentes com SLA vencido", String.valueOf(pendentesVencidos));
        m.put("Resolvidos dentro do SLA", resolvidos.isEmpty() ? "-" :
                String.format("%d de %d (%.0f%%)", noPrazo, resolvidos.size(), 100.0 * noPrazo / resolvidos.size()));
        m.put("Tempo médio de solução", resolvidos.isEmpty() ? "-" : String.format("%.1f h", mediaHoras));
        m.put("Resolvidos no N1 (first level resolution)", resolvidos.isEmpty() ? "-" :
                String.format("%d de %d (%.0f%%)", resolvidosN1, resolvidos.size(), 100.0 * resolvidosN1 / resolvidos.size()));
        m.put("Escalonamentos registrados", String.valueOf(escalonamentos));
        return m;
    }

    private <E extends Enum<E>> Map<E, Long> preencher(E[] valores, List<Object[]> linhas) {
        Map<E, Long> m = new LinkedHashMap<>();
        for (E v : valores) m.put(v, 0L);
        for (Object[] r : linhas) {
            @SuppressWarnings("unchecked") E chave = (E) r[0];
            m.put(chave, (Long) r[1]);
        }
        return m;
    }
}
