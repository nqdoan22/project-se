package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.InventoryLot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class InventoryLotSerializer implements RowSerializer<InventoryLot> {

    @Override public String sourceTable() { return "InventoryLots"; }
    @Override public Class<InventoryLot> entityClass() { return InventoryLot.class; }
    @Override public String pkField() { return "lotId"; }
    @Override public String cursorField() { return "modifiedDate"; }
    @Override public String pk(InventoryLot l) { return l.getLotId(); }
    @Override public LocalDateTime cursor(InventoryLot l) { return l.getModifiedDate(); }

    @Override
    public String content(InventoryLot l) {
        String materialPart = l.getMaterial() == null ? "N/A" : n(l.getMaterial().getPartNumber());
        String materialName = l.getMaterial() == null ? "N/A" : n(l.getMaterial().getMaterialName());
        String materialType = l.getMaterial() == null || l.getMaterial().getMaterialType() == null
                ? "N/A" : l.getMaterial().getMaterialType().name();
        String parent = l.getParentLot() == null ? "none" : n(l.getParentLot().getLotId());

        return ("Inventory lot %s of material %s \"%s\" (type %s) from manufacturer %s " +
                "(mfr lot %s), supplier %s. Quantity: %s %s. Status: %s. " +
                "Received %s, expires %s (in-use expiration %s). " +
                "Stored at %s. Is sample: %s. PO: %s. Parent lot: %s.").formatted(
                n(l.getLotId()), materialPart, materialName, materialType,
                n(l.getManufacturerName()), n(l.getManufacturerLot()), n(l.getSupplierName()),
                n(l.getQuantity()), n(l.getUnitOfMeasure()), n(l.getStatus()),
                n(l.getReceivedDate()), n(l.getExpirationDate()), n(l.getInUseExpirationDate()),
                n(l.getStorageLocation()), n(l.getIsSample()), n(l.getPoNumber()), parent
        );
    }

    @Override
    public Map<String, Object> payload(InventoryLot l) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", l.getLotId());
        p.put("content", content(l));
        if (l.getStatus() != null) p.put("lot_status", l.getStatus().name());
        if (l.getMaterial() != null) {
            p.put("material_id", l.getMaterial().getMaterialId());
            if (l.getMaterial().getPartNumber() != null)
                p.put("material_part_number", l.getMaterial().getPartNumber());
        }
        if (l.getManufacturerLot() != null) p.put("manufacturer_lot", l.getManufacturerLot());
        if (l.getIsSample() != null) p.put("is_sample", l.getIsSample());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
