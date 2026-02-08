package fr.umontpellier.campus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import fr.umontpellier.campus.domain.OsmBuilding;
import fr.umontpellier.campus.repository.OsmBuildingRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;

@Service
public class OsmBuildingService {
  private final OsmBuildingRepository osmBuildingRepository;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final Map<Long, String> nameCache = new ConcurrentHashMap<>();
  private final Map<Long, Map<String, Object>> tagCache = new ConcurrentHashMap<>();
  private final Map<String, String> campusAnchorCache = new ConcurrentHashMap<>();

  @Value("${app.mapbox.token:}")
  private String mapboxToken;

  public OsmBuildingService(OsmBuildingRepository osmBuildingRepository, ObjectMapper objectMapper) {
    this.osmBuildingRepository = osmBuildingRepository;
    this.objectMapper = objectMapper;
  }

  public List<OsmBuilding> findAll() {
    return osmBuildingRepository.findAll(Sort.by("name"));
  }

  public List<OsmBuilding> findByCampus(String campus) {
    return osmBuildingRepository.findByCampusOrderByNameAsc(campus);
  }

  public String displayName(OsmBuilding building) {
    return displayNameInternal(building, true);
  }

  public String displayNameForMap(OsmBuilding building) {
    return displayNameInternal(building, false);
  }

  public boolean isGenericLabel(String label) {
    if (!StringUtils.hasText(label)) {
      return true;
    }
    String normalized = normalizeLabel(label);
    return normalized.equals("batiment")
        || normalized.equals("batimentsansnom")
        || normalized.startsWith("batimentosm");
  }

  public String normalizeLabel(String label) {
    if (!StringUtils.hasText(label)) {
      return "";
    }
    String n = Normalizer.normalize(label, Normalizer.Form.NFD);
    n = n.replaceAll("\\p{M}", "");
    n = n.toLowerCase(Locale.ROOT);
    n = n.replaceAll("[^a-z0-9]+", "");
    return n;
  }

  public int scoreForMap(OsmBuilding building) {
    int score = 0;
    if (building == null) {
      return score;
    }
    String name = sanitizeName(building.getName());
    if (StringUtils.hasText(name)) {
      score += 5;
      if (name.length() > 6) {
        score += 2;
      }
    }
    Map<String, Object> tags = parseTags(building);
    if (hasAnyKey(tags, List.of("name", "name:fr", "official_name", "short_name", "alt_name", "loc_name"))) {
      score += 4;
    }
    if (hasAnyKey(tags, List.of("ref", "building:ref", "addr:housename"))) {
      score += 3;
    }
    if (building.getLatitude() != null && building.getLongitude() != null) {
      score += 1;
    }
    return score;
  }

  private String displayNameInternal(OsmBuilding building, boolean allowGeocode) {
    if (building == null) {
      return "Bâtiment";
    }
    if (building.getOsmId() != null && nameCache.containsKey(building.getOsmId())) {
      return nameCache.get(building.getOsmId());
    }

    Map<String, Object> tags = parseTags(building);
    String name = sanitizeName(building.getName());
    if (isShortCode(name)) {
      String expanded = expandShortCode(name, tags);
      if (StringUtils.hasText(expanded)) {
        return cacheName(building, expanded);
      }
    }
    if (StringUtils.hasText(name)) {
      return cacheName(building, name);
    }

    String tagName = bestTagName(tags);
    if (StringUtils.hasText(tagName)) {
      return cacheName(building, tagName);
    }

    String refLabel = labelFromRef(tags);
    if (StringUtils.hasText(refLabel)) {
      return cacheName(building, refLabel);
    }

    String anchor = campusAnchorName(building.getCampus());
    if (StringUtils.hasText(anchor)) {
      Integer ref = extractNumericRef(tags);
      String label = ref != null ? anchor + " - Bâtiment " + ref : anchor;
      return cacheName(building, label);
    }

    if (allowGeocode) {
      String resolved = reverseGeocodeName(building.getLatitude(), building.getLongitude());
      if (StringUtils.hasText(resolved)) {
        return cacheName(building, resolved);
      }
    }

    if (building.getOsmId() != null) {
      String label = "Bâtiment sans nom";
      return cacheName(building, label);
    }
    return "Bâtiment";
  }

  private String sanitizeName(String raw) {
    if (!StringUtils.hasText(raw)) {
      return "";
    }
    String trimmed = raw.trim();
    if (trimmed.equalsIgnoreCase("university")) {
      return "";
    }
    return trimmed;
  }

  private Map<String, Object> parseTags(OsmBuilding building) {
    if (building == null || !StringUtils.hasText(building.getTags())) {
      return Map.of();
    }
    if (building.getOsmId() != null && tagCache.containsKey(building.getOsmId())) {
      return tagCache.get(building.getOsmId());
    }
    try {
      Map<String, Object> map = objectMapper.readValue(building.getTags(), new TypeReference<>() {});
      if (building.getOsmId() != null) {
        tagCache.putIfAbsent(building.getOsmId(), map);
      }
      return map;
    } catch (Exception ignored) {
      return Map.of();
    }
  }

  private String bestTagName(Map<String, Object> tags) {
    if (tags == null || tags.isEmpty()) {
      return "";
    }
    List<String> keys = List.of(
        "name:fr", "name", "official_name", "short_name", "alt_name", "loc_name",
        "addr:housename");
    for (String key : keys) {
      String value = sanitizeName(Objects.toString(tags.get(key), ""));
      if (StringUtils.hasText(value)) {
        if (isShortCode(value)) {
          String expanded = expandShortCode(value, tags);
          if (StringUtils.hasText(expanded)) {
            return expanded;
          }
        }
        return value;
      }
    }
    return "";
  }

  private boolean isShortCode(String name) {
    if (!StringUtils.hasText(name)) {
      return false;
    }
    String trimmed = name.trim();
    return trimmed.length() <= 2 && trimmed.matches("^[A-Za-z0-9]+$");
  }

  private String expandShortCode(String name, Map<String, Object> tags) {
    if (!StringUtils.hasText(name)) {
      return "";
    }
    String alt = sanitizeName(Objects.toString(tags.get("alt_name"), ""));
    if (StringUtils.hasText(alt) && !isShortCode(alt)) {
      return alt;
    }
    String house = sanitizeName(Objects.toString(tags.get("addr:housename"), ""));
    if (StringUtils.hasText(house) && !isShortCode(house)) {
      return house;
    }
    return "Bâtiment " + name.trim().toUpperCase(Locale.ROOT);
  }

  private String labelFromRef(Map<String, Object> tags) {
    Integer ref = extractNumericRef(tags);
    if (ref != null) {
      return "Bâtiment " + ref;
    }
    String refText = Objects.toString(tags.get("ref"), "").trim();
    if (StringUtils.hasText(refText) && !isShortCode(refText)) {
      return "Bâtiment " + refText;
    }
    refText = Objects.toString(tags.get("building:ref"), "").trim();
    if (StringUtils.hasText(refText) && !isShortCode(refText)) {
      return "Bâtiment " + refText;
    }
    return "";
  }

  private Integer extractNumericRef(Map<String, Object> tags) {
    if (tags == null || tags.isEmpty()) {
      return null;
    }
    String ref = Objects.toString(tags.get("building:ref"), "");
    Integer numeric = parseRefNumber(ref);
    if (numeric != null) {
      return numeric;
    }
    ref = Objects.toString(tags.get("ref"), "");
    numeric = parseRefNumber(ref);
    if (numeric != null) {
      return numeric;
    }
    return null;
  }

  private Integer parseRefNumber(String ref) {
    if (!StringUtils.hasText(ref)) {
      return null;
    }
    String digits = ref.replaceAll("[^0-9]", "");
    if (digits.isEmpty()) {
      return null;
    }
    try {
      return Integer.parseInt(digits);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private boolean hasAnyKey(Map<String, Object> tags, List<String> keys) {
    if (tags == null || tags.isEmpty()) {
      return false;
    }
    for (String key : keys) {
      Object value = tags.get(key);
      if (value != null && StringUtils.hasText(value.toString())) {
        return true;
      }
    }
    return false;
  }

  private String campusAnchorName(String campus) {
    if (!StringUtils.hasText(campus)) {
      return "";
    }
    if (campusAnchorCache.containsKey(campus)) {
      return campusAnchorCache.get(campus);
    }
    List<OsmBuilding> items = findByCampus(campus);
    String best = "";
    int bestScore = -1;
    for (OsmBuilding building : items) {
      Map<String, Object> tags = parseTags(building);
      String candidate = sanitizeName(building.getName());
      if (!StringUtils.hasText(candidate)) {
        candidate = bestTagName(tags);
      }
      if (!StringUtils.hasText(candidate)) {
        continue;
      }
      String normalized = normalizeLabel(candidate);
      if (normalized.equals("universite") || normalized.equals("campus")) {
        continue;
      }
      int score = 0;
      String lowered = candidate.toLowerCase(Locale.ROOT);
      if (lowered.contains("universit") || lowered.contains("facult") || lowered.contains("campus")
          || lowered.contains("ufr") || lowered.contains("iut") || lowered.contains("ecole")) {
        score += 10;
      }
      String amenity = Objects.toString(tags.get("amenity"), "").toLowerCase(Locale.ROOT);
      String buildingTag = Objects.toString(tags.get("building"), "").toLowerCase(Locale.ROOT);
      if (amenity.contains("university") || buildingTag.contains("university")) {
        score += 5;
      }
      score += Math.min(candidate.length(), 30) / 6;
      if (score > bestScore) {
        bestScore = score;
        best = candidate;
      }
    }
    if (!StringUtils.hasText(best)) {
      best = "Campus " + campus;
    }
    campusAnchorCache.putIfAbsent(campus, best);
    return best;
  }

  private String cacheName(OsmBuilding building, String name) {
    if (building != null && building.getOsmId() != null) {
      nameCache.putIfAbsent(building.getOsmId(), name);
    }
    return name;
  }

  private String reverseGeocodeName(Double latitude, Double longitude) {
    if (latitude == null || longitude == null) {
      return "";
    }
    if (!StringUtils.hasText(mapboxToken)) {
      return "";
    }
    try {
      String coord = longitude + "," + latitude;
      String types = URLEncoder.encode("poi,poi.landmark", StandardCharsets.UTF_8);
      String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + coord
          + ".json?types=" + types + "&limit=5&language=fr&access_token="
          + URLEncoder.encode(mapboxToken, StandardCharsets.UTF_8);
      HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
      HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
      if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
        return "";
      }
      Map<String, Object> body = objectMapper.readValue(resp.body(), new TypeReference<>() {});
      Object featuresObj = body.get("features");
      if (!(featuresObj instanceof List<?> features) || features.isEmpty()) {
        return "";
      }
      for (Object candidate : features) {
        if (!(candidate instanceof Map<?, ?> feature)) {
          continue;
        }
        Object propsObj = feature.get("properties");
        String category = "";
        if (propsObj instanceof Map<?, ?> props) {
          category = Objects.toString(props.get("category"), "").toLowerCase(Locale.ROOT);
        }
        if (!category.isBlank() && (category.contains("university") || category.contains("college")
            || category.contains("school"))) {
          String text = Objects.toString(feature.get("text"), "").trim();
          if (StringUtils.hasText(text)) {
            return text;
          }
        }
      }
      Object first = features.get(0);
      if (first instanceof Map<?, ?> feature) {
        String text = Objects.toString(feature.get("text"), "").trim();
        if (StringUtils.hasText(text)) {
          return text;
        }
        String placeName = Objects.toString(feature.get("place_name"), "").trim();
        if (StringUtils.hasText(placeName)) {
          int comma = placeName.indexOf(',');
          return comma > 0 ? placeName.substring(0, comma).trim() : placeName;
        }
      }
    } catch (Exception ignored) {
    }
    return "";
  }
}
