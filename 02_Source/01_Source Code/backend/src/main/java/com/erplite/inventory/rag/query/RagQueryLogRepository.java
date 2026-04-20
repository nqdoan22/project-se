package com.erplite.inventory.rag.query;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RagQueryLogRepository extends JpaRepository<RagQueryLog, Long> {
}
