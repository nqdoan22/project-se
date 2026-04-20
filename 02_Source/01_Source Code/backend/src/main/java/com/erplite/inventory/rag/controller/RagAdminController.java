package com.erplite.inventory.rag.controller;

import com.erplite.inventory.rag.dto.HealthResponse;
import com.erplite.inventory.rag.ingest.RowSerializer;
import com.erplite.inventory.rag.store.VectorStore;
import com.erplite.inventory.rag.sync.RagEmbedLog;
import com.erplite.inventory.rag.sync.RagEmbedLogRepository;
import com.erplite.inventory.rag.sync.RagSyncStateRepository;
import com.erplite.inventory.rag.sync.SyncWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RagAdminController {

    private final RagSyncStateRepository stateRepo;
    private final RagEmbedLogRepository embedLogRepo;
    private final VectorStore vectorStore;
    private final List<RowSerializer<?>> serializers;

    @GetMapping("/health")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<HealthResponse> health() {
        List<HealthResponse.TableStatus> tables = stateRepo.findAll().stream()
                .map(s -> new HealthResponse.TableStatus(
                        s.getSourceTable(),
                        s.getLastModifiedAt(),
                        s.getRowsSynced(),
                        s.getLastRunAt(),
                        s.getLastError()))
                .toList();
        boolean qdrantOk = false;
        String msg = "unknown";
        try {
            qdrantOk = vectorStore.ping();
            msg = qdrantOk ? "ok" : "unreachable";
        } catch (Exception e) {
            msg = e.getMessage();
        }
        return ResponseEntity.ok(new HealthResponse(tables, qdrantOk, msg));
    }

    @PostMapping("/admin/resync")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> resyncAll() {
        vectorStore.deleteAll();
        stateRepo.deleteAll();
        embedLogRepo.deleteAll();
        return ResponseEntity.ok(Map.of(
                "cleared", "all",
                "resetAt", LocalDateTime.now().toString()));
    }

    @PostMapping("/admin/resync/{table}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, Object>> resyncTable(@PathVariable String table) {
        boolean valid = serializers.stream().anyMatch(s -> s.sourceTable().equalsIgnoreCase(table));
        if (!valid) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "unknown table",
                    "table", table));
        }
        stateRepo.findById(table).ifPresent(s -> {
            s.setLastModifiedAt(LocalDateTime.of(1970, 1, 1, 0, 0));
            s.setLastPk(null);
            s.setRowsSynced(0L);
            s.setLastError(null);
            stateRepo.save(s);
        });
        embedLogRepo.deleteByTable(table);
        return ResponseEntity.ok(Map.of(
                "table", table,
                "resetAt", LocalDateTime.now().toString()));
    }

    @DeleteMapping("/admin/point/{table}/{pk}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> deletePoint(@PathVariable String table, @PathVariable String pk) {
        String pointId = SyncWorker.deterministicPointId(table, pk);
        vectorStore.delete(List.of(pointId));
        embedLogRepo.findBySourceTableAndSourcePk(table, pk).ifPresent(log -> {
            log.setStatus(RagEmbedLog.Status.SKIPPED);
            embedLogRepo.save(log);
        });
        return ResponseEntity.noContent().build();
    }
}
