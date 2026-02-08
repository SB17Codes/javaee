package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.OsmBuilding;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OsmBuildingRepository extends JpaRepository<OsmBuilding, Long> {
  List<OsmBuilding> findByCampusOrderByNameAsc(String campus);
}
