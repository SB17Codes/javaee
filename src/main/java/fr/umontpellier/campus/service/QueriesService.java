package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.domain.Composante;
import fr.umontpellier.campus.domain.Exploite;
import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.domain.TypeSalle;
import fr.umontpellier.campus.repository.BatimentRepository;
import fr.umontpellier.campus.repository.CampusRepository;
import fr.umontpellier.campus.repository.ComposanteRepository;
import fr.umontpellier.campus.repository.ExploiteRepository;
import fr.umontpellier.campus.repository.SalleRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QueriesService {
  private final CampusRepository campusRepository;
  private final BatimentRepository batimentRepository;
  private final SalleRepository salleRepository;
  private final ComposanteRepository composanteRepository;
  private final ExploiteRepository exploiteRepository;

  public QueriesService(
      CampusRepository campusRepository,
      BatimentRepository batimentRepository,
      SalleRepository salleRepository,
      ComposanteRepository composanteRepository,
      ExploiteRepository exploiteRepository) {
    this.campusRepository = campusRepository;
    this.batimentRepository = batimentRepository;
    this.salleRepository = salleRepository;
    this.composanteRepository = composanteRepository;
    this.exploiteRepository = exploiteRepository;
  }

  public List<Campus> campusesByVille(String ville) {
    return campusRepository.findByVilleIgnoreCaseOrderByNomC(ville);
  }

  public List<Batiment> batimentsByCampus(String campusNom) {
    return batimentRepository.findByCampus_NomCOrderByCodeB(campusNom);
  }

  public List<Batiment> batimentsByAnnee(Integer minAnnee) {
    return batimentRepository.findByAnneeCGreaterThanEqualOrderByAnneeCDesc(minAnnee);
  }

  public List<Salle> sallesByType(TypeSalle typeS) {
    return salleRepository.findByTypeSOrderByNumS(typeS);
  }

  public List<Salle> sallesByCapacite(Integer minCapacite) {
    return salleRepository.findByCapaciteGreaterThanEqualOrderByCapaciteDesc(minCapacite);
  }

  public List<Salle> sallesByAcces(String acces) {
    return salleRepository.findByAccesIgnoreCaseOrderByNumS(acces);
  }

  public List<Salle> sallesByBatiment(String codeB) {
    return salleRepository.findByBatiment_CodeBOrderByNumS(codeB);
  }

  public List<Salle> sallesByCampus(String campusNom) {
    return salleRepository.findByBatiment_Campus_NomCOrderByNumS(campusNom);
  }

  public List<Composante> composantesByResponsable(String responsable) {
    return composanteRepository.findByResponsableContainingIgnoreCaseOrderByAcronyme(responsable);
  }

  public List<Exploite> exploitationsByComposante(String acronyme) {
    return exploiteRepository.findByComposante_AcronymeOrderByBatiment_CodeB(acronyme);
  }

  public List<Exploite> exploitationsByBatiment(String codeB) {
    return exploiteRepository.findByBatiment_CodeBOrderByComposante_Acronyme(codeB);
  }
}
