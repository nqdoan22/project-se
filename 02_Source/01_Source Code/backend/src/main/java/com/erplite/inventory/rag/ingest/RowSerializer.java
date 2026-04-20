package com.erplite.inventory.rag.ingest;

import java.util.Map;

public interface RowSerializer<T> {

    /** Logical table name used in Qdrant payload + rag_sync_state / rag_embed_log. */
    String sourceTable();

    /** Entity class handled by this serializer. */
    Class<T> entityClass();

    /** JPQL field name of the primary key (used in cursor queries). */
    String pkField();

    /** JPQL field name of the cursor column (modified_date or created_date). */
    String cursorField();

    /** Extract the primary key value from the entity. */
    String pk(T entity);

    /** Extract the cursor timestamp value from the entity. */
    java.time.LocalDateTime cursor(T entity);

    /** Build the human-readable content text that will be embedded. */
    String content(T entity);

    /** Build the Qdrant payload map (filter fields + source_table + source_pk + content). */
    Map<String, Object> payload(T entity);
}
