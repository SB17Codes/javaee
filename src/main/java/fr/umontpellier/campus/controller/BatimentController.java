package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.service.BatimentService;
import fr.umontpellier.campus.service.CampusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/batiments")
public class BatimentController {
  private final BatimentService batimentService;
  private final CampusService campusService;

  public BatimentController(BatimentService batimentService, CampusService campusService) {
    this.batimentService = batimentService;
    this.campusService = campusService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("batimentList", batimentService.findAll());
    model.addAttribute("campusList", campusService.findAll());
    return "batiments";
  }

  @PostMapping("/create")
  public String create(@RequestParam String codeB,
                       @RequestParam(required = false) Integer anneeC,
                       @RequestParam(required = false) Double latitude,
                       @RequestParam(required = false) Double longitude,
                       @RequestParam String campusNom) {
    Campus campus = campusService.findById(campusNom).orElse(null);
    if (campus == null) {
      return "redirect:/batiments?error=campus";
    }
    batimentService.save(new Batiment(codeB, anneeC, latitude, longitude, campus));
    return "redirect:/batiments";
  }

  @PostMapping("/update")
  public String update(@RequestParam String codeB,
                       @RequestParam(required = false) Integer anneeC,
                       @RequestParam(required = false) Double latitude,
                       @RequestParam(required = false) Double longitude,
                       @RequestParam String campusNom) {
    Campus campus = campusService.findById(campusNom).orElse(null);
    if (campus == null) {
      return "redirect:/batiments?error=campus";
    }
    Batiment existing = batimentService.findById(codeB).orElse(null);
    Batiment updated = existing != null ? existing : new Batiment();
    updated.setCodeB(codeB);
    updated.setAnneeC(anneeC);
    updated.setLatitude(latitude);
    updated.setLongitude(longitude);
    updated.setCampus(campus);
    batimentService.save(updated);
    return "redirect:/batiments";
  }

  @PostMapping("/delete")
  public String delete(@RequestParam String codeB) {
    batimentService.deleteById(codeB);
    return "redirect:/batiments";
  }
}
