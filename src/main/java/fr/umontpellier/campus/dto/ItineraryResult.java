package fr.umontpellier.campus.dto;

import java.util.ArrayList;
import java.util.List;

public class ItineraryResult {
  private List<IcsEvent> events = new ArrayList<>();
  private List<ItineraryLeg> legs = new ArrayList<>();
  private List<Waypoint> waypoints = new ArrayList<>();
  private double totalDistanceKm;
  private int totalWalkingMinutes;

  public List<IcsEvent> getEvents() {
    return events;
  }

  public void setEvents(List<IcsEvent> events) {
    this.events = events == null ? new ArrayList<>() : events;
  }

  public List<ItineraryLeg> getLegs() {
    return legs;
  }

  public void setLegs(List<ItineraryLeg> legs) {
    this.legs = legs == null ? new ArrayList<>() : legs;
  }

  public List<Waypoint> getWaypoints() {
    return waypoints;
  }

  public void setWaypoints(List<Waypoint> waypoints) {
    this.waypoints = waypoints == null ? new ArrayList<>() : waypoints;
  }

  public double getTotalDistanceKm() {
    return totalDistanceKm;
  }

  public void setTotalDistanceKm(double totalDistanceKm) {
    this.totalDistanceKm = totalDistanceKm;
  }

  public int getTotalWalkingMinutes() {
    return totalWalkingMinutes;
  }

  public void setTotalWalkingMinutes(int totalWalkingMinutes) {
    this.totalWalkingMinutes = totalWalkingMinutes;
  }
}
