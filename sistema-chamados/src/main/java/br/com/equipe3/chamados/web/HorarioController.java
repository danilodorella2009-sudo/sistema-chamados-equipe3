package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.service.HorarioFuncionamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

/** Tela do supervisor para ver o horário de atendimento e simular horários na apresentação. */
@Controller
@RequestMapping("/admin/horario")
public class HorarioController {

    private final HorarioFuncionamentoService horario;

    public HorarioController(HorarioFuncionamentoService horario) {
        this.horario = horario;
    }

    @GetMapping
    public String tela(Model model) {
        return "horario";
    }

    @PostMapping("/simular")
    public String simular(@RequestParam String hora, RedirectAttributes ra) {
        try {
            horario.simular(LocalTime.parse(hora));
            ra.addFlashAttribute("sucesso", "Relógio simulado em " + horario.getAgoraFormatado()
                    + ". Atendimento " + (horario.isAberto() ? "ABERTO" : "FECHADO") + " para novos chamados.");
        } catch (DateTimeParseException e) {
            ra.addFlashAttribute("erro", "Horário inválido. Use o formato HH:mm (ex.: 21:00).");
        }
        return "redirect:/admin/horario";
    }

    @PostMapping("/real")
    public String usarReal(RedirectAttributes ra) {
        horario.usarHorarioReal();
        ra.addFlashAttribute("sucesso", "Simulação desligada. O sistema voltou a usar o horário real ("
                + horario.getAgoraFormatado() + ").");
        return "redirect:/admin/horario";
    }
}
