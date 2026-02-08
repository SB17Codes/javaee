package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.TypeSalle;
import fr.umontpellier.campus.service.BatimentService;
import fr.umontpellier.campus.service.CampusService;
import fr.umontpellier.campus.service.ComposanteService;
import fr.umontpellier.campus.service.QueriesService;
import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/queries")
public class QueriesController {
  private final QueriesService queriesService;
  private final CampusService campusService;
  private final BatimentService batimentService;
  private final ComposanteService composanteService;

  public QueriesController(
      QueriesService queriesService,
      CampusService campusService,
      BatimentService batimentService,
      ComposanteService composanteService) {
    this.queriesService = queriesService;
    this.campusService = campusService;
    this.batimentService = batimentService;
    this.composanteService = composanteService;
  }

  @GetMapping
  public String queries(
      @RequestParam(required = false) String type,
      @RequestParam(required = false) String ville,
      @RequestParam(required = false) String campus,
      @RequestParam(required = false) Integer minAnnee,
      @RequestParam(required = false) TypeSalle typeS,
      @RequestParam(required = false) Integer minCapacite,
      @RequestParam(required = false) String acces,
      @RequestParam(required = false) String batiment,
      @RequestParam(required = false) String responsable,
      @RequestParam(required = false) String acronyme,
      Model model) {

    model.addAttribute("campusList", campusService.findAll());
    model.addAttribute("batimentList", batimentService.findAll());
    model.addAttribute("composanteList", composanteService.findAll());
    model.addAttribute("typeSalleList", TypeSalle.values());
    model.addAttribute("campusByVilleResults", Collections.emptyList());
    model.addAttribute("batimentsByCampusResults", Collections.emptyList());
    model.addAttribute("batimentsByAnneeResults", Collections.emptyList());
    model.addAttribute("sallesByTypeResults", Collections.emptyList());
    model.addAttribute("sallesByCapaciteResults", Collections.emptyList());
    model.addAttribute("sallesByAccesResults", Collections.emptyList());
    model.addAttribute("sallesByBatimentResults", Collections.emptyList());
    model.addAttribute("sallesByCampusResults", Collections.emptyList());
    model.addAttribute("composantesByResponsableResults", Collections.emptyList());
    model.addAttribute("exploitationsByComposanteResults", Collections.emptyList());
    model.addAttribute("exploitationsByBatimentResults", Collections.emptyList());

    if (!StringUtils.hasText(type)) {
      return "queries";
    }

    model.addAttribute("resultType", type);

    switch (type) {
      case "campusByVille":
        if (StringUtils.hasText(ville)) {
          model.addAttribute("campusByVilleResults", queriesService.campusesByVille(ville));
        } else {
          model.addAttribute("message", "Ville manquante");
        }
        break;
      case "batimentsByCampus":
        if (StringUtils.hasText(campus)) {
          model.addAttribute("batimentsByCampusResults", queriesService.batimentsByCampus(campus));
        } else {
          model.addAttribute("message", "Campus manquant");
        }
        break;
      case "batimentsByAnnee":
        if (minAnnee != null) {
          model.addAttribute("batimentsByAnneeResults", queriesService.batimentsByAnnee(minAnnee));
        } else {
          model.addAttribute("message", "Annee minimale manquante");
        }
        break;
      case "sallesByType":
        if (typeS != null) {
          model.addAttribute("sallesByTypeResults", queriesService.sallesByType(typeS));
        } else {
          model.addAttribute("message", "Type de salle manquant");
        }
        break;
      case "sallesByCapacite":
        if (minCapacite != null) {
          model.addAttribute("sallesByCapaciteResults", queriesService.sallesByCapacite(minCapacite));
        } else {
          model.addAttribute("message", "Capacite minimale manquante");
        }
        break;
      case "sallesByAcces":
        if (StringUtils.hasText(acces)) {
          model.addAttribute("sallesByAccesResults", queriesService.sallesByAcces(acces));
        } else {
          model.addAttribute("message", "Acces manquant");
        }
        break;
      case "sallesByBatiment":
        if (StringUtils.hasText(batiment)) {
          model.addAttribute("sallesByBatimentResults", queriesService.sallesByBatiment(batiment));
        } else {
          model.addAttribute("message", "Batiment manquant");
        }
        break;
      case "sallesByCampus":
        if (StringUtils.hasText(campus)) {
          model.addAttribute("sallesByCampusResults", queriesService.sallesByCampus(campus));
        } else {
          model.addAttribute("message", "Campus manquant");
        }
        break;
      case "composantesByResponsable":
        if (StringUtils.hasText(responsable)) {
          model.addAttribute("composantesByResponsableResults",
              queriesService.composantesByResponsable(responsable));
        } else {
          model.addAttribute("message", "Responsable manquant");
        }
        break;
      case "exploitationsByComposante":
        if (StringUtils.hasText(acronyme)) {
          model.addAttribute("exploitationsByComposanteResults",
              queriesService.exploitationsByComposante(acronyme));
        } else {
          model.addAttribute("message", "Acronyme manquant");
        }
        break;
      case "exploitationsByBatiment":
        if (StringUtils.hasText(batiment)) {
          model.addAttribute("exploitationsByBatimentResults",
              queriesService.exploitationsByBatiment(batiment));
        } else {
          model.addAttribute("message", "Batiment manquant");
        }
        break;
      default:
        model.addAttribute("message", "Type de requete inconnue");
    }

    return "queries";
  }
}
