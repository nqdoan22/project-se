-- V003__create_rag_system_tables.sql
-- System tables for RAG bookkeeping: sync cursor, embed audit, query log.

CREATE TABLE IF NOT EXISTS rag_sync_state (
    source_table     VARCHAR(64)  NOT NULL PRIMARY KEY,
    last_modified_at DATETIME     NOT NULL DEFAULT '1970-01-01 00:00:00',
    last_pk          VARCHAR(36)  NULL,
    rows_synced      BIGINT       NOT NULL DEFAULT 0,
    last_run_at      DATETIME     NULL,
    last_error       TEXT         NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rag_embed_log (
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    source_table   VARCHAR(64)  NOT NULL,
    source_pk      VARCHAR(36)  NOT NULL,
    content_hash   CHAR(64)     NOT NULL,
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
    user_id        VARCHAR(36)  NULL,
    question       TEXT         NOT NULL,
    retrieved_ids  JSON         NULL,
    answer         TEXT         NULL,
    latency_ms     INT          NULL,
    feedback       ENUM('up','down') NULL,
    asked_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_query_user (user_id, asked_at),
    INDEX idx_query_time (asked_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
