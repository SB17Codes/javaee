package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.domain.TypeSalle;
import fr.umontpellier.campus.service.BatimentService;
import fr.umontpellier.campus.service.SalleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/salles")
public class SalleController {
  private final SalleService salleService;
  private final BatimentService batimentService;

  public SalleController(SalleService salleService, BatimentService batimentService) {
    this.salleService = salleService;
    this.batimentService = batimentService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("salleList", salleService.findAll());
    model.addAttribute("batimentList", batimentService.findAll());
    model.addAttribute("typeSalleList", TypeSalle.values());
    return "salles";
  }

  @PostMapping("/create")
  public String create(@RequestParam String numS,
                       @RequestParam(required = false) Integer capacite,
                       @RequestParam TypeSalle typeS,
                       @RequestParam String acces,
                       @RequestParam String etage,
                       @RequestParam String batimentCode) {
    Batiment batiment = batimentService.findById(batimentCode).orElse(null);
    if (batiment == null) {
      return "redirect:/salles?error=batiment";
    }
    salleService.save(new Salle(numS, capacite, typeS, acces, etage, batiment));
    return "redirect:/salles";
  }

  @PostMapping("/update")
  public String update(@RequestParam String numS,
                       @RequestParam(required = false) Integer capacite,
                       @RequestParam TypeSalle typeS,
                       @RequestParam String acces,
                       @RequestParam String etage,
                       @RequestParam String batimentCode) {
    Batiment batiment = batimentService.findById(batimentCode).orElse(null);
    if (batiment == null) {
      return "redirect:/salles?error=batiment";
    }
    salleService.save(new Salle(numS, capacite, typeS, acces, etage, batiment));
    return "redirect:/salles";
  }

  @PostMapping("/delete")
  public String delete(@RequestParam String numS) {
    salleService.deleteById(numS);
    return "redirect:/salles";
  }
}
