package com.erplite.inventory.service;

import com.erplite.inventory.dto.label.*;
import com.erplite.inventory.entity.InventoryLot;
import com.erplite.inventory.entity.LabelTemplate;
import com.erplite.inventory.entity.LabelTemplate.LabelType;
import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.ProductionBatch;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ConflictException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.InventoryLotRepository;
import com.erplite.inventory.repository.LabelTemplateRepository;
import com.erplite.inventory.repository.ProductionBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class LabelServiceTest {

    @Mock private LabelTemplateRepository templateRepository;
    @Mock private InventoryLotRepository lotRepository;
    @Mock private ProductionBatchRepository batchRepository;
    @InjectMocks private LabelService labelService;

    private LabelTemplate buildTemplate(String id) {
        return LabelTemplate.builder()
                .templateId(id)
                .templateName("Standard Label")
                .labelType(LabelType.FINISHED_PRODUCT)
                .templateContent("Batch: {{batchNumber}}, Exp: {{expirationDate}}")
                .width(BigDecimal.valueOf(100))
                .height(BigDecimal.valueOf(50))
                .build();
    }

    private LabelTemplateRequest buildTemplateRequest(String templateId) {
        LabelTemplateRequest req = new LabelTemplateRequest();
        req.setTemplateId(templateId);
        req.setTemplateName("Standard Label");
        req.setLabelType(LabelType.FINISHED_PRODUCT);
        req.setTemplateContent("Batch: {{batchNumber}}, Exp: {{expirationDate}}");
        req.setWidth(BigDecimal.valueOf(100));
        req.setHeight(BigDecimal.valueOf(50));
        return req;
    }

    private Material buildMaterial(String id) {
        return Material.builder()
                .materialId(id)
                .partNumber("PN-001")
                .materialName("Active Ingredient")
                .build();
    }

    private InventoryLot buildLot(String id, String materialId) {
        return InventoryLot.builder()
                .lotId(id)
                .manufacturerLot("MFG-001")
                .material(buildMaterial(materialId))
                .quantity(BigDecimal.valueOf(100))
                .unitOfMeasure("kg")
                .expirationDate(LocalDate.now().plusDays(365))
                .storageLocation("A-01-01")
                .build();
    }

    private ProductionBatch buildBatch(String id) {
        return ProductionBatch.builder()
                .batchId(id)
                .batchNumber("BATCH-001")
                .product(buildMaterial("mat1"))
                .batchSize(BigDecimal.valueOf(1000))
                .unitOfMeasure("kg")
                .expirationDate(LocalDate.now().plusDays(365))
                .build();
    }

    // ── listTemplates ──────────────────────────────────────────────────────

    @Test
    void listTemplates_noFilter_returnsAllTemplates() {
        List<LabelTemplate> templates = List.of(buildTemplate("t1"), buildTemplate("t2"));
        when(templateRepository.findAll()).thenReturn(templates);

        List<LabelTemplateResponse> result = labelService.listTemplates(null);

        assertThat(result).hasSize(2);
        verify(templateRepository).findAll();
    }

    @Test
    void listTemplates_byType_returnTemplatesOfType() {
        List<LabelTemplate> templates = List.of(buildTemplate("t1"));
        when(templateRepository.findByLabelType(LabelType.FINISHED_PRODUCT)).thenReturn(templates);

        List<LabelTemplateResponse> result = labelService.listTemplates(LabelType.FINISHED_PRODUCT);

        assertThat(result).hasSize(1);
        verify(templateRepository).findByLabelType(LabelType.FINISHED_PRODUCT);
    }

    @Test
    void listTemplates_byTypeEmpty_returnsEmptyList() {
        when(templateRepository.findByLabelType(LabelType.RAW_MATERIAL)).thenReturn(List.of());

        List<LabelTemplateResponse> result = labelService.listTemplates(LabelType.RAW_MATERIAL);

        assertThat(result).isEmpty();
    }

    // ── getTemplateById ────────────────────────────────────────────────────

    @Test
    void getTemplateById_found_returnsTemplate() {
        when(templateRepository.findById("t1")).thenReturn(Optional.of(buildTemplate("t1")));

        LabelTemplateResponse result = labelService.getTemplateById("t1");

        assertThat(result.getTemplateId()).isEqualTo("t1");
        assertThat(result.getTemplateName()).isEqualTo("Standard Label");
    }

    @Test
    void getTemplateById_notFound_throwsResourceNotFoundException() {
        when(templateRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.getTemplateById("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("LabelTemplate");
    }

    // ── createTemplate ─────────────────────────────────────────────────────

    @Test
    void createTemplate_success_savesAndReturnsTemplate() {
        LabelTemplateRequest req = buildTemplateRequest("t1");
        LabelTemplate saved = buildTemplate("t1");

        when(templateRepository.existsByTemplateId("t1")).thenReturn(false);
        when(templateRepository.save(any(LabelTemplate.class))).thenReturn(saved);

        LabelTemplateResponse result = labelService.createTemplate(req);

        assertThat(result.getTemplateId()).isEqualTo("t1");
        assertThat(result.getTemplateName()).isEqualTo("Standard Label");
        verify(templateRepository).save(any(LabelTemplate.class));
    }

    @Test
    void createTemplate_duplicateId_throwsConflictException() {
        LabelTemplateRequest req = buildTemplateRequest("EXISTING");
        when(templateRepository.existsByTemplateId("EXISTING")).thenReturn(true);

        assertThatThrownBy(() -> labelService.createTemplate(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("EXISTING");

        verify(templateRepository, never()).save(any());
    }

    // ── updateTemplate ─────────────────────────────────────────────────────

    @Test
    void updateTemplate_success_updatesAndSaves() {
        LabelTemplate existing = buildTemplate("t1");
        LabelTemplateRequest req = buildTemplateRequest("t1");
        req.setTemplateName("Updated Label");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(existing));
        when(templateRepository.save(existing)).thenReturn(existing);

        LabelTemplateResponse result = labelService.updateTemplate("t1", req);

        assertThat(result.getTemplateId()).isEqualTo("t1");
        verify(templateRepository).save(existing);
    }

    @Test
    void updateTemplate_notFound_throwsResourceNotFoundException() {
        when(templateRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.updateTemplate("x", buildTemplateRequest("x")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteTemplate ─────────────────────────────────────────────────────

    @Test
    void deleteTemplate_success_deletesTemplate() {
        when(templateRepository.findById("t1")).thenReturn(Optional.of(buildTemplate("t1")));

        labelService.deleteTemplate("t1");

        verify(templateRepository).deleteById("t1");
    }

    @Test
    void deleteTemplate_notFound_throwsResourceNotFoundException() {
        when(templateRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.deleteTemplate("x"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(templateRepository, never()).deleteById(any());
    }

    // ── generateLabel ──────────────────────────────────────────────────────

    @Test
    void generateLabel_fromLot_replacesPlaceholders() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(LabelGenerateRequest.SourceType.LOT);
        req.setSourceId("lot1");

        LabelTemplate template = buildTemplate("t1");
        template.setTemplateContent("Lot: {{lotId}}, Material: {{materialName}}, Exp: {{expirationDate}}");
        InventoryLot lot = buildLot("lot1", "mat1");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));

        LabelGenerateResponse result = labelService.generateLabel(req);

        assertThat(result.getRenderedContent()).contains("lot1");
        assertThat(result.getRenderedContent()).contains("Active Ingredient");
        verify(lotRepository).findById("lot1");
    }

    @Test
    void generateLabel_fromBatch_replacesPlaceholders() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(LabelGenerateRequest.SourceType.BATCH);
        req.setSourceId("batch1");

        LabelTemplate template = buildTemplate("t1");
        template.setTemplateContent("Batch: {{batchNumber}}, Product: {{productName}}, Size: {{batchSize}}");
        ProductionBatch batch = buildBatch("batch1");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));
        when(batchRepository.findById("batch1")).thenReturn(Optional.of(batch));

        LabelGenerateResponse result = labelService.generateLabel(req);

        assertThat(result.getRenderedContent()).contains("BATCH-001");
        assertThat(result.getRenderedContent()).contains("Active Ingredient");
        verify(batchRepository).findById("batch1");
    }

    @Test
    void generateLabel_lotNotFound_throwsResourceNotFoundException() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(LabelGenerateRequest.SourceType.LOT);
        req.setSourceId("invalid");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(buildTemplate("t1")));
        when(lotRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.generateLabel(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("InventoryLot");
    }

    @Test
    void generateLabel_batchNotFound_throwsResourceNotFoundException() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(LabelGenerateRequest.SourceType.BATCH);
        req.setSourceId("invalid");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(buildTemplate("t1")));
        when(batchRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.generateLabel(req))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProductionBatch");
    }

    @Test
    void generateLabel_templateNotFound_throwsResourceNotFoundException() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("invalid");
        req.setSourceType(LabelGenerateRequest.SourceType.LOT);
        req.setSourceId("lot1");

        when(templateRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> labelService.generateLabel(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void generateLabel_unsupportedSourceType_throwsBusinessException() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(null); // Invalid source type
        req.setSourceId("id1");

        when(templateRepository.findById("t1")).thenReturn(Optional.of(buildTemplate("t1")));

        assertThatThrownBy(() -> labelService.generateLabel(req))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void generateLabel_withNullValues_handlesGracefully() {
        LabelGenerateRequest req = new LabelGenerateRequest();
        req.setTemplateId("t1");
        req.setSourceType(LabelGenerateRequest.SourceType.LOT);
        req.setSourceId("lot1");

        LabelTemplate template = buildTemplate("t1");
        template.setTemplateContent("Lot: {{lotId}}, Notes: {{notes}}");
        InventoryLot lot = buildLot("lot1", "mat1");
        lot.setStorageLocation(null); // Null value

        when(templateRepository.findById("t1")).thenReturn(Optional.of(template));
        when(lotRepository.findById("lot1")).thenReturn(Optional.of(lot));

        LabelGenerateResponse result = labelService.generateLabel(req);

        assertThat(result.getRenderedContent()).isNotNull();
        assertThat(result.getGeneratedAt()).isNotNull();
    }
}
