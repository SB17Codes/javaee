package fr.umontpellier.campus.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.umontpellier.campus.domain.CampusBoundary;
import fr.umontpellier.campus.service.CampusBoundaryService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CampusBoundaryApiController {
  private final CampusBoundaryService campusBoundaryService;
  private final ObjectMapper objectMapper;

  public CampusBoundaryApiController(CampusBoundaryService campusBoundaryService, ObjectMapper objectMapper) {
    this.campusBoundaryService = campusBoundaryService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/api/campus-boundaries/geojson")
  public Map<String, Object> boundaries() {
    List<Map<String, Object>> features = new ArrayList<>();
    for (CampusBoundary boundary : campusBoundaryService.findAll()) {
      Map<String, Object> geometry = parseGeometry(boundary.getGeojson());
      if (geometry == null) {
        continue;
      }
      Map<String, Object> feature = new HashMap<>();
      feature.put("type", "Feature");
      feature.put("geometry", geometry);

      Map<String, Object> props = new HashMap<>();
      props.put("campus", boundary.getCampus());
      props.put("source", boundary.getSource());
      feature.put("properties", props);
      features.add(feature);
    }

    Map<String, Object> collection = new HashMap<>();
    collection.put("type", "FeatureCollection");
    collection.put("features", features);
    return collection;
  }

  private Map<String, Object> parseGeometry(String geojson) {
    if (geojson == null || geojson.isBlank()) {
      return null;
    }
    try {
      Map<String, Object> parsed = objectMapper.readValue(geojson, new TypeReference<>() {});
      Object type = parsed.get("type");
      if ("Feature".equals(type)) {
        Object geom = parsed.get("geometry");
        if (geom instanceof Map) {
          return (Map<String, Object>) geom;
        }
      }
      if ("FeatureCollection".equals(type)) {
        Object feats = parsed.get("features");
        if (feats instanceof List<?> list && !list.isEmpty()) {
          Object first = list.get(0);
          if (first instanceof Map<?, ?> map) {
            Object geom = map.get("geometry");
            if (geom instanceof Map) {
              return (Map<String, Object>) geom;
            }
          }
        }
      }
      return parsed;
    } catch (Exception e) {
      return null;
    }
  }
}
