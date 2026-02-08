package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Composante;
import fr.umontpellier.campus.service.ComposanteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/composantes")
public class ComposanteController {
  private final ComposanteService composanteService;

  public ComposanteController(ComposanteService composanteService) {
    this.composanteService = composanteService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("composanteList", composanteService.findAll());
    return "composantes";
  }

  @PostMapping("/create")
  public String create(@RequestParam String acronyme,
                       @RequestParam String nom,
                       @RequestParam String responsable) {
    composanteService.save(new Composante(acronyme, nom, responsable));
    return "redirect:/composantes";
  }

  @PostMapping("/update")
  public String update(@RequestParam String acronyme,
                       @RequestParam String nom,
                       @RequestParam String responsable) {
    composanteService.save(new Composante(acronyme, nom, responsable));
    return "redirect:/composantes";
  }

  @PostMapping("/delete")
  public String delete(@RequestParam String acronyme) {
    composanteService.deleteById(acronyme);
    return "redirect:/composantes";
  }
}
