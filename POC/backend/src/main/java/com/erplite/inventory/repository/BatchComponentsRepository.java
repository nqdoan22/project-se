package com.erplite.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.erplite.inventory.entity.BatchComponents;

@Repository
public interface BatchComponentsRepository extends JpaRepository<BatchComponents, Long> {
}