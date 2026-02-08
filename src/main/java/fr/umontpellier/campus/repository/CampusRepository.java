package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.Campus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusRepository extends JpaRepository<Campus, String> {
  List<Campus> findByVilleIgnoreCaseOrderByNomC(String ville);
}
