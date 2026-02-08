package fr.umontpellier.campus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "osm_building")
public class OsmBuilding {
  @Id
  @Column(name = "osm_id")
  private Long osmId;

  @Column(name = "name", columnDefinition = "TEXT")
  private String name;

  @Column(name = "campus", length = 32)
  private String campus;

  @Column(name = "latitude")
  private Double latitude;

  @Column(name = "longitude")
  private Double longitude;

  @Column(name = "tags", columnDefinition = "TEXT")
  private String tags;

  public OsmBuilding() {}

  public Long getOsmId() {
    return osmId;
  }

  public void setOsmId(Long osmId) {
    this.osmId = osmId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCampus() {
    return campus;
  }

  public void setCampus(String campus) {
    this.campus = campus;
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

  public String getTags() {
    return tags;
  }

  public void setTags(String tags) {
    this.tags = tags;
  }
}
