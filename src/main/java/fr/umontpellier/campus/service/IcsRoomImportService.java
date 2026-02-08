package fr.umontpellier.campus.service;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.domain.OsmBuilding;
import fr.umontpellier.campus.domain.Salle;
import fr.umontpellier.campus.domain.TypeSalle;
import fr.umontpellier.campus.dto.IcsEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class IcsRoomImportService {
  private static final Pattern ROOM_TOKEN = Pattern.compile("([A-Z]{1,5})\\s*(\\d{1,3})\\s*\\.\\s*(\\d{1,3})");

  private final SalleService salleService;
  private final BatimentService batimentService;
  private final CampusService campusService;
  private final OsmBuildingService osmBuildingService;

  public IcsRoomImportService(SalleService salleService,
      BatimentService batimentService,
      CampusService campusService,
      OsmBuildingService osmBuildingService) {
    this.salleService = salleService;
    this.batimentService = batimentService;
    this.campusService = campusService;
    this.osmBuildingService = osmBuildingService;
  }

  @Transactional
  public void importRooms(List<IcsEvent> events) {
    if (events == null || events.isEmpty()) {
      return;
    }

    Map<String, Map<Integer, Batiment>> batimentIndex = buildBatimentIndex();
    Map<String, Map<Integer, List<OsmBuilding>>> osmIndex = buildOsmIndex();
    String preferredCampus = inferPreferredCampus(events, batimentIndex, osmIndex);
    List<String> campusFallback = campusService.findAll().stream()
        .map(Campus::getNomC)
        .toList();

    for (IcsEvent event : events) {
      String raw = event.getRawLocation();
      if (!StringUtils.hasText(raw)) {
        continue;
      }
      String cleaned = raw.replace("\\,", ",");
      String[] parts = cleaned.split(",");
      for (String part : parts) {
        RoomToken token = parseRoomToken(part);
        if (token == null) {
          continue;
        }
        String roomId = token.type + token.buildingStr + "." + token.roomStr;
        if (salleService.findById(roomId).isPresent()) {
          continue;
        }

        String campus = resolveCampus(token.buildingNumber, preferredCampus, batimentIndex, osmIndex, campusFallback);
        Batiment batiment = resolveBatiment(campus, token.buildingNumber, batimentIndex, osmIndex);
        if (batiment == null) {
          continue;
        }

        TypeSalle typeSalle = mapType(token.type);
        Salle salle = new Salle();
        salle.setNumS(roomId);
        salle.setTypeS(typeSalle);
        salle.setBatiment(batiment);
        salleService.save(salle);
      }
    }
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
    String buildingStr = matcher.group(2);
    String roomStr = matcher.group(3);
    Integer buildingNumber;
    try {
      buildingNumber = Integer.parseInt(buildingStr);
    } catch (NumberFormatException ex) {
      return null;
    }
    return new RoomToken(type, buildingStr, roomStr, buildingNumber);
  }

  private TypeSalle mapType(String prefix) {
    if (!StringUtils.hasText(prefix)) {
      return null;
    }
    String upper = prefix.toUpperCase(Locale.ROOT);
    if (upper.equals("A") || upper.startsWith("AMP")) {
      return TypeSalle.AMPHI;
    }
    if (upper.startsWith("TD")) {
      return TypeSalle.TD;
    }
    if (upper.startsWith("TP")) {
      return TypeSalle.TP;
    }
    if (upper.startsWith("SC")) {
      return TypeSalle.SC;
    }
    return null;
  }

  private Batiment resolveBatiment(String campus, Integer number,
      Map<String, Map<Integer, Batiment>> batimentIndex,
      Map<String, Map<Integer, List<OsmBuilding>>> osmIndex) {
    if (number == null || !StringUtils.hasText(campus)) {
      return null;
    }
    Batiment existing = findBatiment(campus, number, batimentIndex);
    if (existing != null) {
      return existing;
    }

    OsmBuilding osm = findOsmBuilding(campus, number, osmIndex);
    if (osm != null) {
      Batiment created = createBatimentFromOsm(campus, number, osm);
      batimentIndex.computeIfAbsent(campus, k -> new HashMap<>()).put(number, created);
      return created;
    }

    Batiment placeholder = createPlaceholderBatiment(campus, number);
    batimentIndex.computeIfAbsent(campus, k -> new HashMap<>()).put(number, placeholder);
    return placeholder;
  }

  private Batiment createBatimentFromOsm(String campusName, Integer number, OsmBuilding osm) {
    Campus campus = campusService.findById(campusName)
        .orElseGet(() -> campusService.save(new Campus(campusName, campusName)));
    String codeB = formatCodeB(campusName, number, osm.getOsmId());
    Batiment target = batimentService.findById(codeB).orElse(null);
    if (target == null) {
      target = new Batiment();
      target.setCodeB(codeB);
    }
    target.setCampus(campus);
    target.setBuildingNumber(number);
    target.setOsmId(osm.getOsmId());
    target.setName(osmBuildingService.displayName(osm));
    target.setLatitude(osm.getLatitude());
    target.setLongitude(osm.getLongitude());
    return batimentService.save(target);
  }

  private Batiment createPlaceholderBatiment(String campusName, Integer number) {
    Campus campus = campusService.findById(campusName)
        .orElseGet(() -> campusService.save(new Campus(campusName, campusName)));
    String codeB = formatCodeB(campusName, number, null);
    Batiment target = batimentService.findById(codeB).orElse(null);
    if (target == null) {
      target = new Batiment();
      target.setCodeB(codeB);
    }
    target.setCampus(campus);
    target.setBuildingNumber(number);
    target.setName("Bâtiment " + number);
    return batimentService.save(target);
  }

  private Map<String, Map<Integer, Batiment>> buildBatimentIndex() {
    Map<String, Map<Integer, Batiment>> index = new HashMap<>();
    for (Batiment b : batimentService.findAll()) {
      if (b.getBuildingNumber() == null) {
        continue;
      }
      String campus = b.getCampus() != null ? b.getCampus().getNomC() : "";
      index.computeIfAbsent(campus, k -> new HashMap<>()).putIfAbsent(b.getBuildingNumber(), b);
    }
    return index;
  }

  private Map<String, Map<Integer, List<OsmBuilding>>> buildOsmIndex() {
    Map<String, Map<Integer, List<OsmBuilding>>> index = new HashMap<>();
    for (OsmBuilding building : osmBuildingService.findAll()) {
      Integer number = osmBuildingService.extractBuildingNumber(building);
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

  private String inferPreferredCampus(List<IcsEvent> events,
      Map<String, Map<Integer, Batiment>> batimentIndex,
      Map<String, Map<Integer, List<OsmBuilding>>> osmIndex) {
    Map<String, Integer> counts = new HashMap<>();
    for (IcsEvent event : events) {
      String raw = event.getRawLocation();
      if (!StringUtils.hasText(raw)) {
        continue;
      }
      String cleaned = raw.replace("\\,", ",");
      String[] parts = cleaned.split(",");
      for (String part : parts) {
        RoomToken token = parseRoomToken(part);
        if (token == null) {
          continue;
        }
        Integer number = token.buildingNumber;
        for (Map.Entry<String, Map<Integer, Batiment>> entry : batimentIndex.entrySet()) {
          if (entry.getValue().containsKey(number)) {
            counts.merge(entry.getKey(), 1, Integer::sum);
          }
        }
        for (Map.Entry<String, Map<Integer, List<OsmBuilding>>> entry : osmIndex.entrySet()) {
          if (entry.getValue().containsKey(number)) {
            counts.merge(entry.getKey(), 1, Integer::sum);
          }
        }
      }
    }
    if (counts.isEmpty()) {
      return null;
    }
    return counts.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);
  }

  private String resolveCampus(Integer number, String preferredCampus,
      Map<String, Map<Integer, Batiment>> batimentIndex,
      Map<String, Map<Integer, List<OsmBuilding>>> osmIndex,
      List<String> campusFallback) {
    if (number == null) {
      return preferredCampus != null ? preferredCampus : firstCampus(campusFallback);
    }
    if (preferredCampus != null && hasNumber(preferredCampus, number, batimentIndex, osmIndex)) {
      return preferredCampus;
    }
    Set<String> matches = new HashSet<>();
    for (Map.Entry<String, Map<Integer, Batiment>> entry : batimentIndex.entrySet()) {
      if (entry.getValue().containsKey(number)) {
        matches.add(entry.getKey());
      }
    }
    for (Map.Entry<String, Map<Integer, List<OsmBuilding>>> entry : osmIndex.entrySet()) {
      if (entry.getValue().containsKey(number)) {
        matches.add(entry.getKey());
      }
    }
    if (matches.size() == 1) {
      return matches.iterator().next();
    }
    return preferredCampus != null ? preferredCampus : firstCampus(campusFallback);
  }

  private boolean hasNumber(String campus, Integer number,
      Map<String, Map<Integer, Batiment>> batimentIndex,
      Map<String, Map<Integer, List<OsmBuilding>>> osmIndex) {
    if (campus == null || number == null) {
      return false;
    }
    if (batimentIndex.containsKey(campus) && batimentIndex.get(campus).containsKey(number)) {
      return true;
    }
    return osmIndex.containsKey(campus) && osmIndex.get(campus).containsKey(number);
  }

  private Batiment findBatiment(String campus, Integer number, Map<String, Map<Integer, Batiment>> index) {
    if (campus == null || number == null) {
      return null;
    }
    Map<Integer, Batiment> byNumber = index.get(campus);
    if (byNumber != null) {
      return byNumber.get(number);
    }
    return null;
  }

  private OsmBuilding findOsmBuilding(String campus, Integer number,
      Map<String, Map<Integer, List<OsmBuilding>>> index) {
    if (campus != null && index.containsKey(campus)) {
      List<OsmBuilding> list = index.get(campus).get(number);
      if (list != null && !list.isEmpty()) {
        return chooseBestOsm(list);
      }
    }
    for (Map<Integer, List<OsmBuilding>> byNumber : index.values()) {
      List<OsmBuilding> list = byNumber.get(number);
      if (list != null && !list.isEmpty()) {
        return chooseBestOsm(list);
      }
    }
    return null;
  }

  private OsmBuilding chooseBestOsm(List<OsmBuilding> list) {
    if (list.size() == 1) {
      return list.get(0);
    }
    OsmBuilding best = list.get(0);
    int bestScore = osmBuildingService.scoreForMap(best);
    for (OsmBuilding candidate : list) {
      int score = osmBuildingService.scoreForMap(candidate);
      if (score > bestScore) {
        best = candidate;
        bestScore = score;
      }
    }
    return best;
  }

  private String formatCodeB(String campusName, Integer number, Long osmId) {
    String campusCode = campusCode(campusName);
    if (number != null) {
      String formatted = number < 10 ? "0" + number : String.valueOf(number);
      return campusCode + "_B" + formatted;
    }
    if (osmId != null) {
      return campusCode + "_OSM" + osmId;
    }
    return campusCode + "_OSM";
  }

  private String campusCode(String campus) {
    if (campus == null) {
      return "CMP";
    }
    return switch (campus) {
      case "Triolet" -> "TRI";
      case "Richter" -> "RIC";
      case "Pharmacie" -> "PHA";
      case "St Priest" -> "STP";
      case "FDE Mende" -> "FDE";
      case "Medecine Nimes" -> "MED";
      default -> campus.substring(0, Math.min(3, campus.length())).toUpperCase();
    };
  }

  private String firstCampus(List<String> list) {
    return list == null || list.isEmpty() ? null : list.get(0);
  }

  private static class RoomToken {
    private final String type;
    private final String buildingStr;
    private final String roomStr;
    private final Integer buildingNumber;

    private RoomToken(String type, String buildingStr, String roomStr, Integer buildingNumber) {
      this.type = type;
      this.buildingStr = buildingStr;
      this.roomStr = roomStr;
      this.buildingNumber = buildingNumber;
    }
  }
}
