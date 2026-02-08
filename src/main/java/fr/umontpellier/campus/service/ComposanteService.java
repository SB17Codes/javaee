package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Composante;
import fr.umontpellier.campus.repository.ComposanteRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class ComposanteService {
  private final ComposanteRepository composanteRepository;

  public ComposanteService(ComposanteRepository composanteRepository) {
    this.composanteRepository = composanteRepository;
  }

  public List<Composante> findAll() {
    return composanteRepository.findAll(Sort.by("acronyme"));
  }

  public Optional<Composante> findById(String acronyme) {
    return composanteRepository.findById(acronyme);
  }

  public Composante save(Composante composante) {
    return composanteRepository.save(composante);
  }

  public void deleteById(String acronyme) {
    composanteRepository.deleteById(acronyme);
  }
}
