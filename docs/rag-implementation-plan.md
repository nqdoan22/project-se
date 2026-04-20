# RAG Chat — Implementation Plan

## 0. Decisions Log

| Decision | Choice | Reason |
|---|---|---|
| Sync cursor column | `modified_date` (6 tables), `created_date` (InventoryTransactions) | Already present on every business entity via `@PreUpdate` + SQL `ON UPDATE CURRENT_TIMESTAMP`; no schema churn. |
| Vector database | Qdrant Cloud free tier (1GB) | Payload filter fits enum-heavy schema (status, material_type, result_status); no extra container; avoids Aiven vendor lock and MySQL 9 upgrade risk. |
| Soft delete | Skipped for MVP | Admin endpoint performs explicit per-row removal and full resync; simpler migration, no column churn. |
| Embedding model | OpenAI `text-embedding-3-small` (1536-dim) | Cheap (~$0.02/1M tokens), multilingual (Vietnamese OK), batch-friendly. |
| LLM (answer + extraction) | OpenAI `gpt-4o-mini` | Single `OPENAI_API_KEY` covers embedding, pre-filter extraction, and generation. |
| Rate limiting on chat endpoint | None added — rely on existing Spring Security / Keycloak auth | No pre-existing rate-limit layer in the project; chat endpoint requires authenticated user, admin endpoints require `ROLE_Admin`. |
| Migration tooling | Plain SQL files in `database/migrations/`, run manually | Project already uses `spring.jpa.hibernate.ddl-auto=none` with no Flyway/Liquibase; stays consistent. |
| Scheduling | Add `@EnableScheduling` (first in project) | No existing background job to follow; SyncWorker establishes the convention. |

---

## 1. Current State Summary

**Stack**: Spring Boot 4.0.2, Java 21, Gradle 8.14, MySQL (Aiven), `spring.jpa.hibernate.ddl-auto=none`. Package root `com.erplite.inventory`. Auth: Spring Security OAuth2 Resource Server validating Keycloak JWT; `realm_access.roles` mapped to `ROLE_<role>` authorities. Everything under `/api/v1/**` is authenticated except `/api/v1/health`. `@PreAuthorize` is the convention (`isAuthenticated()`, `hasRole('Admin')`, `hasAnyRole(...)`).

**Business tables in RAG scope (6)**:

| Table | PK | Cursor column | Notes |
|---|---|---|---|
| `Materials` | `material_id` | `modified_date` | Master catalog. |
| `InventoryLots` | `lot_id` | `modified_date` | Primary business entity for chat queries. |
| `InventoryTransactions` | `transaction_id` | `created_date` | Immutable, append-only; no UPDATE path. |
| `ProductionBatches` | `batch_id` | `modified_date` | FK `product_id` → Materials. |
| `BatchComponents` | `component_id` | `modified_date` | FK to batch + lot. |
| `QCTests` | `test_id` | `modified_date` | FK to lot. |

**Excluded from RAG**: `Users` (auth metadata), `LabelTemplates` (label printing config) — neither holds inventory business data that chat queries would retrieve.

**No existing**: RAG code, embedding code, vector store, scheduled job, or rate limiter in the codebase.

---

## 2. Database Migrations

Files go in **`02_Source/01_Source Code/database/migrations/`** (new directory). Run manually on staging Aiven first, verify with `EXPLAIN`, then promote to production. No Flyway baseline needed; the existing `dbscript.sql` is V001 by convention.

### V002 — Add sync indexes (6 tables only)

```sql
-- V002__add_rag_sync_indexes.sql
-- Purpose: support incremental sync cursor queries
--   (modified_date > :cursor) or (created_date > :cursor for InventoryTransactions).
-- Non-breaking: index-only change, no data mutation.
-- Target: MySQL 8.0+ (generic syntax, no IF NOT EXISTS on ADD INDEX).
-- Run once. Idempotent behavior is handled by run-once migration discipline.

ALTER TABLE Materials
  ADD INDEX idx_materials_modified (modified_date);

ALTER TABLE InventoryLots
  ADD INDEX idx_lots_modified (modified_date);

ALTER TABLE ProductionBatches
  ADD INDEX idx_batches_modified (modified_date);

ALTER TABLE BatchComponents
  ADD INDEX idx_components_modified (modified_date);

ALTER TABLE QCTests
  ADD INDEX idx_qc_modified (modified_date);

-- InventoryTransactions is append-only; cursor tracks created_date.
ALTER TABLE InventoryTransactions
  ADD INDEX idx_txn_created (created_date);
```

### V003 — Create RAG system tables

```sql
-- V003__create_rag_system_tables.sql
-- Purpose: system tables for RAG pipeline bookkeeping.
--   rag_sync_state — per-table cursor for the sync worker.
--   rag_embed_log  — audit + content-hash dedupe of embedded rows.
--   rag_query_log  — observability and feedback for chat queries.
-- Non-breaking: new tables only.

CREATE TABLE IF NOT EXISTS rag_sync_state (
    source_table     VARCHAR(64)  NOT NULL PRIMARY KEY,
    last_modified_at DATETIME     NOT NULL DEFAULT '1970-01-01 00:00:00',
    last_pk          VARCHAR(36)  NULL COMMENT 'Tie-breaker when multiple rows share the same cursor timestamp',
    rows_synced      BIGINT       NOT NULL DEFAULT 0,
    last_run_at      DATETIME     NULL,
    last_error       TEXT         NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_embed_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_table   VARCHAR(64)  NOT NULL,
    source_pk      VARCHAR(36)  NOT NULL,
    content_hash   CHAR(64)     NOT NULL COMMENT 'SHA-256 hex of serialized row text',
    embedded_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    model          VARCHAR(100) NOT NULL,
    status         ENUM('OK','SKIPPED','FAILED') NOT NULL,
    error_message  TEXT         NULL,
    UNIQUE KEY uk_embed_source_pk (source_table, source_pk),
    INDEX idx_embed_hash (content_hash),
    INDEX idx_embed_status (status, embedded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_query_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id        VARCHAR(36)  NULL COMMENT 'Keycloak sub claim',
    question       TEXT         NOT NULL,
    retrieved_ids  JSON         NULL COMMENT 'Array of {source_table, source_pk, score}',
    answer         TEXT         NULL,
    latency_ms     INT          NULL,
    feedback       ENUM('up','down') NULL,
    asked_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_query_user (user_id, asked_at),
    INDEX idx_query_time (asked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

**Run order**: V002 → V003. Both on staging first; verify `EXPLAIN SELECT * FROM InventoryLots WHERE modified_date > NOW() - INTERVAL 1 HOUR` shows `idx_lots_modified` before promoting to prod.

---

## 3. New Files & Package Structure

All paths relative to `02_Source/01_Source Code/backend/src/main/java/com/erplite/inventory/`.

```
rag/
├── RagConfig.java                         # @Configuration: QdrantClient, OpenAI client, scheduling enable
├── RagProperties.java                     # @ConfigurationProperties("rag") — typed env binding
│
├── embed/
│   ├── EmbeddingClient.java               # interface: List<float[]> embed(List<String> texts)
│   └── OpenAiEmbeddingClient.java         # impl — text-embedding-3-small, batch 100
│
├── store/
│   ├── VectorStore.java                   # interface: upsert / search / delete / count
│   └── QdrantVectorStore.java             # impl — Qdrant Cloud over gRPC (host+port 6334, TLS, API key)
│
├── ingest/
│   ├── RowSerializer.java                 # interface<T>: String pk(T); String content(T); Map<String,Object> payload(T)
│   ├── MaterialSerializer.java            # Materials
│   ├── InventoryLotSerializer.java        # InventoryLots
│   ├── ProductionBatchSerializer.java     # ProductionBatches
│   ├── BatchComponentSerializer.java      # BatchComponents
│   ├── QCTestSerializer.java              # QCTests
│   └── InventoryTransactionSerializer.java # InventoryTransactions (append-only)
│
├── sync/
│   ├── SyncWorker.java                    # @Scheduled(fixedDelayString="${rag.sync.interval-ms}")
│   ├── SyncTableConfig.java               # record: table name, cursor column, serializer, repository ref
│   ├── RagSyncState.java                  # JPA entity for rag_sync_state
│   ├── RagEmbedLog.java                   # JPA entity for rag_embed_log
│   ├── RagSyncStateRepository.java        # Spring Data JPA
│   └── RagEmbedLogRepository.java         # Spring Data JPA
│
├── query/
│   ├── PrefilterExtractor.java            # tiered rule + LLM fallback
│   ├── RagQueryService.java               # orchestrator: extract → embed → search → prompt → LLM
│   ├── PromptBuilder.java                 # pure formatter
│   ├── LlmClient.java                     # interface
│   ├── OpenAiLlmClient.java               # impl — gpt-4o-mini
│   ├── RagQueryLog.java                   # JPA entity for rag_query_log
│   └── RagQueryLogRepository.java
│
├── controller/
│   ├── RagChatController.java             # POST /chat, POST /feedback/{id}
│   └── RagAdminController.java            # GET /health, POST /admin/resync[/{table}], DELETE /admin/point/{table}/{pk}
│
└── dto/
    ├── ChatRequest.java                   # { question: String }
    ├── ChatResponse.java                  # { answer, sources, queryLogId }
    ├── SourceRef.java                     # { sourceTable, sourcePk, score }
    └── FeedbackRequest.java               # { feedback: "up"|"down" }
```

Also modified: `InventoryApplication.java` (add `@EnableScheduling`) or equivalently put the annotation on `RagConfig`.

---

## 4. Row Serializer Templates

Every serializer produces three things: deterministic **pk** (UUID string), **content** text (below), **payload** map (Qdrant filter fields). Point ID in Qdrant is `UUID.nameUUIDFromBytes(("{source_table}:{pk}").getBytes())` — deterministic, so upserts are idempotent.

### 4.1 Materials
```
Material {partNumber} "{materialName}" of type {materialType}.
Storage: {storageConditions}. Spec doc: {specificationDocument}.
Created {createdDate}, modified {modifiedDate}.
```
Payload: `source_table=Materials`, `source_pk=material_id`, `material_type`, `part_number`.

### 4.2 InventoryLots
```
Inventory lot {lotId} of material {material.partNumber} "{material.materialName}"
(type {material.materialType}) from manufacturer {manufacturerName}
(mfr lot {manufacturerLot}), supplier {supplierName}.
Quantity: {quantity} {unitOfMeasure}. Status: {status}.
Received {receivedDate}, expires {expirationDate} (in-use expiration {inUseExpirationDate}).
Stored at {storageLocation}. Is sample: {isSample}. PO: {poNumber}.
Parent lot: {parentLot.lotId|none}.
```
Payload: `source_table=InventoryLots`, `source_pk=lot_id`, `lot_status`, `material_id`, `material_part_number`, `manufacturer_lot`, `is_sample`.

### 4.3 ProductionBatches
```
Production batch {batchId} number {batchNumber} producing {product.partNumber} "{product.materialName}".
Size {batchSize} {unitOfMeasure}. Manufactured {manufactureDate}, expires {expirationDate}.
Status: {status}.
```
Payload: `source_table=ProductionBatches`, `source_pk=batch_id`, `batch_status`, `product_id`, `batch_number`.

### 4.4 BatchComponents
```
Component {componentId} of batch {batch.batchNumber} uses lot {lot.lotId}
(material {lot.material.partNumber}). Planned {plannedQuantity} {unitOfMeasure},
actual {actualQuantity}. Added {additionDate} by {addedBy}.
```
Payload: `source_table=BatchComponents`, `source_pk=component_id`, `batch_id`, `lot_id`.

### 4.5 QCTests
```
QC test {testId} on lot {lot.lotId} (material {lot.material.partNumber}).
Type {testType} by method {testMethod}. Tested {testDate}.
Result: "{testResult}" vs criteria "{acceptanceCriteria}".
Status: {resultStatus}. Performed by {performedBy}, verified by {verifiedBy}.
```
Payload: `source_table=QCTests`, `source_pk=test_id`, `result_status`, `lot_id`, `test_type`.

### 4.6 InventoryTransactions (append-only)
```
Transaction {transactionId}: {transactionType} of {quantity} {unitOfMeasure}
on lot {lot.lotId} (material {lot.material.partNumber}) on {transactionDate}.
Ref: {referenceId}. By {performedBy}. Notes: {notes}.
```
Payload: `source_table=InventoryTransactions`, `source_pk=transaction_id`, `transaction_type`, `lot_id`.

---

## 5. Sync Worker Logic

**Cadence**: `@Scheduled(fixedDelayString="${rag.sync.interval-ms}")`, default 60000 ms. Guarded by `@ConditionalOnProperty(name="rag.sync.enabled", havingValue="true", matchIfMissing=true)`.

**Per-table config** (`SyncTableConfig`): holds table logical name, JPA repository reference, cursor column (enum `MODIFIED_DATE` | `CREATED_DATE`), serializer bean, batch size. Registered for the 6 in-scope tables; `InventoryTransactions` is the only one with `CREATED_DATE`.

**Algorithm (pseudocode)**:

```
for each table in [Materials, InventoryLots, ProductionBatches,
                   BatchComponents, QCTests, InventoryTransactions]:
    state = rag_sync_state.findOrCreate(table.name)
    cursorTs = state.last_modified_at
    cursorPk = state.last_pk

    if table.cursorColumn == MODIFIED_DATE:
        rows = repo.findWindowByModifiedDate(cursorTs, cursorPk, batchSize)
        // SELECT * FROM <t> WHERE modified_date > :ts
        //    OR (modified_date = :ts AND pk > :pk)
        // ORDER BY modified_date, pk LIMIT :batchSize
    else:  // CREATED_DATE — InventoryTransactions, append-only
        rows = repo.findWindowByCreatedDate(cursorTs, cursorPk, batchSize)
        // SELECT * FROM InventoryTransactions WHERE created_date > :ts
        //    OR (created_date = :ts AND transaction_id > :pk)
        // ORDER BY created_date, transaction_id LIMIT :batchSize
        // No re-embed path — INSERT-only source.

    if rows.isEmpty(): continue

    contents = rows.map(serializer::content)
    hashes   = contents.map(sha256)

    existingLog = rag_embed_log.findByPks(table.name, rows.pks)
    toEmbed, toSkip = partition(rows, r ->
        existingLog[r.pk] == null || existingLog[r.pk].content_hash != hash(r))

    if toEmbed.nonEmpty:
        vectors = embeddingClient.embed(toEmbed.map(serializer::content))
        points  = zip(toEmbed, vectors).map(makeQdrantPoint)
        vectorStore.upsert(collection, points)
        rag_embed_log.upsertAll(toEmbed, status=OK)

    rag_embed_log.markSkipped(toSkip)  // unchanged rows

    state.last_modified_at = rows.last().cursorTs
    state.last_pk          = rows.last().pk
    state.rows_synced     += rows.size
    state.last_run_at      = now()
    state.last_error       = null
    rag_sync_state.save(state)
```

**Error handling**:
- Embedding or Qdrant call fails → write `status=FAILED`, `error_message=...` to `rag_embed_log` for that row, **do not advance cursor** past the failed row.
- 3 consecutive failures on the same row → force-advance cursor past it, set `rag_sync_state.last_error` with `{source_pk, error}`, emit WARN log. Admin can re-run via per-table resync later.
- Whole-tick exception → catch, log, skip to next table; next tick retries.

**Dedupe**: `content_hash` (SHA-256 of content text) — if unchanged, skip embed call (saves tokens). `modified_date` churn without content change is common (e.g. entity re-saved with no material change).

**Idempotent upsert**: deterministic Qdrant point ID ensures replay of the same row always targets the same point.

**Deletion handling**: None in the worker. Admin endpoint `DELETE /admin/point/{table}/{pk}` removes from Qdrant and marks `rag_embed_log.status='SKIPPED'`; full resync via `POST /admin/resync` truncates `rag_sync_state` + clears Qdrant collection, then lets the worker backfill from epoch.

---

## 6. Query Pipeline

### 6.1 Pre-filter extractor (2 tiers)

**Tier 1 — regex rules** (no token cost, always runs first):
- `lot[\s-]?([A-Za-z0-9\-]+)` → if matched, filter Qdrant on `source_table=InventoryLots` AND (`source_pk` equals OR `manufacturer_lot` equals).
- `(CT\d+|[A-Z]{2,}-\d+)` in context of "vật liệu" / "material" → filter on `material_part_number`.
- Keyword map →
  - `"còn" | "bao nhiêu" | "quantity"` → prefer `source_table=InventoryLots`.
  - `"stage" | "trạng thái" | "status"` → include status fields in prompt emphasis.
  - `"QC" | "kiểm tra" | "test"` → prefer `source_table=QCTests`.

**Tier 2 — LLM fallback** (only if tier 1 finds nothing actionable): `gpt-4o-mini` with JSON mode, single prompt:
```
You extract entities from inventory questions. Respond as strict JSON with keys:
lot_id, part_number, status, intent. Use null if absent.
Question: "{question}"
```
Result merged with tier 1; any conflict → tier 1 wins (cheaper and deterministic).

### 6.2 Retrieval

1. Build Qdrant `Filter` from extracted entities (`must` / `should` clauses).
2. `vectors = embeddingClient.embed([question])`.
3. `hits = vectorStore.search(collection, vectors[0], filter, limit=10)`.
4. If `hits.isEmpty()` and filter was non-trivial → retry once with filter cleared (`limit=10`).
5. Load original serialized content of each hit from its payload (stored alongside the vector) — no second MySQL round-trip.

### 6.3 Prompt template

```
You are the Inventory Management System retrieval assistant.
Answer strictly from the data provided. If absent, say "Không tìm thấy".
Respond concisely in Vietnamese, include exact numbers and units.

=== Retrieved data ===
{for each hit in hits}
[{source_table}#{source_pk}] {content_text}
{end}

=== Question ===
{question}

=== Answer ===
```

### 6.4 LLM call

`OpenAiLlmClient.complete(model="gpt-4o-mini", messages=[system=template header, user=question+context], temperature=0.1, max_tokens=400)`.

Timing: capture `latency_ms` end-to-end (extract + embed + search + LLM) and persist to `rag_query_log` along with `retrieved_ids` JSON and `user_id` (from `jwt.getClaim("sub")`).

---

## 7. API Endpoints

All mounted under `/api/v1/rag/`. Auth enforced via `@PreAuthorize` matching existing project convention.

| Method | Path | Auth | Request | Response |
|---|---|---|---|---|
| POST | `/api/v1/rag/chat` | `isAuthenticated()` | `{ "question": string }` | `{ "answer": string, "sources": [{ "sourceTable", "sourcePk", "score" }], "queryLogId": number }` |
| POST | `/api/v1/rag/feedback/{queryLogId}` | `isAuthenticated()` | `{ "feedback": "up" \| "down" }` | `204 No Content` |
| GET | `/api/v1/rag/health` | `hasRole('Admin')` | — | `{ "tables": [{ "name", "lastModifiedAt", "rowsSynced", "lastRunAt", "lastError" }], "qdrantOk": bool }` |
| POST | `/api/v1/rag/admin/resync` | `hasRole('Admin')` | — | `{ "clearedPoints": number, "resetTables": number }` |
| POST | `/api/v1/rag/admin/resync/{table}` | `hasRole('Admin')` | `table` ∈ {Materials, InventoryLots, ProductionBatches, BatchComponents, QCTests, InventoryTransactions} | `{ "table", "resetAt" }` |
| DELETE | `/api/v1/rag/admin/point/{table}/{pk}` | `hasRole('Admin')` | path params | `204 No Content` |

No rate limiter is added. Keycloak-authenticated user access is the gate; abuse mitigation is deferred to future work.

---

## 8. Configuration

### 8.1 New env vars (add to `backend/.env.example`)

```env
# ── Qdrant Cloud (cloud.qdrant.io free tier) ──────────────────────────────────
QDRANT_HOST=<cluster-id>.cloud.qdrant.io
QDRANT_PORT=6334
QDRANT_USE_TLS=true
QDRANT_API_KEY=
QDRANT_COLLECTION=ims_rag

# ── OpenAI (embedding + pre-filter + generation — single key) ────────────────
OPENAI_API_KEY=
OPENAI_EMBEDDING_MODEL=text-embedding-3-small
OPENAI_CHAT_MODEL=gpt-4o-mini

# ── RAG sync worker ───────────────────────────────────────────────────────────
RAG_SYNC_ENABLED=true
RAG_SYNC_INTERVAL_MS=60000
RAG_SYNC_BATCH_SIZE=500
```

### 8.2 `application.properties` additions

```properties
# RAG — sync worker
rag.sync.enabled=${RAG_SYNC_ENABLED:true}
rag.sync.interval-ms=${RAG_SYNC_INTERVAL_MS:60000}
rag.sync.batch-size=${RAG_SYNC_BATCH_SIZE:500}

# RAG — Qdrant Cloud
rag.qdrant.host=${QDRANT_HOST}
rag.qdrant.port=${QDRANT_PORT:6334}
rag.qdrant.use-tls=${QDRANT_USE_TLS:true}
rag.qdrant.api-key=${QDRANT_API_KEY}
rag.qdrant.collection=${QDRANT_COLLECTION:ims_rag}

# RAG — OpenAI
rag.openai.api-key=${OPENAI_API_KEY}
rag.openai.embedding-model=${OPENAI_EMBEDDING_MODEL:text-embedding-3-small}
rag.openai.chat-model=${OPENAI_CHAT_MODEL:gpt-4o-mini}
```

### 8.3 `build.gradle` additions

```gradle
dependencies {
    // ... existing ...

    // Qdrant Java client (gRPC). Verify latest on Maven Central before pinning.
    implementation 'io.qdrant:client:1.12.1'

    // OpenAI Java SDK. Flag: pin to current stable on Maven Central at install time;
    // the version line moves fast — 0.8.0 may be outdated by the time of install.
    implementation 'com.openai:openai-java:LATEST_STABLE'
}
```

**Compatibility notes**:
- Qdrant client uses gRPC (Netty). Spring Boot 4 already pulls Netty via Reactor — version conflicts are unlikely but possible; if resolver complains, pin Netty via `dependencyManagement { imports { mavenBom ... } }`.
- OpenAI Java SDK requires Java 8+, works on Java 21.
- Both artifacts are pure HTTP clients — no Spring AI dependency, no Spring Boot version coupling.

### 8.4 Code-side config

- `@EnableScheduling` added to `InventoryApplication` or the new `RagConfig`.
- `RagProperties` is a `@ConfigurationProperties("rag")` record binding the properties above.

---

## 9. Deployment Checklist

Each step marked `[MANUAL]` (human operator action) or `[AUTO]` (Spring Boot / Gradle / worker does it).

1. `[MANUAL]` Create Qdrant Cloud cluster (free tier), copy host + API key.
2. `[MANUAL]` Create OpenAI API key with a $10/month spend cap.
3. `[MANUAL]` Add `QDRANT_*` + `OPENAI_*` + `RAG_*` env vars to local `.env` and to Railway backend service.
4. `[MANUAL]` Run `V002__add_rag_sync_indexes.sql` on Aiven **staging**; verify `EXPLAIN SELECT ... ORDER BY modified_date` uses the new index.
5. `[MANUAL]` Run `V003__create_rag_system_tables.sql` on staging.
6. `[MANUAL]` Repeat steps 4–5 on Aiven **production**.
7. `[MANUAL]` Deploy backend with new `rag/` package (standard Gradle/Docker pipeline).
8. `[AUTO]` On startup, Spring creates the Qdrant collection `ims_rag` (vector size 1536, cosine) if missing.
9. `[AUTO]` Sync worker starts on `@Scheduled` interval, backfills from `1970-01-01` on first run, populates `rag_sync_state` + `rag_embed_log`, upserts to Qdrant.
10. `[MANUAL]` After first 10 minutes, check `GET /api/v1/rag/health` — confirm `rowsSynced` > 0 for all 6 tables and `lastError` is null.
11. `[MANUAL]` Inspect `rag_embed_log` for `status='FAILED'` rows; triage if any.
12. `[MANUAL]` Smoke test 10 Vietnamese questions via `POST /api/v1/rag/chat` (5 with specific entity like lot/material code, 5 open-ended).
13. `[MANUAL]` Verify `OPENAI_API_KEY` and `QDRANT_API_KEY` are not emitted by logs (check `spring.jpa.show-sql=true` does not dump config, and that Actuator `/env` is disabled or admin-only).
14. `[MANUAL]` Set OpenAI billing alert at $5 and $10.
15. `[MANUAL]` Set Qdrant Cloud collection size alert at 80% of 1GB.

**Rollback**: disable worker by setting `RAG_SYNC_ENABLED=false` and redeploying. Chat endpoint returns `"Không tìm thấy"` gracefully when collection is empty. Migrations V002/V003 are non-breaking and can remain in place.

---

## 10. Implementation Order

1. Write `V002` and `V003` SQL files into `02_Source/01_Source Code/database/migrations/`.
2. Update `backend/.env.example`, `application.properties`, and `build.gradle` with the new config keys and dependencies (pin exact artifact versions at install time).
3. Scaffold the `rag/` package: `RagProperties`, `RagConfig`, JPA entities (`RagSyncState`, `RagEmbedLog`, `RagQueryLog`) and their repositories — interfaces and stubs only.
4. Implement the six `RowSerializer` classes as pure functions with unit tests covering null FKs and formatting edge cases.
5. Implement `OpenAiEmbeddingClient`, `QdrantVectorStore`, and `SyncWorker`; smoke-test end-to-end against `Materials` first, then extend to the other five tables.
6. Implement query pipeline (`PrefilterExtractor`, `RagQueryService`, `PromptBuilder`, `OpenAiLlmClient`) and both controllers (`RagChatController`, `RagAdminController`).
7. Manual QA with Vietnamese test questions, then hand off to frontend for chat UI integration (out of scope for this plan).

---

**Estimated total OpenAI cost over project lifetime**: ~**$5–15** (one-time backfill of ~50k rows × ~200 tokens ≈ $0.20 for embeddings; ongoing incremental embeddings near zero at current data-change rate; chat queries at ~1k tokens in + 400 out on `gpt-4o-mini` ≈ $0.0003/query → $3/month at 300 queries/day). Stays well under the $10/month cap.
