package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.service.CampusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/campus")
public class CampusController {
  private final CampusService campusService;

  public CampusController(CampusService campusService) {
    this.campusService = campusService;
  }

  @GetMapping
  public String list(Model model) {
    model.addAttribute("campusList", campusService.findAll());
    return "campus";
  }

  @PostMapping("/create")
  public String create(@RequestParam String nomC, @RequestParam String ville) {
    campusService.save(new Campus(nomC, ville));
    return "redirect:/campus";
  }

  @PostMapping("/update")
  public String update(@RequestParam String nomC, @RequestParam String ville) {
    campusService.save(new Campus(nomC, ville));
    return "redirect:/campus";
  }

  @PostMapping("/delete")
  public String delete(@RequestParam String nomC) {
    campusService.deleteById(nomC);
    return "redirect:/campus";
  }
}
