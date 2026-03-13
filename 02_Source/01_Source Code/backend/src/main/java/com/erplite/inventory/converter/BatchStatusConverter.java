package com.erplite.inventory.converter;

import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BatchStatusConverter implements AttributeConverter<BatchStatus, String> {

    @Override
    public String convertToDatabaseColumn(BatchStatus attribute) {
        if (attribute == null) return null;
        return attribute.getDbValue();
    }

    @Override
    public BatchStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        for (BatchStatus s : BatchStatus.values()) {
            if (s.getDbValue().equals(dbData)) return s;
        }
        throw new IllegalArgumentException("Unknown DB value for BatchStatus: " + dbData);
    }
}
