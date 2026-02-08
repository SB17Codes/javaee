package fr.umontpellier.campus.service;

import fr.umontpellier.campus.dto.IcsEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class IcsParserService {
  private static final ZoneId DEFAULT_ZONE = ZoneId.of("Europe/Paris");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
  private static final DateTimeFormatter TIME_FORMAT = new DateTimeFormatterBuilder()
      .appendValue(ChronoField.HOUR_OF_DAY, 2)
      .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
      .optionalStart()
      .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
      .optionalEnd()
      .toFormatter(Locale.ROOT);

  public List<IcsEvent> parse(InputStream input) throws IOException {
    List<String> unfolded = unfold(input);
    List<IcsEvent> events = new ArrayList<>();

    boolean inEvent = false;
    Map<String, String> current = new HashMap<>();
    Map<String, String> tzids = new HashMap<>();

    for (String line : unfolded) {
      if ("BEGIN:VEVENT".equals(line)) {
        inEvent = true;
        current.clear();
        tzids.clear();
        continue;
      }
      if ("END:VEVENT".equals(line)) {
        if (inEvent) {
          IcsEvent event = buildEvent(current, tzids);
          if (event != null) {
            events.add(event);
          }
        }
        inEvent = false;
        continue;
      }
      if (!inEvent) {
        continue;
      }
      int colon = line.indexOf(':');
      if (colon == -1) {
        continue;
      }
      String nameAndParams = line.substring(0, colon).trim();
      String value = line.substring(colon + 1).trim();
      String name = nameAndParams;
      String tzid = null;

      int semi = nameAndParams.indexOf(';');
      if (semi != -1) {
        name = nameAndParams.substring(0, semi);
        String[] params = nameAndParams.substring(semi + 1).split(";");
        for (String p : params) {
          if (p.startsWith("TZID=")) {
            tzid = p.substring("TZID=".length());
          }
        }
      }
      current.put(name, value);
      if (tzid != null) {
        tzids.put(name, tzid);
      }
    }

    return events;
  }

  private List<String> unfold(InputStream input) throws IOException {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(" ") || line.startsWith("\t")) {
          if (!lines.isEmpty()) {
            String prev = lines.remove(lines.size() - 1);
            lines.add(prev + line.substring(1));
          } else {
            lines.add(line.trim());
          }
        } else {
          lines.add(line.trim());
        }
      }
    }
    return lines;
  }

  private IcsEvent buildEvent(Map<String, String> current, Map<String, String> tzids) {
    String startRaw = current.get("DTSTART");
    if (startRaw == null) {
      return null;
    }

    ZonedDateTime start = parseDateTime(startRaw, tzids.get("DTSTART"));
    ZonedDateTime end = null;
    if (current.get("DTEND") != null) {
      end = parseDateTime(current.get("DTEND"), tzids.get("DTEND"));
    }

    IcsEvent event = new IcsEvent();
    event.setSummary(current.getOrDefault("SUMMARY", ""));
    event.setRawLocation(current.getOrDefault("LOCATION", ""));
    event.setStart(start);
    event.setEnd(end);
    return event;
  }

  private ZonedDateTime parseDateTime(String value, String tzid) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String v = value.trim();
    boolean utc = v.endsWith("Z");
    if (utc) {
      v = v.substring(0, v.length() - 1);
    }

    ZoneId zone = utc ? ZoneId.of("UTC") : (tzid != null ? ZoneId.of(tzid) : DEFAULT_ZONE);

    if (v.length() == 8) {
      LocalDate date = LocalDate.parse(v, DATE_FORMAT);
      return date.atStartOfDay(zone).withZoneSameInstant(DEFAULT_ZONE);
    }

    if (v.contains("T")) {
      String datePart = v.substring(0, 8);
      String timePart = v.substring(9);
      LocalDate date = LocalDate.parse(datePart, DATE_FORMAT);
      LocalTime time = LocalTime.parse(timePart, TIME_FORMAT);
      return ZonedDateTime.of(date, time, zone).withZoneSameInstant(DEFAULT_ZONE);
    }

    LocalDate date = LocalDate.parse(v, DATE_FORMAT);
    return date.atStartOfDay(zone).withZoneSameInstant(DEFAULT_ZONE);
  }
}
