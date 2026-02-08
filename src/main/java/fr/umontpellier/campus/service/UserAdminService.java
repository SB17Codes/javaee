package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.AppUser;
import fr.umontpellier.campus.domain.Role;
import fr.umontpellier.campus.repository.AppUserRepository;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAdminService {
  private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";
  private final AppUserRepository appUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final SecureRandom random = new SecureRandom();

  public UserAdminService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
    this.appUserRepository = appUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<AppUser> findAll() {
    return appUserRepository.findAll();
  }

  public Optional<AppUser> findById(Long id) {
    return appUserRepository.findById(id);
  }

  public Optional<AppUser> findByUsername(String username) {
    return appUserRepository.findByUsername(username);
  }

  public long countEnabledAdmins() {
    return appUserRepository.countByRoleAndEnabledTrue(Role.ADMIN);
  }

  @Transactional
  public CreatedUser createUser(String username, Role role) {
    String normalized = username == null ? "" : username.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("Le nom d'utilisateur est requis.");
    }
    if (appUserRepository.findByUsername(normalized).isPresent()) {
      throw new IllegalArgumentException("Ce nom d'utilisateur existe deja.");
    }
    String rawPassword = generatePassword(12);
    String hash = passwordEncoder.encode(rawPassword);
    AppUser user = new AppUser(normalized, hash, role, true, LocalDateTime.now());
    appUserRepository.save(user);
    return new CreatedUser(user, rawPassword);
  }

  @Transactional
  public void updateRole(Long id, Role role) {
    AppUser user = appUserRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    user.setRole(role);
    appUserRepository.save(user);
  }

  @Transactional
  public void toggleEnabled(Long id) {
    AppUser user = appUserRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    if (user.getRole() == Role.ADMIN && user.isEnabled() && countEnabledAdmins() <= 1) {
      throw new IllegalStateException("Impossible de desactiver le dernier admin.");
    }
    user.setEnabled(!user.isEnabled());
    appUserRepository.save(user);
  }

  @Transactional
  public void deleteUser(Long id) {
    AppUser user = appUserRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    if (user.getRole() == Role.ADMIN && user.isEnabled() && countEnabledAdmins() <= 1) {
      throw new IllegalStateException("Impossible de supprimer le dernier admin.");
    }
    appUserRepository.delete(user);
  }

  private String generatePassword(int length) {
    StringBuilder builder = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int idx = random.nextInt(PASSWORD_CHARS.length());
      builder.append(PASSWORD_CHARS.charAt(idx));
    }
    return builder.toString();
  }

  public record CreatedUser(AppUser user, String rawPassword) {}
}
