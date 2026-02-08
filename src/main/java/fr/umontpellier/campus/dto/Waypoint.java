package fr.umontpellier.campus.dto;

public class Waypoint {
  private String label;
  private Double latitude;
  private Double longitude;
  private String timeLabel;

  public Waypoint() {
  }

  public Waypoint(String label, Double latitude, Double longitude, String timeLabel) {
    this.label = label;
    this.latitude = latitude;
    this.longitude = longitude;
    this.timeLabel = timeLabel;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
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

  public String getTimeLabel() {
    return timeLabel;
  }

  public void setTimeLabel(String timeLabel) {
    this.timeLabel = timeLabel;
  }
}
