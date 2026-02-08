package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Role;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PermissionsController {
  @GetMapping("/permissions")
  public String permissions(Authentication authentication, Model model) {
    Role role = extractRole(authentication);
    model.addAttribute("currentRole", role != null ? role.name() : "UNKNOWN");

    Map<String, List<Boolean>> matrix = buildMatrix();
    model.addAttribute("permissionMatrix", matrix);
    model.addAttribute("permissionActions", List.of(
        "Voir pages",
        "Creer / Mettre a jour",
        "Supprimer",
        "Analytics / Distance / Itineraire",
        "Gestion utilisateurs"
    ));
    model.addAttribute("allowedActions", allowedActions(role));
    return "permissions";
  }

  private Role extractRole(Authentication authentication) {
    if (authentication == null || authentication.getAuthorities() == null) {
      return null;
    }
    return authentication.getAuthorities().stream()
        .map(auth -> auth.getAuthority().replace("ROLE_", ""))
        .map(value -> {
          try {
            return Role.valueOf(value);
          } catch (IllegalArgumentException ex) {
            return null;
          }
        })
        .filter(role -> role != null)
        .findFirst()
        .orElse(null);
  }

  private Map<String, List<Boolean>> buildMatrix() {
    Map<String, List<Boolean>> matrix = new LinkedHashMap<>();
    matrix.put("ADMIN", List.of(true, true, true, true, true));
    matrix.put("MANAGER", List.of(true, true, false, true, false));
    matrix.put("TEACHER", List.of(true, false, false, true, false));
    matrix.put("STUDENT", List.of(true, false, false, true, false));
    return matrix;
  }

  private List<String> allowedActions(Role role) {
    if (role == null) {
      return List.of();
    }
    return switch (role) {
      case ADMIN -> List.of(
          "Consulter toutes les pages",
          "Creer et mettre a jour les ressources",
          "Supprimer des ressources",
          "Utiliser Analytics, Distance et Itineraire",
          "Gerer les utilisateurs"
      );
      case MANAGER -> List.of(
          "Consulter toutes les pages",
          "Creer et mettre a jour les ressources",
          "Utiliser Analytics, Distance et Itineraire"
      );
      case TEACHER, STUDENT -> List.of(
          "Consulter toutes les pages",
          "Utiliser Analytics, Distance et Itineraire"
      );
    };
  }
}
