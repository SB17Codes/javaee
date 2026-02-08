package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.repository.CampusRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CampusService {
  private final CampusRepository campusRepository;

  public CampusService(CampusRepository campusRepository) {
    this.campusRepository = campusRepository;
  }

  public List<Campus> findAll() {
    return campusRepository.findAll(Sort.by("nomC"));
  }

  public Optional<Campus> findById(String nomC) {
    return campusRepository.findById(nomC);
  }

  public Campus save(Campus campus) {
    return campusRepository.save(campus);
  }

  public void deleteById(String nomC) {
    campusRepository.deleteById(nomC);
  }
}
