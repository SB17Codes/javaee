package fr.umontpellier.campus.controller;

import fr.umontpellier.campus.dto.IcsEvent;
import fr.umontpellier.campus.dto.ItineraryResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.umontpellier.campus.service.IcsParserService;
import fr.umontpellier.campus.service.IcsRoomImportService;
import fr.umontpellier.campus.service.ItineraryService;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/itinerary")
public class ItineraryController {
  private final IcsParserService icsParserService;
  private final ItineraryService itineraryService;
  private final IcsRoomImportService icsRoomImportService;
  private final ObjectMapper objectMapper;

  @Value("${app.mapbox.token:}")
  private String mapboxToken;

  public ItineraryController(IcsParserService icsParserService, ItineraryService itineraryService,
      IcsRoomImportService icsRoomImportService, ObjectMapper objectMapper) {
    this.icsParserService = icsParserService;
    this.itineraryService = itineraryService;
    this.icsRoomImportService = icsRoomImportService;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public String itinerary(Model model, HttpSession session) {
    populateCommon(model, session);
    return "itinerary";
  }

  @PostMapping("/upload")
  public String upload(@RequestParam("file") MultipartFile file, Model model, HttpSession session) {
    if (file == null || file.isEmpty()) {
      model.addAttribute("error", "Veuillez sélectionner un fichier .ics.");
      populateCommon(model, session);
      return "itinerary";
    }

    List<IcsEvent> events;
    try {
      events = icsParserService.parse(file.getInputStream());
    } catch (IOException e) {
      model.addAttribute("error", "Impossible de lire le fichier.");
      populateCommon(model, session);
      return "itinerary";
    }
    if (events.isEmpty()) {
      model.addAttribute("error", "Aucun événement détecté dans ce fichier.");
      populateCommon(model, session);
      return "itinerary";
    }

    icsRoomImportService.importRooms(events);
    session.setAttribute("icsEvents", events);
    populateCommon(model, session);
    model.addAttribute("uploadedCount", events.size());
    return "itinerary";
  }

  @PostMapping("/plan")
  public String plan(@RequestParam("date") String date, Model model, HttpSession session) {
    List<IcsEvent> events = getSessionEvents(session);
    if (events == null || events.isEmpty()) {
      model.addAttribute("error", "Veuillez d'abord importer un fichier .ics.");
      populateCommon(model, session);
      return "itinerary";
    }

    LocalDate selected;
    try {
      selected = LocalDate.parse(date);
    } catch (Exception e) {
      model.addAttribute("error", "Date invalide.");
      populateCommon(model, session);
      return "itinerary";
    }

    ItineraryResult result = itineraryService.buildItinerary(events, selected);
    populateCommon(model, session);
    model.addAttribute("selectedDate", selected);
    model.addAttribute("result", result);
    model.addAttribute("waypointsJson", toJson(result.getWaypoints()));
    if (result.getEvents().isEmpty()) {
      model.addAttribute("info", "Aucun cours pour cette date.");
    }
    return "itinerary";
  }

  private void populateCommon(Model model, HttpSession session) {
    model.addAttribute("mapboxToken", mapboxToken == null ? "" : mapboxToken);
    model.addAttribute("waypointsJson", "[]");
    List<IcsEvent> events = getSessionEvents(session);
    if (events != null && !events.isEmpty()) {
      model.addAttribute("availableDates", itineraryService.availableDates(events));
    }
  }

  private String toJson(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      return "[]";
    }
  }

  @SuppressWarnings("unchecked")
  private List<IcsEvent> getSessionEvents(HttpSession session) {
    Object stored = session.getAttribute("icsEvents");
    if (stored instanceof List) {
      return (List<IcsEvent>) stored;
    }
    return null;
  }
}
