package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.Batiment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatimentRepository extends JpaRepository<Batiment, String> {
  List<Batiment> findByCampus_NomCOrderByCodeB(String nomC);

  List<Batiment> findByAnneeCGreaterThanEqualOrderByAnneeCDesc(Integer anneeC);

  List<Batiment> findByCampus_NomCAndBuildingNumber(String nomC, Integer buildingNumber);
}
