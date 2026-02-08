package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.domain.Batiment;
import fr.umontpellier.campus.service.BatimentService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MapApiController {
  private final BatimentService batimentService;

  public MapApiController(BatimentService batimentService) {
    this.batimentService = batimentService;
  }

  @GetMapping("/api/batiments/geojson")
  public Map<String, Object> batimentsGeoJson() {
    List<Map<String, Object>> features = new ArrayList<>();

    for (Batiment b : batimentService.findAll()) {
      if (b.getLatitude() == null || b.getLongitude() == null) {
        continue;
      }
      Map<String, Object> feature = new HashMap<>();
      feature.put("type", "Feature");

      Map<String, Object> geometry = new HashMap<>();
      geometry.put("type", "Point");
      geometry.put("coordinates", List.of(b.getLongitude(), b.getLatitude()));
      feature.put("geometry", geometry);

      Map<String, Object> props = new HashMap<>();
      props.put("codeB", b.getCodeB());
      props.put("name", b.getName());
      props.put("buildingNumber", b.getBuildingNumber());
      props.put("anneeC", b.getAnneeC());
      props.put("campus", b.getCampus() != null ? b.getCampus().getNomC() : "-");
      feature.put("properties", props);

      features.add(feature);
    }

    Map<String, Object> collection = new HashMap<>();
    collection.put("type", "FeatureCollection");
    collection.put("features", features);
    return collection;
  }
}
