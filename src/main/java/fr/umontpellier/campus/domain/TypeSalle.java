package fr.umontpellier.campus.domain;

import java.util.Arrays;

public enum TypeSalle {
  AMPHI("amphi"),
  SC("sc"),
  TD("td"),
  TP("tp"),
  NUMERIQUE("numerique");

  private final String dbValue;

  TypeSalle(String dbValue) {
    this.dbValue = dbValue;
  }

  public String getDbValue() {
    return dbValue;
  }

  public static TypeSalle fromDbValue(String value) {
    if (value == null) {
      return null;
    }
    return Arrays.stream(values())
        .filter(v -> v.dbValue.equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown typeS: " + value));
  }
}
