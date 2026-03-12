package com.erplite.inventory.controller;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.lot.*;
import com.erplite.inventory.dto.transaction.TransactionResponse;
import com.erplite.inventory.entity.InventoryLot.LotStatus;
import com.erplite.inventory.service.InventoryLotService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/lots")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryLotController {

    private final InventoryLotService lotService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<LotResponse>> listLots(
            @RequestParam(required = false) String materialId,
            @RequestParam(required = false) LotStatus status,
            @RequestParam(required = false) Boolean nearExpiry,
            @RequestParam(required = false) Boolean isSample,
            @PageableDefault(size = 20, sort = "receivedDate") Pageable pageable) {
        return ResponseEntity.ok(lotService.listLots(materialId, status, nearExpiry, isSample, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LotResponse> getLot(@PathVariable String id) {
        return ResponseEntity.ok(lotService.getLotById(id));
    }

    @PostMapping("/receive")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager')")
    public ResponseEntity<LotResponse> receiveLot(
            @Valid @RequestBody LotReceiveRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.receiveLot(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager')")
    public ResponseEntity<LotResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody LotStatusUpdateRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(lotService.updateLotStatus(id, req));
    }

    @PostMapping("/{id}/split")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','QualityControl')")
    public ResponseEntity<LotResponse> splitLot(
            @PathVariable String id,
            @Valid @RequestBody LotSplitRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(lotService.splitLot(id, req));
    }

    @PostMapping("/{id}/adjust")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager')")
    public ResponseEntity<LotResponse> adjustLot(
            @PathVariable String id,
            @Valid @RequestBody LotAdjustRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(lotService.adjustLot(id, req));
    }

    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager')")
    public ResponseEntity<LotResponse> transferLot(
            @PathVariable String id,
            @Valid @RequestBody LotTransferRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(lotService.transferLot(id, req));
    }

    @PostMapping("/{id}/dispose")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager')")
    public ResponseEntity<LotResponse> disposeLot(
            @PathVariable String id,
            @Valid @RequestBody LotDisposeRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(lotService.disposeLot(id, req));
    }

    @GetMapping("/{lotId}/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @PathVariable String lotId,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(lotService.getTransactions(lotId, type));
    }
}
