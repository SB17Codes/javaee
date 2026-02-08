package fr.umontpellier.campus.repository;

import fr.umontpellier.campus.domain.CampusBoundary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampusBoundaryRepository extends JpaRepository<CampusBoundary, String> {
}
