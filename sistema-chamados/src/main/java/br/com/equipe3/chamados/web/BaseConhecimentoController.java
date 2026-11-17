package br.com.equipe3.chamados.web;

import br.com.equipe3.chamados.model.ArtigoConhecimento;
import br.com.equipe3.chamados.model.Usuario;
import br.com.equipe3.chamados.repository.ArtigoConhecimentoRepository;
import br.com.equipe3.chamados.repository.CategoriaRepository;
import br.com.equipe3.chamados.service.RegraNegocioException;
import br.com.equipe3.chamados.web.form.ArtigoForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/kb")
public class BaseConhecimentoController {

    private final ArtigoConhecimentoRepository artigoRepository;
    private final CategoriaRepository categoriaRepository;

    public BaseConhecimentoController(ArtigoConhecimentoRepository artigoRepository, CategoriaRepository categoriaRepository) {
        this.artigoRepository = artigoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("artigos", StringUtils.hasText(q) ? artigoRepository.buscar(q.trim()) : artigoRepository.findAllByOrderByTituloAsc());
        model.addAttribute("q", q);
        return "kb/lista";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        model.addAttribute("a", artigo(id));
        return "kb/artigo";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("form", new ArtigoForm());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByIdAsc());
        return "kb/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ArtigoConhecimento a = artigo(id);
        ArtigoForm f = new ArtigoForm();
        f.setId(a.getId());
        f.setTitulo(a.getTitulo());
        f.setProblema(a.getProblema());
        f.setSolucao(a.getSolucao());
        f.setPalavrasChave(a.getPalavrasChave());
        f.setCategoriaId(a.getCategoria() != null ? a.getCategoria().getId() : null);
        model.addAttribute("form", f);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByIdAsc());
        return "kb/form";
    }

    @PostMapping
    public String salvar(@ModelAttribute(name = "usuarioLogado", binding = false) Usuario u, @Valid @ModelAttribute("form") ArtigoForm form,
                         BindingResult br, Model model, RedirectAttributes ra) {
        if (br.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAllByOrderByIdAsc());
            return "kb/form";
        }
        ArtigoConhecimento a;
        if (form.getId() != null) {
            a = artigo(form.getId());
            a.setDataAtualizacao(LocalDateTime.now());
        } else {
            a = new ArtigoConhecimento();
            a.setAutor(u);
        }
        a.setTitulo(form.getTitulo());
        a.setProblema(form.getProblema());
        a.setSolucao(form.getSolucao());
        a.setPalavrasChave(form.getPalavrasChave());
        a.setCategoria(form.getCategoriaId() != null ? categoriaRepository.findById(form.getCategoriaId()).orElse(null) : null);
        artigoRepository.save(a);
        ra.addFlashAttribute("sucesso", "Artigo salvo.");
        return "redirect:/kb/" + a.getId();
    }

    private ArtigoConhecimento artigo(Long id) {
        return artigoRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Artigo não encontrado."));
    }
}
