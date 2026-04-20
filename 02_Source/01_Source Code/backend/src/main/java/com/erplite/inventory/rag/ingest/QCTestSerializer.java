package com.erplite.inventory.rag.ingest;

import com.erplite.inventory.entity.QCTest;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class QCTestSerializer implements RowSerializer<QCTest> {

    @Override public String sourceTable() { return "QCTests"; }
    @Override public Class<QCTest> entityClass() { return QCTest.class; }
    @Override public String pkField() { return "testId"; }
    @Override public String cursorField() { return "modifiedDate"; }
    @Override public String pk(QCTest q) { return q.getTestId(); }
    @Override public LocalDateTime cursor(QCTest q) { return q.getModifiedDate(); }

    @Override
    public String content(QCTest q) {
        String lotId = q.getLot() == null ? "N/A" : n(q.getLot().getLotId());
        String matPart = q.getLot() == null || q.getLot().getMaterial() == null
                ? "N/A" : n(q.getLot().getMaterial().getPartNumber());

        return ("QC test %s on lot %s (material %s). Type %s by method %s. Tested %s. " +
                "Result: \"%s\" vs criteria \"%s\". Status: %s. " +
                "Performed by %s, verified by %s.").formatted(
                n(q.getTestId()), lotId, matPart,
                n(q.getTestType()), n(q.getTestMethod()), n(q.getTestDate()),
                n(q.getTestResult()), n(q.getAcceptanceCriteria()), n(q.getResultStatus()),
                n(q.getPerformedBy()), n(q.getVerifiedBy())
        );
    }

    @Override
    public Map<String, Object> payload(QCTest q) {
        Map<String, Object> p = new HashMap<>();
        p.put("source_table", sourceTable());
        p.put("source_pk", q.getTestId());
        p.put("content", content(q));
        if (q.getResultStatus() != null) p.put("result_status", q.getResultStatus().name());
        if (q.getLot() != null) p.put("lot_id", q.getLot().getLotId());
        if (q.getTestType() != null) p.put("test_type", q.getTestType().name());
        return p;
    }

    private static String n(Object v) { return v == null ? "N/A" : v.toString(); }
}
