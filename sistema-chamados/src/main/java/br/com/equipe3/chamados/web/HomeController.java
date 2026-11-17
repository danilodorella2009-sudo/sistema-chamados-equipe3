package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.model.Usuario;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String home(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario usuario) {
        if (usuario == null) return "redirect:/login";
        if (usuario.isSolicitante()) return "redirect:/chamados/meus";
        if (usuario.isTecnico()) return "redirect:/chamados/fila";
        return "redirect:/dashboard";
    }
}
