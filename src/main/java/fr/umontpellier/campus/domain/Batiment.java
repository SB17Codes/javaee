package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "batiment")
public class Batiment {
  @Id
  @Column(name = "codeB", length = 32)
  private String codeB;

  @Column(name = "name", columnDefinition = "TEXT")
  private String name;

  @Column(name = "osm_id")
  private Long osmId;

  @Column(name = "building_number")
  private Integer buildingNumber;

  @Column(name = "anneeC")
  private Integer anneeC;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campus")
  private Campus campus;

  @OneToMany(mappedBy = "batiment")
  private List<Salle> salles = new ArrayList<>();

  @OneToMany(mappedBy = "batiment")
  private List<Exploite> exploitations = new ArrayList<>();

  public Batiment() {}

  public Batiment(String codeB, Integer anneeC, Double latitude, Double longitude, Campus campus) {
    this.codeB = codeB;
    this.anneeC = anneeC;
    this.latitude = latitude;
    this.longitude = longitude;
    this.campus = campus;
  }

  public Batiment(String codeB, String name, Long osmId, Integer buildingNumber,
                  Integer anneeC, Double latitude, Double longitude, Campus campus) {
    this.codeB = codeB;
    this.name = name;
    this.osmId = osmId;
    this.buildingNumber = buildingNumber;
    this.anneeC = anneeC;
    this.latitude = latitude;
    this.longitude = longitude;
    this.campus = campus;
  }

  public String getCodeB() {
    return codeB;
  }

  public void setCodeB(String codeB) {
    this.codeB = codeB;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getOsmId() {
    return osmId;
  }

  public void setOsmId(Long osmId) {
    this.osmId = osmId;
  }

  public Integer getBuildingNumber() {
    return buildingNumber;
  }

  public void setBuildingNumber(Integer buildingNumber) {
    this.buildingNumber = buildingNumber;
  }

  public Integer getAnneeC() {
    return anneeC;
  }

  public void setAnneeC(Integer anneeC) {
    this.anneeC = anneeC;
  }

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Campus getCampus() {
    return campus;
  }

  public void setCampus(Campus campus) {
    this.campus = campus;
  }

  public List<Salle> getSalles() {
    return salles;
  }

  public void setSalles(List<Salle> salles) {
    this.salles = salles;
  }

  public List<Exploite> getExploitations() {
    return exploitations;
  }

  public void setExploitations(List<Exploite> exploitations) {
    this.exploitations = exploitations;
  }
}
