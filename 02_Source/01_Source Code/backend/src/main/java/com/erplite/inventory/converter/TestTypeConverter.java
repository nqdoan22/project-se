package com.erplite.inventory.converter;

import com.erplite.inventory.entity.QCTest.TestType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TestTypeConverter implements AttributeConverter<TestType, String> {

    @Override
    public String convertToDatabaseColumn(TestType attribute) {
        if (attribute == null) return null;
        return attribute.getDbValue();
    }

    @Override
    public TestType convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (TestType t : TestType.values()) {
            if (t.getDbValue().equals(dbData)) return t;
        }
        throw new IllegalArgumentException("Unknown DB value for TestType: " + dbData);
    }
}
