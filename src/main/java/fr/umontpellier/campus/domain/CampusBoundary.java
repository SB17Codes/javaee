package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "campus_boundary")
public class CampusBoundary {
  @Id
  @Column(name = "campus", length = 32, nullable = false)
  private String campus;

  @Column(name = "source", length = 64)
  private String source;

  @Column(name = "geojson", columnDefinition = "TEXT")
  private String geojson;

  public CampusBoundary() {
  }

  public CampusBoundary(String campus, String source, String geojson) {
    this.campus = campus;
    this.source = source;
    this.geojson = geojson;
  }

  public String getCampus() {
    return campus;
  }

  public void setCampus(String campus) {
    this.campus = campus;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getGeojson() {
    return geojson;
  }

  public void setGeojson(String geojson) {
    this.geojson = geojson;
  }
}
