package fr.umontpellier.campus.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MapController {
  @Value("${app.mapbox.token:}")
  private String mapboxToken;

  @GetMapping("/map")
  public String map(Model model) {
    model.addAttribute("mapboxToken", mapboxToken == null ? "" : mapboxToken);
    return "map";
  }
}
