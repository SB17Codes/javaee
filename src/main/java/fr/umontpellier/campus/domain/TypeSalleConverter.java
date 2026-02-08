package fr.umontpellier.campus.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TypeSalleConverter implements AttributeConverter<TypeSalle, String> {
  @Override
  public String convertToDatabaseColumn(TypeSalle attribute) {
    return attribute == null ? null : attribute.getDbValue();
  }

  @Override
  public TypeSalle convertToEntityAttribute(String dbData) {
    return TypeSalle.fromDbValue(dbData);
  }
}
