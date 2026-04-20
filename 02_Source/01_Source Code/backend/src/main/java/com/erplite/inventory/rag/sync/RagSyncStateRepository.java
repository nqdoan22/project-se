package com.erplite.inventory.rag.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RagSyncStateRepository extends JpaRepository<RagSyncState, String> {
}
