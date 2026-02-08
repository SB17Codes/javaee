package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Role;
import fr.umontpellier.campus.service.UserAdminService;
import fr.umontpellier.campus.service.UserAdminService.CreatedUser;
import java.util.Arrays;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserAdminController {
  private final UserAdminService userAdminService;

  public UserAdminController(UserAdminService userAdminService) {
    this.userAdminService = userAdminService;
  }

  @GetMapping("/admin/users")
  public String users(Model model) {
    model.addAttribute("users", userAdminService.findAll());
    model.addAttribute("roles", Arrays.asList(Role.values()));
    model.addAttribute("enabledAdmins", userAdminService.countEnabledAdmins());
    return "admin-users";
  }

  @PostMapping("/admin/users/create")
  public String create(@RequestParam String username,
      @RequestParam Role role,
      RedirectAttributes redirectAttributes) {
    try {
      CreatedUser created = userAdminService.createUser(username, role);
      redirectAttributes.addFlashAttribute("generatedPassword", created.rawPassword());
      redirectAttributes.addFlashAttribute("createdUser", created.user().getUsername());
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }
    return "redirect:/admin/users";
  }

  @PostMapping("/admin/users/role")
  public String updateRole(@RequestParam Long id,
      @RequestParam Role role,
      RedirectAttributes redirectAttributes) {
    try {
      userAdminService.updateRole(id, role);
      redirectAttributes.addFlashAttribute("successMessage", "Role mis a jour.");
    } catch (IllegalArgumentException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }
    return "redirect:/admin/users";
  }

  @PostMapping("/admin/users/toggle")
  public String toggle(@RequestParam Long id,
      RedirectAttributes redirectAttributes) {
    try {
      userAdminService.toggleEnabled(id);
      redirectAttributes.addFlashAttribute("successMessage", "Statut mis a jour.");
    } catch (IllegalArgumentException | IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }
    return "redirect:/admin/users";
  }

  @PostMapping("/admin/users/delete")
  public String delete(@RequestParam Long id,
      RedirectAttributes redirectAttributes) {
    try {
      userAdminService.deleteUser(id);
      redirectAttributes.addFlashAttribute("successMessage", "Utilisateur supprime.");
    } catch (IllegalArgumentException | IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
    }
    return "redirect:/admin/users";
  }
}
