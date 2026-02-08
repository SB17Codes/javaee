package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.AppUser;
import fr.umontpellier.campus.repository.AppUserRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
  private final AppUserRepository appUserRepository;

  public AppUserDetailsService(AppUserRepository appUserRepository) {
    this.appUserRepository = appUserRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    Optional<AppUser> userOpt = appUserRepository.findByUsername(username);
    if (userOpt.isEmpty()) {
      throw new UsernameNotFoundException("User not found");
    }
    AppUser user = userOpt.get();
    return User.withUsername(user.getUsername())
        .password(user.getPasswordHash())
        .roles(user.getRole().name())
        .disabled(!user.isEnabled())
        .build();
  }
}
