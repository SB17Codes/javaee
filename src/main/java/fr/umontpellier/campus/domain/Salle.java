package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "salle")
public class Salle {
  @Id
  @Column(name = "numS", length = 16)
  private String numS;

  @Column(name = "capacite")
  private Integer capacite;

  @Column(name = "typeS", length = 12)
  @Convert(converter = TypeSalleConverter.class)
  private TypeSalle typeS;

  @Column(name = "acces", length = 3)
  private String acces;

  @Column(name = "etage", length = 3)
  private String etage;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "batiment")
  private Batiment batiment;

  public Salle() {}

  public Salle(String numS, Integer capacite, TypeSalle typeS, String acces, String etage, Batiment batiment) {
    this.numS = numS;
    this.capacite = capacite;
    this.typeS = typeS;
    this.acces = acces;
    this.etage = etage;
    this.batiment = batiment;
  }

  public String getNumS() {
    return numS;
  }

  public void setNumS(String numS) {
    this.numS = numS;
  }

  public Integer getCapacite() {
    return capacite;
  }

  public void setCapacite(Integer capacite) {
    this.capacite = capacite;
  }

  public TypeSalle getTypeS() {
    return typeS;
  }

  public void setTypeS(TypeSalle typeS) {
    this.typeS = typeS;
  }

  public String getAcces() {
    return acces;
  }

  public void setAcces(String acces) {
    this.acces = acces;
  }

  public String getEtage() {
    return etage;
  }

  public void setEtage(String etage) {
    this.etage = etage;
  }

  public Batiment getBatiment() {
    return batiment;
  }

  public void setBatiment(Batiment batiment) {
    this.batiment = batiment;
  }
}
