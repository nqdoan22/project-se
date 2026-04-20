package com.erplite.inventory.rag.query;

import com.erplite.inventory.rag.store.VectorStore;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PromptBuilder {

    private static final String SYSTEM = """
            Bạn là trợ lý Inventory Management System.
            Trả lời CHỈ dựa trên dữ liệu được cung cấp. Nếu không có dữ liệu, nói "Không tìm thấy".
            Trả lời ngắn gọn bằng tiếng Việt, luôn kèm con số và đơn vị chính xác.
            Khi nhắc đến lô/lot, vật tư, batch: ghi rõ mã tham chiếu ở cuối câu dạng [Table#ID].
            """;

    /** Build messages for the main LLM call. */
    public List<Map<String, String>> build(String question,
                                           List<VectorStore.Hit> hits,
                                           List<Map<String, String>> historyPairs) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM));
        if (historyPairs != null) messages.addAll(historyPairs);

        StringBuilder ctx = new StringBuilder();
        ctx.append("=== Dữ liệu truy xuất ===\n");
        if (hits == null || hits.isEmpty()) {
            ctx.append("(không có kết quả)\n");
        } else {
            for (VectorStore.Hit h : hits) {
                Object table = h.payload().getOrDefault("source_table", "?");
                Object pk = h.payload().getOrDefault("source_pk", "?");
                Object content = h.payload().getOrDefault("content", "");
                ctx.append("[").append(table).append("#").append(pk).append("] ")
                        .append(content).append("\n");
            }
        }
        ctx.append("\n=== Câu hỏi ===\n").append(question);
        messages.add(Map.of("role", "user", "content", ctx.toString()));
        return messages;
    }
}
