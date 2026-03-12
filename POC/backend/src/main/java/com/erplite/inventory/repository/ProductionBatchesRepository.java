package com.erplite.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erplite.inventory.entity.ProductionBatches;

@Repository
public interface ProductionBatchesRepository extends JpaRepository<ProductionBatches, Long> {
}