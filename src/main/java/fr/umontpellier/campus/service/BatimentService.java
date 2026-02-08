package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.repository.BatimentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BatimentService {
  private final BatimentRepository batimentRepository;

  public BatimentService(BatimentRepository batimentRepository) {
    this.batimentRepository = batimentRepository;
  }

  public List<Batiment> findAll() {
    return batimentRepository.findAll(Sort.by("codeB"));
  }

  public Optional<Batiment> findById(String codeB) {
    return batimentRepository.findById(codeB);
  }

  public Batiment save(Batiment batiment) {
    return batimentRepository.save(batiment);
  }

  public void deleteById(String codeB) {
    batimentRepository.deleteById(codeB);
  }
}
