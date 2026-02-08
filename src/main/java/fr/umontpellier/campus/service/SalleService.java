package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.repository.SalleRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class SalleService {
  private final SalleRepository salleRepository;

  public SalleService(SalleRepository salleRepository) {
    this.salleRepository = salleRepository;
  }

  public List<Salle> findAll() {
    return salleRepository.findAll(Sort.by("numS"));
  }

  public Optional<Salle> findById(String numS) {
    return salleRepository.findById(numS);
  }

  public Salle save(Salle salle) {
    return salleRepository.save(salle);
  }

  public void deleteById(String numS) {
    salleRepository.deleteById(numS);
  }
}
