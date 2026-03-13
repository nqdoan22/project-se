package com.erplite.inventory.service;

import com.erplite.inventory.dto.qc.QCTestRequest;
import com.erplite.inventory.dto.qc.QCTestResponse;
import com.erplite.inventory.dto.qc.QCTestSummaryResponse;
import com.erplite.inventory.entity.InventoryLot;
import com.erplite.inventory.entity.InventoryLot.LotStatus;
import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.Material.MaterialType;
import com.erplite.inventory.entity.QCTest;
import com.erplite.inventory.entity.QCTest.ResultStatus;
import com.erplite.inventory.entity.QCTest.TestType;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.InventoryLotRepository;
import com.erplite.inventory.repository.QCTestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class QCTestServiceTest {

    @Mock private QCTestRepository qcTestRepository;
    @Mock private InventoryLotRepository lotRepository;
    @InjectMocks private QCTestService qcTestService;

    private InventoryLot buildLot(String id, LotStatus status) {
        return InventoryLot.builder()
                .lotId(id)
                .material(Material.builder().materialId("mat1").partNumber("PN-001")
                        .materialName("Vitamin D3").materialType(MaterialType.API).build())
                .manufacturerLot("LOT-001")
                .quantity(new BigDecimal("10.0"))
                .unitOfMeasure("kg")
                .status(status)
                .receivedDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusYears(2))
                .isSample(false)
                .build();
    }

    private QCTest buildTest(String id, InventoryLot lot, ResultStatus status) {
        return QCTest.builder()
                .testId(id)
                .lot(lot)
                .testType(TestType.IDENTITY)
                .testMethod("HPLC-UV")
                .testDate(LocalDate.now())
                .testResult("Conforms to specification")
                .acceptanceCriteria("NLT 99.0%")
                .resultStatus(status)
                .performedBy("qc_analyst")
                .verifiedBy("admin")
                .build();
    }

    // ── getTestsForLot ─────────────────────────────────────────────────────

    @Test
    void getTestsForLot_returnsMappedResponsesOrderedByDate() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        List<QCTest> tests = List.of(
                buildTest("t1", lot, ResultStatus.Pass),
                buildTest("t2", lot, ResultStatus.Pending)
        );
        when(qcTestRepository.findByLot_LotIdOrderByTestDateDesc("lot1")).thenReturn(tests);

        List<QCTestResponse> result = qcTestService.getTestsForLot("lot1");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTestId()).isEqualTo("t1");
    }

    @Test
    void getTestsForLot_noTests_returnsEmptyList() {
        when(qcTestRepository.findByLot_LotIdOrderByTestDateDesc("lot1")).thenReturn(List.of());

        List<QCTestResponse> result = qcTestService.getTestsForLot("lot1");

        assertThat(result).isEmpty();
    }

    // ── getSummary ─────────────────────────────────────────────────────────

    @Test
    void getSummary_correctlyCounts_passFailPendingTests() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        List<QCTest> tests = List.of(
                buildTest("t1", lot, ResultStatus.Pass),
                buildTest("t2", lot, ResultStatus.Pass),
                buildTest("t3", lot, ResultStatus.Fail),
                buildTest("t4", lot, ResultStatus.Pending)
        );

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(qcTestRepository.findByLot_LotId("lot1")).thenReturn(tests);

        QCTestSummaryResponse summary = qcTestService.getSummary("lot1");

        assertThat(summary.getLotId()).isEqualTo("lot1");
        assertThat(summary.getLotStatus()).isEqualTo("Quarantine");
        assertThat(summary.getTotalTests()).isEqualTo(4);
        assertThat(summary.getPassed()).isEqualTo(2);
        assertThat(summary.getFailed()).isEqualTo(1);
        assertThat(summary.getPending()).isEqualTo(1);
    }

    @Test
    void getSummary_noTests_returnsAllCountsZero() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(qcTestRepository.findByLot_LotId("lot1")).thenReturn(List.of());

        QCTestSummaryResponse summary = qcTestService.getSummary("lot1");

        assertThat(summary.getTotalTests()).isEqualTo(0);
        assertThat(summary.getPassed()).isEqualTo(0);
        assertThat(summary.getFailed()).isEqualTo(0);
        assertThat(summary.getPending()).isEqualTo(0);
    }

    @Test
    void getSummary_lotNotFound_throwsResourceNotFoundException() {
        when(lotRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qcTestService.getSummary("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    // ── getTestById ────────────────────────────────────────────────────────

    @Test
    void getTestById_found_returnsResponse() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        QCTest test = buildTest("t1", lot, ResultStatus.Pass);
        when(qcTestRepository.findById("t1")).thenReturn(Optional.of(test));

        QCTestResponse result = qcTestService.getTestById("t1");

        assertThat(result.getTestId()).isEqualTo("t1");
        assertThat(result.getResultStatus()).isEqualTo(ResultStatus.Pass);
    }

    @Test
    void getTestById_notFound_throwsResourceNotFoundException() {
        when(qcTestRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qcTestService.getTestById("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("QCTest");
    }

    // ── createTest ─────────────────────────────────────────────────────────

    @Test
    void createTest_success_savesTestWithProvidedFields() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        QCTestRequest req = new QCTestRequest();
        req.setLotId("lot1");
        req.setTestType(TestType.IDENTITY);
        req.setTestMethod("HPLC-UV");
        req.setTestDate(LocalDate.now());
        req.setTestResult("Conforms");
        req.setAcceptanceCriteria("NLT 99.0%");
        req.setResultStatus(ResultStatus.Pass);
        req.setPerformedBy("qc_analyst");
        req.setVerifiedBy("admin");

        QCTest saved = buildTest("t1", lot, ResultStatus.Pass);

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(qcTestRepository.save(any(QCTest.class))).thenReturn(saved);

        QCTestResponse result = qcTestService.createTest(req, "qc_analyst");

        assertThat(result.getTestId()).isEqualTo("t1");
        assertThat(result.getResultStatus()).isEqualTo(ResultStatus.Pass);
    }

    @Test
    void createTest_usesCallerUsernameWhenPerformedByIsNull() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        QCTestRequest req = new QCTestRequest();
        req.setLotId("lot1");
        req.setTestType(TestType.MICROBIAL);
        req.setTestMethod("USP <61>");
        req.setTestDate(LocalDate.now());
        req.setTestResult("Pass");
        req.setResultStatus(ResultStatus.Pass);
        req.setPerformedBy(null);

        QCTest saved = buildTest("t1", lot, ResultStatus.Pass);
        saved.setPerformedBy("jwt_user");

        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));
        when(qcTestRepository.save(any(QCTest.class))).thenReturn(saved);

        qcTestService.createTest(req, "jwt_user");

        ArgumentCaptor<QCTest> captor = ArgumentCaptor.forClass(QCTest.class);
        verify(qcTestRepository).save(captor.capture());
        assertThat(captor.getValue().getPerformedBy()).isEqualTo("jwt_user");
    }

    @Test
    void createTest_lotNotFound_throwsResourceNotFoundException() {
        QCTestRequest req = new QCTestRequest();
        req.setLotId("missing");
        when(lotRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qcTestService.createTest(req, "qc_analyst"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    // ── updateTest ─────────────────────────────────────────────────────────

    @Test
    void updateTest_sameLot_updatesFieldsWithoutReloadingLot() {
        InventoryLot lot = buildLot("lot1", LotStatus.Quarantine);
        QCTest existing = buildTest("t1", lot, ResultStatus.Pending);

        QCTestRequest req = new QCTestRequest();
        req.setLotId("lot1");
        req.setTestType(TestType.POTENCY);
        req.setTestMethod("UV-Spec");
        req.setTestDate(LocalDate.now());
        req.setTestResult("100,500 IU/g");
        req.setAcceptanceCriteria("NLT 95,000 IU/g");
        req.setResultStatus(ResultStatus.Pass);
        req.setPerformedBy("qc_analyst");
        req.setVerifiedBy("admin");

        QCTest saved = buildTest("t1", lot, ResultStatus.Pass);
        saved.setTestType(TestType.POTENCY);

        when(qcTestRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(qcTestRepository.save(existing)).thenReturn(saved);

        QCTestResponse result = qcTestService.updateTest("t1", req);

        assertThat(result.getResultStatus()).isEqualTo(ResultStatus.Pass);
        verify(lotRepository, never()).findById(any());
    }

    @Test
    void updateTest_differentLot_reloadsNewLot() {
        InventoryLot originalLot = buildLot("lot1", LotStatus.Quarantine);
        InventoryLot newLot = buildLot("lot2", LotStatus.Quarantine);
        QCTest existing = buildTest("t1", originalLot, ResultStatus.Pending);

        QCTestRequest req = new QCTestRequest();
        req.setLotId("lot2");
        req.setTestType(TestType.IDENTITY);
        req.setTestMethod("HPLC-UV");
        req.setTestDate(LocalDate.now());
        req.setTestResult("Pass");
        req.setResultStatus(ResultStatus.Pass);
        req.setPerformedBy("qc_analyst");

        QCTest saved = buildTest("t1", newLot, ResultStatus.Pass);

        when(qcTestRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(lotRepository.findById("lot2")).thenReturn(Optional.of(newLot));
        when(qcTestRepository.save(existing)).thenReturn(saved);

        qcTestService.updateTest("t1", req);

        assertThat(existing.getLot().getLotId()).isEqualTo("lot2");
    }

    @Test
    void updateTest_notFound_throwsResourceNotFoundException() {
        when(qcTestRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> qcTestService.updateTest("x", new QCTestRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("QCTest");
    }
}
