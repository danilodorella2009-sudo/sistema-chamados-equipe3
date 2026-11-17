package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.model.Usuario;
import br.com.equipe3.chamados.service.ChamadoService;
import br.com.equipe3.chamados.service.RegraNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Tela do supervisor para TESTAR os alertas de prazo na apresentação (ajusta o prazo de um chamado). */
@Controller
@RequestMapping("/admin/prazos")
public class PrazoTesteController {

    private final ChamadoService chamadoService;

    public PrazoTesteController(ChamadoService chamadoService) {
        this.chamadoService = chamadoService;
    }

    @GetMapping
    public String tela(Model model) {
        model.addAttribute("pendentes", chamadoService.todos().stream()
                .filter(c -> ChamadoService.PENDENTES.contains(c.getStatus())).toList());
        return "prazos";
    }

    @PostMapping("/ajustar")
    public String ajustar(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                          @RequestParam Long chamadoId, @RequestParam int minutos, RedirectAttributes ra) {
        try {
            chamadoService.ajustarPrazoParaTeste(chamadoId, u, minutos);
            ra.addFlashAttribute("sucesso", "Prazo do chamado #" + chamadoId + " ajustado: "
                    + (minutos >= 0 ? "vence em " + minutos + " min." : "vencido há " + (-minutos) + " min."));
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/prazos";
    }

    @PostMapping("/restaurar")
    public String restaurar(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u,
                            @RequestParam Long chamadoId, RedirectAttributes ra) {
        try {
            chamadoService.restaurarPrazo(chamadoId, u);
            ra.addFlashAttribute("sucesso", "Prazo do chamado #" + chamadoId + " restaurado pelo SLA original.");
        } catch (RegraNegocioException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/prazos";
    }
}
