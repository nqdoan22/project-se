package com.erplite.inventory.converter;

import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToBatchStatusConverter implements Converter<String, BatchStatus> {
    @Override
    public BatchStatus convert(String source) {
        return BatchStatus.fromJson(source);
    }
}
