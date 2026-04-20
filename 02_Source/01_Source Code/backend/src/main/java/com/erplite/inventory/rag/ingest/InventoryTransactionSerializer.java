package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.InventoryTransaction;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class InventoryTransactionSerializer implements RowSerializer<InventoryTransaction> {

    @Override public String sourceTable() { return "InventoryTransactions"; }
    @Override public Class<InventoryTransaction> entityClass() { return InventoryTransaction.class; }
    @Override public String pkField() { return "transactionId"; }
    @Override public String cursorField() { return "createdDate"; }
    @Override public String pk(InventoryTransaction t) { return t.getTransactionId(); }
    @Override public LocalDateTime cursor(InventoryTransaction t) { return t.getCreatedDate(); }

    @Override
    public String content(InventoryTransaction t) {
        String lotId = t.getLot() == null ? "N/A" : n(t.getLot().getLotId());
        String matPart = t.getLot() == null || t.getLot().getMaterial() == null
                ? "N/A" : n(t.getLot().getMaterial().getPartNumber());

        return ("Transaction %s: %s of %s %s on lot %s (material %s) on %s. " +
                "Ref: %s. By %s. Notes: %s.").formatted(
                n(t.getTransactionId()), n(t.getTransactionType()),
                n(t.getQuantity()), n(t.getUnitOfMeasure()), lotId, matPart,
                n(t.getTransactionDate()), n(t.getReferenceId()),
                n(t.getPerformedBy()), n(t.getNotes())
        );
    }

    @Override
    public Map<String, Object> payload(InventoryTransaction t) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", t.getTransactionId());
        p.put("content", content(t));
        if (t.getTransactionType() != null) p.put("transaction_type", t.getTransactionType().name());
        if (t.getLot() != null) p.put("lot_id", t.getLot().getLotId());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
