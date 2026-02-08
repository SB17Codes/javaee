package fr.umontpellier.campus.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class IcsEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  private String summary;
  private String rawLocation;
  private String normalizedLocation;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private String roomId;
  private String buildingCode;
  private String campusName;
  private Double latitude;
  private Double longitude;
  private List<String> warnings = new ArrayList<>();

  public IcsEvent() {
  }

  public IcsEvent(IcsEvent other) {
    this.summary = other.summary;
    this.rawLocation = other.rawLocation;
    this.normalizedLocation = other.normalizedLocation;
    this.start = other.start;
    this.end = other.end;
    this.roomId = other.roomId;
    this.buildingCode = other.buildingCode;
    this.campusName = other.campusName;
    this.latitude = other.latitude;
    this.longitude = other.longitude;
    this.warnings = new ArrayList<>(other.warnings);
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getRawLocation() {
    return rawLocation;
  }

  public void setRawLocation(String rawLocation) {
    this.rawLocation = rawLocation;
  }

  public String getNormalizedLocation() {
    return normalizedLocation;
  }

  public void setNormalizedLocation(String normalizedLocation) {
    this.normalizedLocation = normalizedLocation;
  }

  public ZonedDateTime getStart() {
    return start;
  }

  public void setStart(ZonedDateTime start) {
    this.start = start;
  }

  public ZonedDateTime getEnd() {
    return end;
  }

  public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getBuildingCode() {
    return buildingCode;
  }

  public void setBuildingCode(String buildingCode) {
    this.buildingCode = buildingCode;
  }

  public String getCampusName() {
    return campusName;
  }

  public void setCampusName(String campusName) {
    this.campusName = campusName;
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

  public List<String> getWarnings() {
    return warnings;
  }

  public void setWarnings(List<String> warnings) {
    this.warnings = warnings == null ? new ArrayList<>() : warnings;
  }

  public void addWarning(String warning) {
    if (warning != null && !warning.isBlank()) {
      this.warnings.add(warning);
    }
  }
}
