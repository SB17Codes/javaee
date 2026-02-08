package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.OsmBuilding;
import fr.umontpellier.campus.service.OsmBuildingService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OsmBuildingApiController {
  private final OsmBuildingService osmBuildingService;

  public OsmBuildingApiController(OsmBuildingService osmBuildingService) {
    this.osmBuildingService = osmBuildingService;
  }

  @GetMapping("/api/osm-buildings/geojson")
  public Map<String, Object> osmBuildingsGeoJson(@RequestParam(required = false) String campus) {
    List<OsmBuilding> items = (campus == null || campus.isBlank())
        ? osmBuildingService.findAll()
        : osmBuildingService.findByCampus(campus);

    Map<String, Map<String, Object>> bestByKey = new HashMap<>();
    Map<String, Integer> bestScore = new HashMap<>();
    for (OsmBuilding b : items) {
      if (b.getLatitude() == null || b.getLongitude() == null) {
        continue;
      }
      String label = osmBuildingService.displayNameForMap(b);
      if (osmBuildingService.isGenericLabel(label)) {
        continue;
      }
      String key = (b.getCampus() == null ? "" : b.getCampus()) + "|"
          + osmBuildingService.normalizeLabel(label);
      int score = osmBuildingService.scoreForMap(b);

      Map<String, Object> feature = new HashMap<>();
      feature.put("type", "Feature");

      Map<String, Object> geometry = new HashMap<>();
      geometry.put("type", "Point");
      geometry.put("coordinates", List.of(b.getLongitude(), b.getLatitude()));
      feature.put("geometry", geometry);

      Map<String, Object> props = new HashMap<>();
      props.put("osmId", b.getOsmId());
      props.put("name", label);
      props.put("rawName", b.getName());
      props.put("campus", b.getCampus());
      feature.put("properties", props);
      if (!bestByKey.containsKey(key) || score > bestScore.getOrDefault(key, -1)) {
        bestByKey.put(key, feature);
        bestScore.put(key, score);
      }
    }

    Map<String, Object> collection = new HashMap<>();
    collection.put("type", "FeatureCollection");
    collection.put("features", new ArrayList<>(bestByKey.values()));
    return collection;
  }
}
