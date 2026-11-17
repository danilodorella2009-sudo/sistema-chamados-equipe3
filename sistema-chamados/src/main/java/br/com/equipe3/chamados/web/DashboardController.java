package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        var porStatus = dashboardService.porStatus();
        model.addAttribute("porStatus", porStatus);
        model.addAttribute("total", porStatus.values().stream().mapToLong(Long::longValue).sum());
        model.addAttribute("porPrioridade", dashboardService.porPrioridade());
        model.addAttribute("porTipo", dashboardService.porTipo());
        model.addAttribute("porCategoria", dashboardService.porCategoria());
        model.addAttribute("porNivel", dashboardService.porNivel());
        model.addAttribute("indicadores", dashboardService.indicadoresSla());
        return "dashboard";
    }
}
