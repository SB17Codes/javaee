package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "campus")
public class Campus {
  @Id
  @Column(name = "nomC", length = 16)
  private String nomC;

  @Column(name = "ville", length = 20)
  private String ville;

  @OneToMany(mappedBy = "campus")
  private List<Batiment> batiments = new ArrayList<>();

  public Campus() {}

  public Campus(String nomC, String ville) {
    this.nomC = nomC;
    this.ville = ville;
  }

  public String getNomC() {
    return nomC;
  }

  public void setNomC(String nomC) {
    this.nomC = nomC;
  }

  public String getVille() {
    return ville;
  }

  public void setVille(String ville) {
    this.ville = ville;
  }

  public List<Batiment> getBatiments() {
    return batiments;
  }

  public void setBatiments(List<Batiment> batiments) {
    this.batiments = batiments;
  }
}
