package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.service.BatimentService;
import fr.umontpellier.campus.service.DistanceService;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/distance")
public class DistanceController {
  private final BatimentService batimentService;
  private final DistanceService distanceService;

  public DistanceController(BatimentService batimentService, DistanceService distanceService) {
    this.batimentService = batimentService;
    this.distanceService = distanceService;
  }

  @GetMapping
  public String distance(
      @RequestParam(required = false) String from,
      @RequestParam(required = false) String to,
      Model model) {

    model.addAttribute("batimentList", batimentService.findAll());

    if (!StringUtils.hasText(from) || !StringUtils.hasText(to)) {
      return "distance";
    }

    Optional<Batiment> fromBatiment = batimentService.findById(from);
    Optional<Batiment> toBatiment = batimentService.findById(to);

    if (fromBatiment.isEmpty() || toBatiment.isEmpty()) {
      model.addAttribute("message", "Batiment introuvable");
      return "distance";
    }

    Batiment a = fromBatiment.get();
    Batiment b = toBatiment.get();
    Double km = distanceService.distanceKm(a, b);

    if (km == null) {
      model.addAttribute("message", "Coordonnees manquantes pour calculer la distance");
      return "distance";
    }

    model.addAttribute("fromBatiment", a);
    model.addAttribute("toBatiment", b);
    model.addAttribute("distanceKm", km);
    if (a.getCampus() != null && b.getCampus() != null) {
      model.addAttribute("sameCampus", a.getCampus().getNomC().equals(b.getCampus().getNomC()));
    }

    return "distance";
  }
}
