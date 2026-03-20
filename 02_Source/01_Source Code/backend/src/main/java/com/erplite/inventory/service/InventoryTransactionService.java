package com.erplite.inventory.service;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.transaction.TransactionCreateRequest;
import com.erplite.inventory.dto.transaction.TransactionResponse;
import com.erplite.inventory.entity.InventoryLot;
import com.erplite.inventory.entity.InventoryTransaction;
import com.erplite.inventory.entity.InventoryTransaction.TransactionType;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.InventoryLotRepository;
import com.erplite.inventory.repository.InventoryTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final InventoryLotRepository lotRepository;

    public PagedResponse<TransactionResponse> listTransactions(String lotId, Pageable pageable) {
        Page<InventoryTransaction> page;
        
        if (lotId != null && !lotId.isBlank()) {
            // Verify lot exists
            lotRepository.findById(lotId)
                .orElseThrow(() -> new ResourceNotFoundException("InventoryLot", "id", lotId));
            
            List<InventoryTransaction> all = transactionRepository.findByLot_LotIdOrderByTransactionDateDesc(lotId);
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), all.size());
            
            if (start > all.size()) {
                page = new PageImpl<>(List.of(), pageable, all.size());
            } else {
                page = new PageImpl<>(all.subList(start, end), pageable, all.size());
            }
        } else {
            page = transactionRepository.findAll(pageable);
        }
        
        return PagedResponse.from(page.map(TransactionResponse::from));
    }

    public TransactionResponse getTransactionById(String id) {
        InventoryTransaction tx = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("InventoryTransaction", "id", id));
        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest req) {
        InventoryLot lot = lotRepository.findById(req.getLotId())
            .orElseThrow(() -> new ResourceNotFoundException("InventoryLot", "id", req.getLotId()));

        // Validate transaction type
        try {
            TransactionType.valueOf(req.getTransactionType());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid transaction type: " + req.getTransactionType());
        }

        InventoryTransaction tx = InventoryTransaction.builder()
            .lot(lot)
            .transactionType(TransactionType.valueOf(req.getTransactionType()))
            .quantity(req.getQuantity())
            .unitOfMeasure(req.getUnitOfMeasure())
            .referenceId(req.getReferenceId())
            .notes(req.getNotes())
            .performedBy(req.getPerformedBy())
            .transactionDate(req.getTransactionDate() != null ? req.getTransactionDate() : LocalDateTime.now())
            .build();

        tx = transactionRepository.save(tx);
        return TransactionResponse.from(tx);
    }
}
