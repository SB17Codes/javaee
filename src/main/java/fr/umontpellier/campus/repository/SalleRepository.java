package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.domain.TypeSalle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalleRepository extends JpaRepository<Salle, String> {
  List<Salle> findByTypeSOrderByNumS(TypeSalle typeS);

  List<Salle> findByCapaciteGreaterThanEqualOrderByCapaciteDesc(Integer capacite);

  List<Salle> findByAccesIgnoreCaseOrderByNumS(String acces);

  List<Salle> findByBatiment_CodeBOrderByNumS(String codeB);

  List<Salle> findByBatiment_Campus_NomCOrderByNumS(String nomC);
}
