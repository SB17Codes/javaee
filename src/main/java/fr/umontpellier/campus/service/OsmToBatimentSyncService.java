package fr.umontpellier.campus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.domain.Campus;
import fr.umontpellier.campus.domain.CampusBoundary;
import fr.umontpellier.campus.domain.OsmBuilding;
import fr.umontpellier.campus.repository.OsmBuildingRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class OsmToBatimentSyncService {
  private final OsmBuildingRepository osmBuildingRepository;
  private final OsmBuildingService osmBuildingService;
  private final BatimentService batimentService;
  private final CampusService campusService;
  private final CampusBoundaryService campusBoundaryService;
  private final ObjectMapper objectMapper;

  public OsmToBatimentSyncService(OsmBuildingRepository osmBuildingRepository,
      OsmBuildingService osmBuildingService,
      BatimentService batimentService,
      CampusService campusService,
      CampusBoundaryService campusBoundaryService,
      ObjectMapper objectMapper) {
    this.osmBuildingRepository = osmBuildingRepository;
    this.osmBuildingService = osmBuildingService;
    this.batimentService = batimentService;
    this.campusService = campusService;
    this.campusBoundaryService = campusBoundaryService;
    this.objectMapper = objectMapper;
  }

  public void sync() {
    Map<String, List<List<List<double[]>>>> campusPolygons = loadCampusPolygons();
    boolean hasBoundaries = !campusPolygons.isEmpty();
    List<OsmBuilding> all = osmBuildingRepository.findAll();
    List<Long> toDelete = new ArrayList<>();
    Map<String, OsmBuilding> bestByKey = new HashMap<>();
    Map<String, Integer> bestScore = new HashMap<>();

    for (OsmBuilding building : all) {
      if (building.getLatitude() == null || building.getLongitude() == null) {
        toDelete.add(building.getOsmId());
        continue;
      }
      boolean inside = true;
      if (hasBoundaries) {
        inside = isInsideCampus(building, campusPolygons);
      }
      if (!inside) {
        toDelete.add(building.getOsmId());
        continue;
      }

      Integer buildingNumber = osmBuildingService.extractBuildingNumber(building);
      String label = osmBuildingService.displayName(building);
      String key;
      if (buildingNumber != null) {
        key = keyForNumber(building.getCampus(), buildingNumber);
      } else {
        String normalized = osmBuildingService.normalizeLabel(label);
        if (!StringUtils.hasText(normalized)) {
          normalized = "osm-" + building.getOsmId();
        }
        key = (building.getCampus() == null ? "" : building.getCampus()) + "|" + normalized;
      }
      int score = osmBuildingService.scoreForMap(building);
      if (!bestByKey.containsKey(key) || score > bestScore.getOrDefault(key, -1)) {
        bestByKey.put(key, building);
        bestScore.put(key, score);
      }
    }

    if (!toDelete.isEmpty()) {
      osmBuildingRepository.deleteAllById(toDelete);
    }

    for (OsmBuilding building : bestByKey.values()) {
      upsertBatiment(building);
    }
  }

  private void upsertBatiment(OsmBuilding building) {
    String campusName = building.getCampus();
    if (!StringUtils.hasText(campusName)) {
      return;
    }
    Campus campus = campusService.findById(campusName)
        .orElseGet(() -> campusService.save(new Campus(campusName, campusName)));

    Integer buildingNumber = osmBuildingService.extractBuildingNumber(building);
    String codeB = formatCodeB(campusName, buildingNumber, building.getOsmId());
    Batiment target = null;

    if (buildingNumber != null) {
      List<Batiment> matches = batimentService.findByCampusAndBuildingNumber(campusName, buildingNumber);
      if (!matches.isEmpty()) {
        target = matches.get(0);
      }
    }
    if (target == null && StringUtils.hasText(codeB)) {
      target = batimentService.findById(codeB).orElse(null);
    }
    if (target == null) {
      target = new Batiment();
      target.setCodeB(codeB);
    }

    target.setCampus(campus);
    target.setBuildingNumber(buildingNumber);
    target.setOsmId(building.getOsmId());
    String name = osmBuildingService.displayName(building);
    if (StringUtils.hasText(name)) {
      target.setName(name);
    }
    if (building.getLatitude() != null) {
      target.setLatitude(building.getLatitude());
    }
    if (building.getLongitude() != null) {
      target.setLongitude(building.getLongitude());
    }
    batimentService.save(target);
  }

  private boolean isInsideCampus(OsmBuilding building, Map<String, List<List<List<double[]>>>> campusPolygons) {
    double lon = building.getLongitude();
    double lat = building.getLatitude();
    List<List<List<double[]>>> campusPolys = campusPolygons.get(building.getCampus());
    if (campusPolys != null && pointInAnyPolygon(lon, lat, campusPolys)) {
      return true;
    }
    return pointInAnyPolygon(lon, lat, flattenAll(campusPolygons));
  }

  private List<List<List<double[]>>> flattenAll(Map<String, List<List<List<double[]>>>> campusPolygons) {
    List<List<List<double[]>>> all = new ArrayList<>();
    for (List<List<List<double[]>>> polys : campusPolygons.values()) {
      all.addAll(polys);
    }
    return all;
  }

  private Map<String, List<List<List<double[]>>>> loadCampusPolygons() {
    Map<String, List<List<List<double[]>>>> byCampus = new HashMap<>();
    List<CampusBoundary> boundaries = campusBoundaryService.findAll();
    for (CampusBoundary boundary : boundaries) {
      if (!StringUtils.hasText(boundary.getGeojson())) {
        continue;
      }
      List<List<List<double[]>>> polygons = parsePolygons(boundary.getGeojson());
      if (!polygons.isEmpty()) {
        byCampus.put(boundary.getCampus(), polygons);
      }
    }
    return byCampus;
  }

  private List<List<List<double[]>>> parsePolygons(String geojson) {
    List<List<List<double[]>>> polygons = new ArrayList<>();
    try {
      Map<String, Object> parsed = objectMapper.readValue(geojson, new TypeReference<>() {});
      Object featuresObj = parsed.get("features");
      if (featuresObj instanceof List<?> features) {
        for (Object featureObj : features) {
          if (!(featureObj instanceof Map<?, ?> feature)) {
            continue;
          }
          Object geomObj = feature.get("geometry");
          if (!(geomObj instanceof Map<?, ?> geom)) {
            continue;
          }
          String type = Objects.toString(geom.get("type"), "");
          Object coordsObj = geom.get("coordinates");
          if ("Polygon".equalsIgnoreCase(type) && coordsObj instanceof List<?> coords) {
            polygons.add(parsePolygon(coords));
          } else if ("MultiPolygon".equalsIgnoreCase(type) && coordsObj instanceof List<?> polys) {
            for (Object polyObj : polys) {
              if (polyObj instanceof List<?> polyCoords) {
                polygons.add(parsePolygon(polyCoords));
              }
            }
          }
        }
      }
    } catch (Exception ignored) {
    }
    return polygons;
  }

  private List<List<double[]>> parsePolygon(List<?> coords) {
    List<List<double[]>> rings = new ArrayList<>();
    for (Object ringObj : coords) {
      if (!(ringObj instanceof List<?> ringCoords)) {
        continue;
      }
      List<double[]> ring = new ArrayList<>();
      for (Object pointObj : ringCoords) {
        if (pointObj instanceof List<?> pair && pair.size() >= 2) {
          double lon = Double.parseDouble(pair.get(0).toString());
          double lat = Double.parseDouble(pair.get(1).toString());
          ring.add(new double[] { lon, lat });
        }
      }
      if (!ring.isEmpty()) {
        rings.add(ring);
      }
    }
    return rings;
  }

  private boolean pointInAnyPolygon(double lon, double lat, List<List<List<double[]>>> polygons) {
    if (polygons == null || polygons.isEmpty()) {
      return false;
    }
    for (List<List<double[]>> poly : polygons) {
      if (pointInPolygon(lon, lat, poly)) {
        return true;
      }
    }
    return false;
  }

  private boolean pointInPolygon(double lon, double lat, List<List<double[]>> polygon) {
    if (polygon == null || polygon.isEmpty()) {
      return false;
    }
    if (!pointInRing(lon, lat, polygon.get(0))) {
      return false;
    }
    for (int i = 1; i < polygon.size(); i++) {
      if (pointInRing(lon, lat, polygon.get(i))) {
        return false;
      }
    }
    return true;
  }

  private boolean pointInRing(double lon, double lat, List<double[]> ring) {
    boolean inside = false;
    for (int i = 0, j = ring.size() - 1; i < ring.size(); j = i++) {
      double xi = ring.get(i)[0];
      double yi = ring.get(i)[1];
      double xj = ring.get(j)[0];
      double yj = ring.get(j)[1];
      boolean intersect = ((yi > lat) != (yj > lat))
          && (lon < (xj - xi) * (lat - yi) / (yj - yi + 0.0) + xi);
      if (intersect) {
        inside = !inside;
      }
    }
    return inside;
  }

  private String keyForNumber(String campus, Integer number) {
    return (campus == null ? "" : campus) + "|num|" + number;
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
}
