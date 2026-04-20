package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.ProductionBatch;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class ProductionBatchSerializer implements RowSerializer<ProductionBatch> {

    @Override public String sourceTable() { return "ProductionBatches"; }
    @Override public Class<ProductionBatch> entityClass() { return ProductionBatch.class; }
    @Override public String pkField() { return "batchId"; }
    @Override public String cursorField() { return "modifiedDate"; }
    @Override public String pk(ProductionBatch b) { return b.getBatchId(); }
    @Override public LocalDateTime cursor(ProductionBatch b) { return b.getModifiedDate(); }

    @Override
    public String content(ProductionBatch b) {
        String part = b.getProduct() == null ? "N/A" : n(b.getProduct().getPartNumber());
        String name = b.getProduct() == null ? "N/A" : n(b.getProduct().getMaterialName());

        return ("Production batch %s number %s producing %s \"%s\". Size %s %s. " +
                "Manufactured %s, expires %s. Status: %s.").formatted(
                n(b.getBatchId()), n(b.getBatchNumber()), part, name,
                n(b.getBatchSize()), n(b.getUnitOfMeasure()),
                n(b.getManufactureDate()), n(b.getExpirationDate()), n(b.getStatus())
        );
    }

    @Override
    public Map<String, Object> payload(ProductionBatch b) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", b.getBatchId());
        p.put("content", content(b));
        if (b.getStatus() != null) p.put("batch_status", b.getStatus().name());
        if (b.getProduct() != null) p.put("material_id", b.getProduct().getMaterialId());
        if (b.getBatchNumber() != null) p.put("batch_number", b.getBatchNumber());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
