package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.CampusBoundary;
import fr.umontpellier.campus.repository.CampusBoundaryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CampusBoundaryService {
  private final CampusBoundaryRepository campusBoundaryRepository;

  public CampusBoundaryService(CampusBoundaryRepository campusBoundaryRepository) {
    this.campusBoundaryRepository = campusBoundaryRepository;
  }

  public List<CampusBoundary> findAll() {
    return campusBoundaryRepository.findAll();
  }
}
