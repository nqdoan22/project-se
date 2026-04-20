package com.erplite.inventory.rag.sync;

import com.erplite.inventory.rag.RagProperties;
import com.erplite.inventory.rag.embed.EmbeddingClient;
import com.erplite.inventory.rag.ingest.RowSerializer;
import com.erplite.inventory.rag.store.VectorStore;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@ConditionalOnProperty(name = "rag.sync.enabled", havingValue = "true", matchIfMissing = true)
public class SyncWorker {

    private static final LocalDateTime EPOCH = LocalDateTime.of(1970, 1, 1, 0, 0);
    private static final int VECTOR_SIZE = 1536;

    private final List<RowSerializer<?>> serializers;
    private final RagProperties props;
    private final RagSyncStateRepository stateRepo;
    private final RagEmbedLogRepository embedLogRepo;
    private final EmbeddingClient embeddingClient;
    private final VectorStore vectorStore;
    private final SyncWorker self;

    @PersistenceContext
    private EntityManager em;

    private final AtomicBoolean running = new AtomicBoolean(false);

    public SyncWorker(List<RowSerializer<?>> serializers,
                      RagProperties props,
                      RagSyncStateRepository stateRepo,
                      RagEmbedLogRepository embedLogRepo,
                      EmbeddingClient embeddingClient,
                      VectorStore vectorStore,
                      @Lazy SyncWorker self) {
        this.serializers = serializers;
        this.props = props;
        this.stateRepo = stateRepo;
        this.embedLogRepo = embedLogRepo;
        this.embeddingClient = embeddingClient;
        this.vectorStore = vectorStore;
        this.self = self;
    }

    @PostConstruct
    void bootstrap() {
        if (props.qdrant().host() == null || props.qdrant().host().isBlank()) {
            log.warn("RAG: QDRANT_HOST not set — sync worker will no-op.");
            return;
        }
        try {
            vectorStore.ensureCollection(VECTOR_SIZE);
        } catch (Exception e) {
            log.warn("RAG: failed to ensure Qdrant collection on startup: {}", e.getMessage());
        }
    }

    @Scheduled(fixedDelayString = "${rag.sync.interval-ms:60000}", initialDelay = 15000)
    public void runTick() {
        if (props.qdrant().host() == null || props.qdrant().host().isBlank()) return;
        if (props.openai().apiKey() == null || props.openai().apiKey().isBlank()) {
            log.debug("RAG: OPENAI_API_KEY not set — skipping sync tick.");
            return;
        }
        if (!running.compareAndSet(false, true)) {
            log.debug("RAG: previous sync tick still running; skip.");
            return;
        }
        try {
            for (RowSerializer<?> s : serializers) {
                try {
                    self.syncOneTable(s);
                } catch (Exception e) {
                    log.error("RAG: sync failed for table {}: {}", s.sourceTable(), e.getMessage(), e);
                    recordTableError(s.sourceTable(), e.getMessage());
                }
            }
        } finally {
            running.set(false);
        }
    }

    @Transactional
    public <T> void syncOneTable(RowSerializer<T> s) {
        String table = s.sourceTable();
        RagSyncState state = stateRepo.findById(table).orElseGet(() -> {
            RagSyncState fresh = RagSyncState.builder()
                    .sourceTable(table)
                    .lastModifiedAt(EPOCH)
                    .rowsSynced(0L)
                    .build();
            return stateRepo.save(fresh);
        });

        LocalDateTime cursorTs = state.getLastModifiedAt() != null ? state.getLastModifiedAt() : EPOCH;
        String cursorPk = state.getLastPk() == null ? "" : state.getLastPk();

        String jpql = "SELECT e FROM " + s.entityClass().getSimpleName() + " e " +
                "WHERE e." + s.cursorField() + " > :ts " +
                "   OR (e." + s.cursorField() + " = :ts AND e." + s.pkField() + " > :pk) " +
                "ORDER BY e." + s.cursorField() + " ASC, e." + s.pkField() + " ASC";
        TypedQuery<T> q = em.createQuery(jpql, s.entityClass())
                .setParameter("ts", cursorTs)
                .setParameter("pk", cursorPk)
                .setMaxResults(props.sync().batchSize());
        List<T> rows = q.getResultList();
        if (rows.isEmpty()) return;

        List<String> pks = rows.stream().map(s::pk).toList();
        List<RagEmbedLog> existing = embedLogRepo.findBySourceTableAndSourcePkIn(table, pks);
        Map<String, RagEmbedLog> byPk = new HashMap<>();
        for (RagEmbedLog e : existing) byPk.put(e.getSourcePk(), e);

        List<T> toEmbed = new ArrayList<>();
        List<String> contents = new ArrayList<>();
        List<String> hashes = new ArrayList<>();

        for (T row : rows) {
            String content = s.content(row);
            String hash = sha256(content);
            RagEmbedLog prev = byPk.get(s.pk(row));
            if (prev == null || !hash.equals(prev.getContentHash()) || prev.getStatus() == RagEmbedLog.Status.FAILED) {
                toEmbed.add(row);
                contents.add(content);
                hashes.add(hash);
            }
        }

        if (!toEmbed.isEmpty()) {
            List<float[]> vectors;
            try {
                vectors = embeddingClient.embed(contents);
            } catch (Exception e) {
                log.error("RAG: embedding call failed for table {} ({} rows): {}",
                        table, toEmbed.size(), e.getMessage());
                // Mark each row as FAILED but DO NOT advance cursor past them.
                for (int i = 0; i < toEmbed.size(); i++) {
                    T row = toEmbed.get(i);
                    upsertLog(table, s.pk(row), hashes.get(i), RagEmbedLog.Status.FAILED,
                            truncate(e.getMessage(), 500), byPk.get(s.pk(row)));
                }
                return;
            }

            List<VectorStore.Point> points = new ArrayList<>(toEmbed.size());
            for (int i = 0; i < toEmbed.size(); i++) {
                T row = toEmbed.get(i);
                String pointId = deterministicPointId(table, s.pk(row));
                points.add(new VectorStore.Point(pointId, vectors.get(i), s.payload(row)));
            }
            try {
                vectorStore.upsert(points);
            } catch (Exception e) {
                log.error("RAG: Qdrant upsert failed for table {}: {}", table, e.getMessage());
                for (int i = 0; i < toEmbed.size(); i++) {
                    T row = toEmbed.get(i);
                    upsertLog(table, s.pk(row), hashes.get(i), RagEmbedLog.Status.FAILED,
                            truncate(e.getMessage(), 500), byPk.get(s.pk(row)));
                }
                return;
            }

            for (int i = 0; i < toEmbed.size(); i++) {
                T row = toEmbed.get(i);
                upsertLog(table, s.pk(row), hashes.get(i), RagEmbedLog.Status.OK, null,
                        byPk.get(s.pk(row)));
            }
        }

        // Advance cursor to last row.
        T last = rows.get(rows.size() - 1);
        state.setLastModifiedAt(s.cursor(last));
        state.setLastPk(s.pk(last));
        state.setRowsSynced(state.getRowsSynced() + rows.size());
        state.setLastRunAt(LocalDateTime.now());
        state.setLastError(null);
        stateRepo.save(state);

        log.info("RAG: synced {} rows from {} (cursor now {} / {})",
                rows.size(), table, state.getLastModifiedAt(), state.getLastPk());
    }

    private void upsertLog(String table, String pk, String hash, RagEmbedLog.Status status,
                           String errorMessage, RagEmbedLog existing) {
        RagEmbedLog entry = existing != null ? existing : RagEmbedLog.builder()
                .sourceTable(table)
                .sourcePk(pk)
                .build();
        entry.setContentHash(hash);
        entry.setEmbeddedAt(LocalDateTime.now());
        entry.setModel(embeddingClient.modelId());
        entry.setStatus(status);
        entry.setErrorMessage(errorMessage);
        embedLogRepo.save(entry);
    }

    private void recordTableError(String table, String message) {
        RagSyncState state = stateRepo.findById(table).orElseGet(() -> RagSyncState.builder()
                .sourceTable(table).lastModifiedAt(EPOCH).rowsSynced(0L).build());
        state.setLastRunAt(LocalDateTime.now());
        state.setLastError(truncate(message, 1000));
        stateRepo.save(state);
    }

    public static String deterministicPointId(String table, String pk) {
        return UUID.nameUUIDFromBytes((table + ":" + pk).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
