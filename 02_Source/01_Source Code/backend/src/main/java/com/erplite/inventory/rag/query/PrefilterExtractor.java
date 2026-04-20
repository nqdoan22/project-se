package com.erplite.inventory.rag.query;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrefilterExtractor {

    private static final Pattern LOT_PATTERN = Pattern.compile(
            "(?i)(?:lot|lô)[\\s\\-:#]*([A-Za-z0-9\\-]{3,})");
    private static final Pattern PART_PATTERN = Pattern.compile(
            "(?i)(?:part|mã|material)[\\s\\-:#]*([A-Za-z0-9\\-]{2,})|\\b([A-Z]{2,}-\\d+|CT\\d+)\\b");

    private final LlmClient llmClient;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Extracted filters + intent hints. `must` is the filter passed to Qdrant.
     * `preferTables` is a list of source_table values to boost (used in the follow-up
     * if the primary search returns too few hits).
     */
    public record Filters(Map<String, Object> must, List<String> preferTables) {}

    public Filters extract(String question) {
        Map<String, Object> must = new LinkedHashMap<>();
        List<String> prefer = new java.util.ArrayList<>();

        // Tier 1 — regex.
        Matcher lm = LOT_PATTERN.matcher(question);
        if (lm.find()) {
            String lot = lm.group(1);
            if (lot != null && !lot.isBlank()) {
                // Keep as a hint in `must` against manufacturer_lot OR source_pk — Qdrant
                // filters must be AND'd, so we can't OR here; instead we prefer the Lots
                // table and drop the filter if the initial search is empty (retry path).
                must.put("source_table", "InventoryLots");
                must.put("manufacturer_lot", lot);
                prefer.add("InventoryLots");
            }
        }
        Matcher pm = PART_PATTERN.matcher(question);
        if (pm.find()) {
            String part = pm.group(1) != null ? pm.group(1) : pm.group(2);
            if (part != null && !part.isBlank()) {
                // If we already filtered by lot, don't clobber.
                must.putIfAbsent("material_part_number", part);
                if (prefer.isEmpty()) prefer.add("Materials");
            }
        }

        String lower = question.toLowerCase(Locale.ROOT);
        if (lower.contains("qc") || lower.contains("kiểm nghi") || lower.contains("kiểm tra")
                || lower.contains("pass") || lower.contains("fail")) {
            if (!must.containsKey("source_table")) prefer.add("QCTests");
        }
        if (lower.contains("batch") || lower.contains("lô sản xuất") || lower.contains("sản xuất")) {
            if (!must.containsKey("source_table")) prefer.add("ProductionBatches");
        }
        if (lower.contains("còn") || lower.contains("tồn") || lower.contains("bao nhiêu")
                || lower.contains("quantity")) {
            if (!must.containsKey("source_table")) prefer.add("InventoryLots");
        }

        if (!must.isEmpty()) return new Filters(must, prefer);

        // Tier 2 — LLM fallback.
        try {
            String json = llmClient.completeJson(List.of(
                    Map.of("role", "system",
                            "content", "You extract entities from inventory questions. " +
                                    "Respond as STRICT JSON with keys: lot_id, part_number, status, intent. " +
                                    "Use null if absent."),
                    Map.of("role", "user", "content", question)
            ), 0.0, 150);
            JsonNode node = mapper.readTree(json);
            String lot = optText(node, "lot_id");
            String part = optText(node, "part_number");
            String status = optText(node, "status");

            Map<String, Object> fMust = new HashMap<>();
            if (lot != null) {
                fMust.put("source_table", "InventoryLots");
                fMust.put("manufacturer_lot", lot);
            } else if (part != null) {
                fMust.put("material_part_number", part);
            }
            if (status != null) {
                fMust.putIfAbsent("lot_status", status);
            }
            List<String> fPrefer = new java.util.ArrayList<>();
            if (lot != null || fMust.containsKey("lot_status")) fPrefer.add("InventoryLots");
            return new Filters(fMust, fPrefer);
        } catch (Exception e) {
            log.debug("RAG prefilter LLM failed: {}", e.getMessage());
            return new Filters(Map.of(), prefer);
        }
    }

    private static String optText(JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v == null || v.isNull()) return null;
        String s = v.asText();
        return (s == null || s.isBlank() || "null".equalsIgnoreCase(s)) ? null : s;
    }
}
