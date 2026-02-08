package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.AppUser;
import fr.umontpellier.campus.domain.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByUsername(String username);

  long countByRoleAndEnabledTrue(Role role);
}
