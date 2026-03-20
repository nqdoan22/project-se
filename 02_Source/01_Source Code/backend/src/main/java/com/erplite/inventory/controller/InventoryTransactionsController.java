package com.erplite.inventory.controller;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.transaction.TransactionCreateRequest;
import com.erplite.inventory.dto.transaction.TransactionResponse;
import com.erplite.inventory.service.InventoryTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryTransactionsController {

    private final InventoryTransactionService transactionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<TransactionResponse>> listTransactions(
            @RequestParam(required = false) String lotId,
            @PageableDefault(size = 20, sort = "transactionDate") Pageable pageable) {
        return ResponseEntity.ok(transactionService.listTransactions(lotId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','QualityControl')")
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionCreateRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(req));
    }
}
