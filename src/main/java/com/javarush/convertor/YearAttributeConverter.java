package com.javarush.convertor;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Year;

@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Short> {
    @Override
    public Short convertToDatabaseColumn(Year year) {
        if (year == null) return null;
        else {
            return (short) year.getValue();
        }
    }

    @Override
    public Year convertToEntityAttribute(Short aShort) {
        if (aShort == null) return null;
        else {
            return Year.of(aShort);
        }
    }
}
