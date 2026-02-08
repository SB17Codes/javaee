package fr.umontpellier.campus.dto;

public class ItineraryLeg {
  private IcsEvent from;
  private IcsEvent to;
  private Double distanceKm;
  private Integer walkingMinutes;

  public ItineraryLeg() {
  }

  public ItineraryLeg(IcsEvent from, IcsEvent to, Double distanceKm, Integer walkingMinutes) {
    this.from = from;
    this.to = to;
    this.distanceKm = distanceKm;
    this.walkingMinutes = walkingMinutes;
  }

  public IcsEvent getFrom() {
    return from;
  }

  public void setFrom(IcsEvent from) {
    this.from = from;
  }

  public IcsEvent getTo() {
    return to;
  }

  public void setTo(IcsEvent to) {
    this.to = to;
  }

  public Double getDistanceKm() {
    return distanceKm;
  }

  public void setDistanceKm(Double distanceKm) {
    this.distanceKm = distanceKm;
  }

  public Integer getWalkingMinutes() {
    return walkingMinutes;
  }

  public void setWalkingMinutes(Integer walkingMinutes) {
    this.walkingMinutes = walkingMinutes;
  }
}
