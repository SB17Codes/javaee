package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.domain.OsmBuilding;
import fr.umontpellier.campus.dto.IcsEvent;
import fr.umontpellier.campus.dto.ItineraryLeg;
import fr.umontpellier.campus.dto.ItineraryResult;
import fr.umontpellier.campus.dto.Waypoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ItineraryService {
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");
  private static final Pattern BUILDING_NUMBER = Pattern.compile("(\\d{1,3})\\s*\\.");
  private static final Pattern ROOM_TOKEN = Pattern.compile("([A-Z]{1,5})\\s*(\\d{1,3})\\s*\\.\\s*(\\d{1,3})");
  private static final Pattern OSM_NUMBER = Pattern.compile("\\b(?:batiment|bat|bâtiment|batiment|pavillon|pav|csu|building|bldg|bloc|block)\\s*0*(\\d{1,3})\\b");
  private static final Pattern ONLY_NUMBER = Pattern.compile("\\b(\\d{1,3})\\b");
  private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("HH:mm");

  private final SalleService salleService;
  private final OsmBuildingService osmBuildingService;
  private final DistanceService distanceService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public ItineraryService(SalleService salleService, OsmBuildingService osmBuildingService, DistanceService distanceService) {
    this.salleService = salleService;
    this.osmBuildingService = osmBuildingService;
    this.distanceService = distanceService;
  }

  public List<LocalDate> availableDates(List<IcsEvent> events) {
    Set<LocalDate> dates = new TreeSet<>();
    if (events == null) {
      return new ArrayList<>();
    }
    for (IcsEvent e : events) {
      if (e.getStart() != null) {
        dates.add(e.getStart().withZoneSameInstant(DEFAULT_ZONE).toLocalDate());
      }
    }
    return new ArrayList<>(dates);
  }

  public ItineraryResult buildItinerary(List<IcsEvent> events, LocalDate day) {
    ItineraryResult result = new ItineraryResult();
    if (events == null || day == null) {
      return result;
    }

    Map<String, Map<Integer, List<OsmBuilding>>> osmIndex = buildOsmIndex();
    Map<String, Salle> salleMap = buildSalleMap();
    String preferredCampus = inferPreferredCampus(events, osmIndex);
    List<IcsEvent> dayEvents = new ArrayList<>();
    for (IcsEvent e : events) {
      if (e.getStart() == null) {
        continue;
      }
      LocalDate eventDay = e.getStart().withZoneSameInstant(DEFAULT_ZONE).toLocalDate();
      if (!day.equals(eventDay)) {
        continue;
      }
      IcsEvent copy = new IcsEvent(e);
      matchLocation(copy, osmIndex, salleMap, preferredCampus);
      dayEvents.add(copy);
    }

    dayEvents.sort(Comparator.comparing(IcsEvent::getStart));
    result.setEvents(dayEvents);

    List<ItineraryLeg> legs = new ArrayList<>();
    double totalKm = 0.0;
    int totalMinutes = 0;

    for (int i = 0; i < dayEvents.size() - 1; i++) {
      IcsEvent from = dayEvents.get(i);
      IcsEvent to = dayEvents.get(i + 1);
      if (from.getLatitude() == null || from.getLongitude() == null
          || to.getLatitude() == null || to.getLongitude() == null) {
        continue;
      }
      Double km = distanceService.distanceKm(from.getLatitude(), from.getLongitude(),
          to.getLatitude(), to.getLongitude());
      if (km == null) {
        continue;
      }
      int minutes = (int) Math.round((km / 5.0) * 60.0);
      legs.add(new ItineraryLeg(from, to, km, minutes));
      totalKm += km;
      totalMinutes += minutes;
    }

    result.setLegs(legs);
    result.setTotalDistanceKm(totalKm);
    result.setTotalWalkingMinutes(totalMinutes);

    List<Waypoint> waypoints = new ArrayList<>();
    for (IcsEvent e : dayEvents) {
      if (e.getLatitude() == null || e.getLongitude() == null) {
        continue;
      }
      String label = buildLabel(e);
      String timeLabel = e.getStart() != null ? e.getStart().format(TIME_LABEL) : "";
      waypoints.add(new Waypoint(label, e.getLatitude(), e.getLongitude(), timeLabel));
    }
    result.setWaypoints(waypoints);

    return result;
  }

  private String inferPreferredCampus(List<IcsEvent> events, Map<String, Map<Integer, List<OsmBuilding>>> osmIndex) {
    if (events == null || events.isEmpty() || osmIndex.isEmpty()) {
      return null;
    }
    Map<String, Integer> counts = new HashMap<>();
    for (IcsEvent event : events) {
      String raw = event.getRawLocation();
      if (!StringUtils.hasText(raw)) {
        continue;
      }
      String cleaned = raw.replace("\\,", ",");
      String[] parts = cleaned.split(",");
      for (String part : parts) {
        Integer number = extractBuildingNumber(part);
        if (number == null) {
          continue;
        }
        for (Map.Entry<String, Map<Integer, List<OsmBuilding>>> entry : osmIndex.entrySet()) {
          String campus = entry.getKey();
          if (campus == null || campus.isBlank()) {
            continue;
          }
          if (entry.getValue().containsKey(number)) {
            counts.merge(campus, 1, Integer::sum);
          }
        }
      }
    }
    if (counts.isEmpty()) {
      return null;
    }
    int max = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    List<String> candidates = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      if (entry.getValue() == max) {
        candidates.add(entry.getKey());
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    if (candidates.contains("Triolet")) {
      return "Triolet";
    }
    candidates.sort(String::compareToIgnoreCase);
    return candidates.get(0);
  }

  private void matchLocation(IcsEvent event, Map<String, Map<Integer, List<OsmBuilding>>> osmIndex,
      Map<String, Salle> salleMap, String preferredCampus) {
    String raw = event.getRawLocation();
    if (!StringUtils.hasText(raw)) {
      event.addWarning("Lieu manquant dans l'emploi du temps");
      return;
    }

    String cleaned = raw.replace("\\,", ",");
    String[] parts = cleaned.split(",");
    for (String part : parts) {
      String normalized = normalizeLocation(part);
      String altNormalized = normalizePreserveCase(part);
      RoomToken roomToken = parseRoomToken(part);
      String extractedRoom = roomToken != null ? roomToken.full : "";
      Integer buildingNumber = roomToken != null ? roomToken.buildingNumber : extractBuildingNumber(part);
      if (!StringUtils.hasText(normalized)) {
        continue;
      }
      event.setNormalizedLocation(normalized);

      Salle salle = null;
      if (StringUtils.hasText(extractedRoom)) {
        salle = salleMap.get(extractedRoom);
        event.setNormalizedLocation(extractedRoom);
        if (event.getRoomId() == null) {
          event.setRoomId(extractedRoom);
        }
      }
      if (salle == null) {
        salle = salleMap.get(normalized);
      }
      if (salle == null && StringUtils.hasText(altNormalized)) {
        salle = salleMap.get(altNormalized);
        if (salle != null) {
          event.setNormalizedLocation(altNormalized);
        }
      }
      if (salle != null) {
        event.setRoomId(salle.getNumS());
        if (salle.getBatiment() != null && salle.getBatiment().getCampus() != null) {
          event.setCampusName(salle.getBatiment().getCampus().getNomC());
        }
        if (buildingNumber == null) {
          buildingNumber = extractBuildingNumber(salle.getNumS());
        }
      }

      if (buildingNumber == null) {
        buildingNumber = extractBuildingNumber(normalized);
      }

      if (buildingNumber != null) {
        String campusHint = event.getCampusName() != null ? event.getCampusName() : preferredCampus;
        OsmBuilding osm = findOsmBuilding(campusHint, buildingNumber, osmIndex);
        if (osm != null) {
          applyOsmBuilding(event, osm, buildingNumber);
          if (salle == null) {
            event.addWarning("Salle non trouvée, bâtiment OSM estimé via le numéro.");
          }
          return;
        }
      }
    }

    event.addWarning("Lieu non reconnu: " + raw);
  }

  private void applyOsmBuilding(IcsEvent event, OsmBuilding building, Integer buildingNumber) {
    String label = osmBuildingService.displayName(building);
    if (buildingNumber != null && osmBuildingService.isGenericLabel(label)) {
      label = "Bâtiment " + buildingNumber;
    }
    event.setBuildingCode(label);
    if (building.getCampus() != null) {
      event.setCampusName(building.getCampus());
    }
    event.setLatitude(building.getLatitude());
    event.setLongitude(building.getLongitude());
    if (building.getLatitude() == null || building.getLongitude() == null) {
      event.addWarning("Coordonnées manquantes pour le bâtiment OSM");
    }
  }

  private String normalizeLocation(String raw) {
    if (raw == null) {
      return "";
    }
    String normalized = raw.toUpperCase(Locale.ROOT);
    normalized = normalized.replaceAll("\\s+", "");
    normalized = normalized.replace("TDINFO", "TD");
    return normalized;
  }

  private String normalizePreserveCase(String raw) {
    if (raw == null) {
      return "";
    }
    return raw.replaceAll("\\s+", "");
  }

  private RoomToken parseRoomToken(String raw) {
    if (raw == null) {
      return null;
    }
    String cleaned = raw.toUpperCase(Locale.ROOT).replace("INFO", "");
    Matcher matcher = ROOM_TOKEN.matcher(cleaned);
    if (!matcher.find()) {
      return null;
    }
    String type = matcher.group(1);
    String building = matcher.group(2);
    String room = matcher.group(3);
    String full = type + building + "." + room;
    Integer buildingNumber = null;
    try {
      buildingNumber = Integer.parseInt(building);
    } catch (NumberFormatException ignored) {
    }
    return new RoomToken(full, buildingNumber);
  }

  private static class RoomToken {
    private final String full;
    private final Integer buildingNumber;

    private RoomToken(String full, Integer buildingNumber) {
      this.full = full;
      this.buildingNumber = buildingNumber;
    }
  }

  private Integer extractBuildingNumber(String normalized) {
    if (normalized == null) {
      return null;
    }
    Matcher matcher = BUILDING_NUMBER.matcher(normalized);
    if (!matcher.find()) {
      return null;
    }
    return Integer.parseInt(matcher.group(1));
  }

  private Map<String, Map<Integer, List<OsmBuilding>>> buildOsmIndex() {
    Map<String, Map<Integer, List<OsmBuilding>>> index = new HashMap<>();
    for (OsmBuilding building : osmBuildingService.findAll()) {
      Integer number = extractOsmNumber(building);
      if (number == null) {
        continue;
      }
      String campus = building.getCampus() != null ? building.getCampus() : "";
      index.computeIfAbsent(campus, k -> new HashMap<>())
          .computeIfAbsent(number, k -> new ArrayList<>())
          .add(building);
    }
    return index;
  }

  private Integer extractOsmNumber(OsmBuilding building) {
    List<String> refCandidates = new ArrayList<>();
    List<String> candidates = new ArrayList<>();
    if (building.getName() != null) {
      candidates.add(building.getName());
    }
    String tags = building.getTags();
    if (tags != null && !tags.isBlank()) {
      try {
        Map<String, Object> map = objectMapper.readValue(tags, new TypeReference<>() {});
        for (String key : List.of("ref", "building:ref")) {
          Object value = map.get(key);
          if (value != null) {
            refCandidates.add(String.valueOf(value));
          }
        }
        for (String key : List.of("short_name", "name")) {
          Object value = map.get(key);
          if (value != null) {
            candidates.add(String.valueOf(value));
          }
        }
      } catch (Exception ignored) {
      }
    }

    for (String candidate : refCandidates) {
      Integer ref = extractNumericRef(candidate);
      if (ref != null) {
        return ref;
      }
    }

    for (String candidate : candidates) {
      Integer direct = extractNumericRef(candidate);
      if (direct != null) {
        return direct;
      }
      String normalized = normalizeText(candidate);
      Matcher matcher = OSM_NUMBER.matcher(normalized);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    }

    return null;
  }

  private OsmBuilding findOsmBuilding(String campus, int number, Map<String, Map<Integer, List<OsmBuilding>>> index) {
    if (campus != null && index.containsKey(campus)) {
      List<OsmBuilding> list = index.get(campus).get(number);
      if (list != null && !list.isEmpty()) {
        return chooseBestOsm(list, number);
      }
    }
    for (Map<Integer, List<OsmBuilding>> byNumber : index.values()) {
      List<OsmBuilding> list = byNumber.get(number);
      if (list != null && !list.isEmpty()) {
        return chooseBestOsm(list, number);
      }
    }
    return null;
  }

  private OsmBuilding chooseBestOsm(List<OsmBuilding> list, int number) {
    if (list.size() == 1) {
      return list.get(0);
    }
    for (OsmBuilding b : list) {
      String normalized = normalizeText(b.getName() != null ? b.getName() : "");
      if (normalized.contains("batiment") && normalized.contains(String.valueOf(number))) {
        return b;
      }
      if (normalized.contains("pavillon") && normalized.contains(String.valueOf(number))) {
        return b;
      }
    }
    return list.get(0);
  }

  private String normalizeText(String input) {
    if (input == null) {
      return "";
    }
    String n = Normalizer.normalize(input, Normalizer.Form.NFD);
    n = n.replaceAll("\\p{M}", "");
    return n.toLowerCase(Locale.ROOT);
  }

  private Integer extractNumericRef(String candidate) {
    if (candidate == null) {
      return null;
    }
    String normalized = normalizeText(candidate);
    String compact = normalized.replaceAll("[^a-z0-9]", "");
    Matcher compactMatcher = Pattern.compile("^[a-z]*0*(\\d{1,3})$").matcher(compact);
    if (compactMatcher.matches()) {
      try {
        return Integer.parseInt(compactMatcher.group(1));
      } catch (NumberFormatException ignored) {
      }
    }
    String spaced = normalized.replaceAll("[^a-z0-9]", " ").trim();
    Matcher matcher = ONLY_NUMBER.matcher(spaced);
    if (matcher.find()) {
      try {
        return Integer.parseInt(matcher.group(1));
      } catch (NumberFormatException ignored) {
      }
    }
    return null;
  }

  private Map<String, Salle> buildSalleMap() {
    Map<String, Salle> map = new HashMap<>();
    for (Salle salle : salleService.findAll()) {
      if (salle.getNumS() == null) {
        continue;
      }
      String normalized = normalizeLocation(salle.getNumS());
      if (StringUtils.hasText(normalized)) {
        map.putIfAbsent(normalized, salle);
      }
      String alt = normalizePreserveCase(salle.getNumS());
      if (StringUtils.hasText(alt)) {
        map.putIfAbsent(alt, salle);
      }
    }
    return map;
  }

  private String buildLabel(IcsEvent event) {
    StringBuilder sb = new StringBuilder();
    if (StringUtils.hasText(event.getSummary())) {
      sb.append(event.getSummary());
    }
    if (StringUtils.hasText(event.getRoomId())) {
      sb.append(" (").append(event.getRoomId()).append(")");
    } else if (StringUtils.hasText(event.getBuildingCode())) {
      sb.append(" (").append(event.getBuildingCode()).append(")");
    }
    return sb.toString();
  }
}
