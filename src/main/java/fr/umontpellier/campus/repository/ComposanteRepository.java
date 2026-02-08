package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.Composante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComposanteRepository extends JpaRepository<Composante, String> {
  List<Composante> findByResponsableContainingIgnoreCaseOrderByAcronyme(String responsable);
}
