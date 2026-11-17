package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.model.Chamado;
import br.com.equipe3.chamados.model.Usuario;
import br.com.equipe3.chamados.repository.UsuarioRepository;
import br.com.equipe3.chamados.service.RegraNegocioException;
import br.com.equipe3.chamados.service.SlaAlertaService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

/** Disponibiliza o usuário logado em todas as telas e trata erros de regra de negócio em GETs. */
@ControllerAdvice
public class UsuarioLogadoAdvice {

    private final UsuarioRepository usuarioRepository;
    private final SlaAlertaService slaAlertaService;

    public UsuarioLogadoAdvice(UsuarioRepository usuarioRepository, SlaAlertaService slaAlertaService) {
        this.usuarioRepository = usuarioRepository;
        this.slaAlertaService = slaAlertaService;
    }

    /** SLA vencido: faixa vermelha para TODOS os técnicos (N1, N2, N3) e o supervisor. */
    @ModelAttribute("slaVencidos")
    public List<Chamado> slaVencidos() {
        Usuario u = usuarioLogado();
        return (u != null && (u.isTecnico() || u.isSupervisor())) ? slaAlertaService.vencidos() : List.of();
    }

    /** Chamados com prazo próximo de acabar ou vencido (sino da barra superior e aviso flutuante). */
    @ModelAttribute("alertasSla")
    public List<Chamado> alertasSla() {
        Usuario u = usuarioLogado();
        return u == null ? List.of() : slaAlertaService.alertasPara(u);
    }

    @ModelAttribute("usuarioLogado")
    public Usuario usuarioLogado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) return null;
        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ModelAndView regraNegocio(RegraNegocioException e) {
        ModelAndView mv = new ModelAndView("erro");
        mv.addObject("mensagem", e.getMessage());
        mv.addObject("usuarioLogado", usuarioLogado());
        mv.addObject("alertasSla", alertasSla());
        mv.addObject("slaVencidos", slaVencidos());
        return mv;
    }
}
