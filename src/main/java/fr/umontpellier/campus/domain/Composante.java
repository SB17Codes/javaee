package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "composante")
public class Composante {
  @Id
  @Column(name = "acronyme", length = 8)
  private String acronyme;

  @Column(name = "nom", length = 50)
  private String nom;

  @Column(name = "responsable", length = 30)
  private String responsable;

  @OneToMany(mappedBy = "composante")
  private List<Exploite> exploitations = new ArrayList<>();

  public Composante() {}

  public Composante(String acronyme, String nom, String responsable) {
    this.acronyme = acronyme;
    this.nom = nom;
    this.responsable = responsable;
  }

  public String getAcronyme() {
    return acronyme;
  }

  public void setAcronyme(String acronyme) {
    this.acronyme = acronyme;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public String getResponsable() {
    return responsable;
  }

  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  public List<Exploite> getExploitations() {
    return exploitations;
  }

  public void setExploitations(List<Exploite> exploitations) {
    this.exploitations = exploitations;
  }
}
