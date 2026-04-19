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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryTransactionServiceTest {

    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private InventoryLotRepository lotRepository;
    @InjectMocks private InventoryTransactionService transactionService;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private InventoryLot buildLot(String id) {
        return InventoryLot.builder()
                .lotId(id)
                .manufacturerLot("MFG-001")
                .quantity(BigDecimal.valueOf(100))
                .unitOfMeasure("kg")
                .build();
    }

    private InventoryTransaction buildTransaction(String id, String lotId) {
        return InventoryTransaction.builder()
                .transactionId(id)
                .lot(buildLot(lotId))
                .transactionType(TransactionType.Receipt)
                .quantity(BigDecimal.valueOf(50))
                .unitOfMeasure("kg")
                .referenceId("REF-001")
                .notes("Initial receipt")
                .performedBy("user1")
                .transactionDate(LocalDateTime.now())
                .build();
    }

    private TransactionCreateRequest buildRequest(String lotId) {
        TransactionCreateRequest req = new TransactionCreateRequest();
        req.setLotId(lotId);
        req.setTransactionType("Receipt");
        req.setQuantity(BigDecimal.valueOf(50));
        req.setUnitOfMeasure("kg");
        req.setReferenceId("REF-001");
        req.setNotes("Receipt notes");
        req.setPerformedBy("user1");
        return req;
    }

    // ── listTransactions ───────────────────────────────────────────────────

    @Test
    void listTransactions_noLotId_callsFindAll() {
        Page<InventoryTransaction> page = new PageImpl<>(List.of(buildTransaction("t1", "lot1")));
        when(transactionRepository.findAll(PAGE)).thenReturn(page);

        PagedResponse<TransactionResponse> result = transactionService.listTransactions(null, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll(PAGE);
    }

    @Test
    void listTransactions_blankLotId_callsFindAll() {
        Page<InventoryTransaction> page = new PageImpl<>(List.of(buildTransaction("t1", "lot1")));
        when(transactionRepository.findAll(PAGE)).thenReturn(page);

        PagedResponse<TransactionResponse> result = transactionService.listTransactions("   ", PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(transactionRepository).findAll(PAGE);
    }

    @Test
    void listTransactions_withValidLotId_returnsTransactionsForLot() {
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(buildLot("lot1")));
        List<InventoryTransaction> txList = List.of(buildTransaction("t1", "lot1"));
        when(transactionRepository.findByLot_LotIdOrderByTransactionDateDesc("lot1"))
                .thenReturn(txList);

        PagedResponse<TransactionResponse> result = transactionService.listTransactions("lot1", PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(lotRepository).findById("lot1");
        verify(transactionRepository).findByLot_LotIdOrderByTransactionDateDesc("lot1");
    }

    @Test
    void listTransactions_lotNotFound_throwsResourceNotFoundException() {
        when(lotRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.listTransactions("invalid", PAGE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    @Test
    void listTransactions_pagination_appliesPagination() {
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(buildLot("lot1")));
        List<InventoryTransaction> allTxs = List.of(
                buildTransaction("t1", "lot1"),
                buildTransaction("t2", "lot1"),
                buildTransaction("t3", "lot1")
        );
        when(transactionRepository.findByLot_LotIdOrderByTransactionDateDesc("lot1"))
                .thenReturn(allTxs);

        PagedResponse<TransactionResponse> result = transactionService.listTransactions("lot1", PAGE);

        assertThat(result.getContent()).hasSize(3);
    }

    // ── getTransactionById ─────────────────────────────────────────────────

    @Test
    void getTransactionById_found_returnsTransactionResponse() {
        when(transactionRepository.findById("t1")).thenReturn(Optional.of(buildTransaction("t1", "lot1")));

        TransactionResponse result = transactionService.getTransactionById("t1");

        assertThat(result.getTransactionId()).isEqualTo("t1");
        assertThat(result.getQuantity()).isEqualTo(BigDecimal.valueOf(50));
    }

    @Test
    void getTransactionById_notFound_throwsResourceNotFoundException() {
        when(transactionRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryTransaction");
    }

    // ── createTransaction ──────────────────────────────────────────────────

    @Test
    void createTransaction_success_savesAndReturnsTransaction() {
        TransactionCreateRequest req = buildRequest("lot1");
        InventoryLot lot = buildLot("lot1");
        InventoryTransaction saved = buildTransaction("t1", "lot1");

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction(req);

        assertThat(result.getTransactionId()).isEqualTo("t1");
        assertThat(result.getTransactionType()).isEqualTo("Receipt");
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void createTransaction_lotNotFound_throwsResourceNotFoundException() {
        TransactionCreateRequest req = buildRequest("invalid");
        when(lotRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.createTransaction(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_invalidTransactionType_throwsBusinessException() {
        TransactionCreateRequest req = buildRequest("lot1");
        req.setTransactionType("InvalidType");
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(buildLot("lot1")));

        assertThatThrownBy(() -> transactionService.createTransaction(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Invalid transaction type");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_withCustomDate_usesProvidedDate() {
        TransactionCreateRequest req = buildRequest("lot1");
        LocalDateTime customDate = LocalDateTime.of(2025, 1, 15, 10, 30);
        req.setTransactionDate(customDate);
        InventoryTransaction saved = buildTransaction("t1", "lot1");
        saved.setTransactionDate(customDate);

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(buildLot("lot1")));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction(req);

        assertThat(result.getTransactionId()).isEqualTo("t1");
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    void createTransaction_nullDate_usesCurrentTime() {
        TransactionCreateRequest req = buildRequest("lot1");
        req.setTransactionDate(null); // Should use current time
        InventoryTransaction saved = buildTransaction("t1", "lot1");

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(buildLot("lot1")));
        when(transactionRepository.save(any(InventoryTransaction.class))).thenReturn(saved);

        TransactionResponse result = transactionService.createTransaction(req);

        assertThat(result.getTransactionId()).isEqualTo("t1");
        verify(transactionRepository).save(any(InventoryTransaction.class));
    }
}
