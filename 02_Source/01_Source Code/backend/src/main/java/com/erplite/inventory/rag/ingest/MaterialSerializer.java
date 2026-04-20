package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.Material;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class MaterialSerializer implements RowSerializer<Material> {

    @Override public String sourceTable() { return "Materials"; }
    @Override public Class<Material> entityClass() { return Material.class; }
    @Override public String pkField() { return "materialId"; }
    @Override public String cursorField() { return "modifiedDate"; }
    @Override public String pk(Material m) { return m.getMaterialId(); }
    @Override public LocalDateTime cursor(Material m) { return m.getModifiedDate(); }

    @Override
    public String content(Material m) {
        return "Material %s \"%s\" of type %s. Storage: %s. Spec doc: %s. Created %s, modified %s.".formatted(
                n(m.getPartNumber()),
                n(m.getMaterialName()),
                n(m.getMaterialType()),
                n(m.getStorageConditions()),
                n(m.getSpecificationDocument()),
                n(m.getCreatedDate()),
                n(m.getModifiedDate())
        );
    }

    @Override
    public Map<String, Object> payload(Material m) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", m.getMaterialId());
        p.put("content", content(m));
        if (m.getMaterialType() != null) p.put("material_type", m.getMaterialType().name());
        if (m.getPartNumber() != null) p.put("part_number", m.getPartNumber());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
