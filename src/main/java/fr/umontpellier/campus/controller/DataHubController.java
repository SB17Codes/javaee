package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.service.BatimentService;
import fr.umontpellier.campus.service.CampusService;
import fr.umontpellier.campus.service.ComposanteService;
import fr.umontpellier.campus.service.ExploiteService;
import fr.umontpellier.campus.service.SalleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DataHubController {
  private final CampusService campusService;
  private final BatimentService batimentService;
  private final SalleService salleService;
  private final ComposanteService composanteService;
  private final ExploiteService exploiteService;

  public DataHubController(
      CampusService campusService,
      BatimentService batimentService,
      SalleService salleService,
      ComposanteService composanteService,
      ExploiteService exploiteService) {
    this.campusService = campusService;
    this.batimentService = batimentService;
    this.salleService = salleService;
    this.composanteService = composanteService;
    this.exploiteService = exploiteService;
  }

  @GetMapping("/data")
  public String dataHub(Model model) {
    model.addAttribute("campusCount", campusService.findAll().size());
    model.addAttribute("batimentCount", batimentService.findAll().size());
    model.addAttribute("salleCount", salleService.findAll().size());
    model.addAttribute("composanteCount", composanteService.findAll().size());
    model.addAttribute("exploiteCount", exploiteService.findAll().size());
    return "data";
  }
}
