-- V002__add_rag_sync_indexes.sql
-- Support incremental sync cursor queries for the RAG pipeline.
-- Non-breaking: index-only change. MySQL 8.0+. Run once.

ALTER TABLE Materials             ADD INDEX idx_materials_modified (modified_date);
ALTER TABLE InventoryLots         ADD INDEX idx_lots_modified (modified_date);
ALTER TABLE ProductionBatches     ADD INDEX idx_batches_modified (modified_date);
ALTER TABLE BatchComponents       ADD INDEX idx_components_modified (modified_date);
ALTER TABLE QCTests               ADD INDEX idx_qc_modified (modified_date);
ALTER TABLE InventoryTransactions ADD INDEX idx_txn_created (created_date);
