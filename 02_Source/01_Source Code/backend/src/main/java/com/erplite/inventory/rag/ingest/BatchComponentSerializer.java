package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.BatchComponent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class BatchComponentSerializer implements RowSerializer<BatchComponent> {

    @Override public String sourceTable() { return "BatchComponents"; }
    @Override public Class<BatchComponent> entityClass() { return BatchComponent.class; }
    @Override public String pkField() { return "componentId"; }
    @Override public String cursorField() { return "modifiedDate"; }
    @Override public String pk(BatchComponent c) { return c.getComponentId(); }
    @Override public LocalDateTime cursor(BatchComponent c) { return c.getModifiedDate(); }

    @Override
    public String content(BatchComponent c) {
        String batchNum = c.getBatch() == null ? "N/A" : n(c.getBatch().getBatchNumber());
        String lotId = c.getLot() == null ? "N/A" : n(c.getLot().getLotId());
        String matPart = c.getLot() == null || c.getLot().getMaterial() == null
                ? "N/A" : n(c.getLot().getMaterial().getPartNumber());

        return ("Component %s of batch %s uses lot %s (material %s). " +
                "Planned %s %s, actual %s. Added %s by %s.").formatted(
                n(c.getComponentId()), batchNum, lotId, matPart,
                n(c.getPlannedQuantity()), n(c.getUnitOfMeasure()), n(c.getActualQuantity()),
                n(c.getAdditionDate()), n(c.getAddedBy())
        );
    }

    @Override
    public Map<String, Object> payload(BatchComponent c) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", c.getComponentId());
        p.put("content", content(c));
        if (c.getBatch() != null) p.put("batch_id", c.getBatch().getBatchId());
        if (c.getLot() != null) p.put("lot_id", c.getLot().getLotId());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
