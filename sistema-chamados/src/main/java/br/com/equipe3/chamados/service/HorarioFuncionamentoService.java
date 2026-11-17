package br.com.equipe3.chamados.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Horário de funcionamento para ABERTURA de chamados (padrão 07:00 às 20:00).
 *
 * Para testes/apresentação existe um "relógio simulado": quando definido, o sistema usa esse horário
 * no lugar do relógio real para decidir se o atendimento está aberto. Ele pode ser definido pelo
 * supervisor na tela "Horário de atendimento" ou já na subida via app.horario.simulado=HH:mm.
 * A simulação afeta apenas a regra de horário (as datas gravadas nos chamados continuam reais).
 */
@Service
public class HorarioFuncionamentoService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalTime inicio;
    private final LocalTime fim;
    private volatile LocalTime simulado;

    public HorarioFuncionamentoService(@Value("${app.horario.inicio:07:00}") String inicio,
                                       @Value("${app.horario.fim:20:00}") String fim,
                                       @Value("${app.horario.simulado:}") String simulado) {
        this.inicio = LocalTime.parse(inicio);
        this.fim = LocalTime.parse(fim);
        this.simulado = StringUtils.hasText(simulado) ? LocalTime.parse(simulado) : null;
    }

    /** Hora usada pela regra: a simulada (se houver) ou a real. */
    public LocalTime agora() {
        return simulado != null ? simulado : LocalTime.now().withSecond(0).withNano(0);
    }

    /** Aberto das 07:00 (inclusive) até 20:00 (exclusive): às 20:00 em ponto já está fechado. */
    public boolean isAberto() {
        LocalTime t = agora();
        return !t.isBefore(inicio) && t.isBefore(fim);
    }

    public void exigirAberto() {
        if (!isAberto()) {
            throw new RegraNegocioException("Fora do horário de atendimento. Novos chamados podem ser abertos das "
                    + getInicio() + " às " + getFim() + " (agora: " + getAgoraFormatado() + ").");
        }
    }

    public void simular(LocalTime hora) { this.simulado = hora.withSecond(0).withNano(0); }
    public void usarHorarioReal() { this.simulado = null; }

    public boolean isSimulado() { return simulado != null; }
    public String getInicio() { return inicio.format(HH_MM); }
    public String getFim() { return fim.format(HH_MM); }
    public String getAgoraFormatado() { return agora().format(HH_MM); }
}
