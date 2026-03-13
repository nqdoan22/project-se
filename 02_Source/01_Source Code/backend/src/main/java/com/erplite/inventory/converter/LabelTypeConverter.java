package com.erplite.inventory.converter;

import com.erplite.inventory.entity.LabelTemplate.LabelType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LabelTypeConverter implements AttributeConverter<LabelType, String> {

    @Override
    public String convertToDatabaseColumn(LabelType attribute) {
        if (attribute == null) return null;
        return attribute.getDbValue();
    }

    @Override
    public LabelType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (LabelType t : LabelType.values()) {
            if (t.getDbValue().equals(dbData)) return t;
        }
        throw new IllegalArgumentException("Unknown DB value for LabelType: " + dbData);
    }
}
