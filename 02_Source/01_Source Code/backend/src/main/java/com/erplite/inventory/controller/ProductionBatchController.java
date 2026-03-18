package com.erplite.inventory.controller;

import com.erplite.inventory.dto.batch.*;
import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.entity.ProductionBatch.BatchStatus;
import com.erplite.inventory.service.ProductionBatchService;
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
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductionBatchController {

    private final ProductionBatchService batchService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<BatchResponse>> listBatches(
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) String productId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(batchService.listBatches(status, productId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BatchResponse> getBatch(@PathVariable String id) {
        return ResponseEntity.ok(batchService.getBatchById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','Production')")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody BatchCreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(req));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('Admin','Production')")
    public ResponseEntity<BatchResponse> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody BatchStatusRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(batchService.updateBatchStatus(id, req));
    }

    @PostMapping("/{id}/components")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','Production')")
    public ResponseEntity<ComponentResponse> addComponent(
            @PathVariable String id,
            @Valid @RequestBody BatchComponentRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getAddedBy() == null && jwt != null) {
            req.setAddedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.addComponent(id, req));
    }

    @PatchMapping("/components/{componentId}/confirm")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','Production')")
    public ResponseEntity<ComponentResponse> confirmComponent(
            @PathVariable String componentId,
            @Valid @RequestBody ComponentConfirmRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getPerformedBy() == null && jwt != null) {
            req.setPerformedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(batchService.confirmComponent(componentId, req));
    }

    @PatchMapping("/components/{componentId}")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','Production')")
    public ResponseEntity<ComponentResponse> updateComponent(
            @PathVariable String componentId,
            @Valid @RequestBody BatchComponentRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        if (req.getAddedBy() == null && jwt != null) {
            req.setAddedBy(jwt.getClaimAsString("preferred_username"));
        }
        return ResponseEntity.ok(batchService.updateComponent(componentId, req));
    }

    @DeleteMapping("/components/{componentId}")
    @PreAuthorize("hasAnyRole('Admin','InventoryManager','Production')")
    public ResponseEntity<Void> deleteComponent(@PathVariable String componentId) {
        batchService.deleteComponent(componentId);
        return ResponseEntity.noContent().build();
    }
}
