package com.erplite.inventory.controller;

import com.erplite.inventory.dto.qc.QCTestRequest;
import com.erplite.inventory.dto.qc.QCTestResponse;
import com.erplite.inventory.dto.qc.QCTestSummaryResponse;
import com.erplite.inventory.service.QCTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/qctests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QCTestController {

    private final QCTestService qcTestService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QCTestResponse>> listTests(@RequestParam String lotId) {
        return ResponseEntity.ok(qcTestService.getTestsForLot(lotId));
    }

    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QCTestSummaryResponse> getSummary(@RequestParam String lotId) {
        return ResponseEntity.ok(qcTestService.getSummary(lotId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QCTestResponse> getTest(@PathVariable String id) {
        return ResponseEntity.ok(qcTestService.getTestById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','QualityControl')")
    public ResponseEntity<QCTestResponse> createTest(
            @Valid @RequestBody QCTestRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(qcTestService.createTest(req, jwt != null ? jwt.getClaimAsString("preferred_username") : null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','QualityControl')")
    public ResponseEntity<QCTestResponse> updateTest(
            @PathVariable String id,
            @Valid @RequestBody QCTestRequest req) {
        return ResponseEntity.ok(qcTestService.updateTest(id, req));
    }
}
