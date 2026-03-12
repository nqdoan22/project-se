package com.erplite.inventory.service;

import com.erplite.inventory.dto.common.PagedResponse;
import com.erplite.inventory.dto.material.MaterialRequest;
import com.erplite.inventory.dto.material.MaterialResponse;
import com.erplite.inventory.entity.Material;
import com.erplite.inventory.entity.Material.MaterialType;
import com.erplite.inventory.exception.BusinessException;
import com.erplite.inventory.exception.ResourceNotFoundException;
import com.erplite.inventory.repository.InventoryLotRepository;
import com.erplite.inventory.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock private MaterialRepository materialRepository;
    @Mock private InventoryLotRepository inventoryLotRepository;
    @InjectMocks private MaterialService materialService;

    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Material buildMaterial(String id) {
        return Material.builder()
                .materialId(id)
                .partNumber("PN-001")
                .materialName("Vitamin D3")
                .materialType(MaterialType.API)
                .storageConditions("2-8°C")
                .specificationDocument("SPEC-001")
                .build();
    }

    private MaterialRequest buildRequest(String partNumber) {
        MaterialRequest req = new MaterialRequest();
        req.setPartNumber(partNumber);
        req.setMaterialName("Vitamin D3");
        req.setMaterialType(MaterialType.API);
        req.setStorageConditions("2-8°C");
        return req;
    }

    // ── listMaterials ──────────────────────────────────────────────────────

    @Test
    void listMaterials_noFilter_callsFindAll() {
        Page<Material> page = new PageImpl<>(List.of(buildMaterial("m1")));
        when(materialRepository.findAll(PAGE)).thenReturn(page);

        PagedResponse<MaterialResponse> result = materialService.listMaterials(null, null, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findAll(PAGE);
    }

    @Test
    void listMaterials_keywordOnly_callsFindByName() {
        when(materialRepository.findByMaterialNameContainingIgnoreCase("vitamin", PAGE))
                .thenReturn(new PageImpl<>(List.of(buildMaterial("m1"))));

        PagedResponse<MaterialResponse> result = materialService.listMaterials("vitamin", null, PAGE);

        assertThat(result.getContent()).hasSize(1);
        verify(materialRepository).findByMaterialNameContainingIgnoreCase("vitamin", PAGE);
    }

    @Test
    void listMaterials_typeOnly_callsFindByType() {
        when(materialRepository.findByMaterialType(MaterialType.API, PAGE))
                .thenReturn(new PageImpl<>(List.of(buildMaterial("m1"))));

        materialService.listMaterials(null, MaterialType.API, PAGE);

        verify(materialRepository).findByMaterialType(MaterialType.API, PAGE);
    }

    @Test
    void listMaterials_keywordAndType_callsCombinedQuery() {
        when(materialRepository.findByMaterialTypeAndMaterialNameContainingIgnoreCase(
                MaterialType.API, "vit", PAGE))
                .thenReturn(new PageImpl<>(List.of()));

        materialService.listMaterials("vit", MaterialType.API, PAGE);

        verify(materialRepository)
                .findByMaterialTypeAndMaterialNameContainingIgnoreCase(MaterialType.API, "vit", PAGE);
    }

    @Test
    void listMaterials_blankKeyword_treatedAsNoKeyword() {
        when(materialRepository.findByMaterialType(MaterialType.API, PAGE))
                .thenReturn(new PageImpl<>(List.of()));

        materialService.listMaterials("   ", MaterialType.API, PAGE);

        verify(materialRepository).findByMaterialType(MaterialType.API, PAGE);
    }

    // ── getMaterialById ────────────────────────────────────────────────────

    @Test
    void getMaterialById_found_returnsMaterialResponse() {
        when(materialRepository.findById("m1")).thenReturn(Optional.of(buildMaterial("m1")));

        MaterialResponse result = materialService.getMaterialById("m1");

        assertThat(result.getMaterialId()).isEqualTo("m1");
        assertThat(result.getPartNumber()).isEqualTo("PN-001");
        assertThat(result.getMaterialType()).isEqualTo(MaterialType.API);
    }

    @Test
    void getMaterialById_notFound_throwsResourceNotFoundException() {
        when(materialRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.getMaterialById("x"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Material");
    }

    // ── createMaterial ─────────────────────────────────────────────────────

    @Test
    void createMaterial_success_savesAndReturnsMaterial() {
        MaterialRequest req = buildRequest("PN-002");
        Material saved = buildMaterial("m2");
        saved.setPartNumber("PN-002");

        when(materialRepository.existsByPartNumber("PN-002")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenReturn(saved);

        MaterialResponse result = materialService.createMaterial(req);

        assertThat(result.getMaterialId()).isEqualTo("m2");
        assertThat(result.getPartNumber()).isEqualTo("PN-002");
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    void createMaterial_duplicatePartNumber_throwsBusinessException() {
        MaterialRequest req = buildRequest("EXISTING");
        when(materialRepository.existsByPartNumber("EXISTING")).thenReturn(true);

        assertThatThrownBy(() -> materialService.createMaterial(req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("EXISTING");

        verify(materialRepository, never()).save(any());
    }

    // ── updateMaterial ─────────────────────────────────────────────────────

    @Test
    void updateMaterial_success_updatesFieldsAndSaves() {
        Material existing = buildMaterial("m1");
        MaterialRequest req = buildRequest("PN-001");
        req.setMaterialName("Updated Vitamin D3");
        req.setMaterialType(MaterialType.EXCIPIENT);

        when(materialRepository.findById("m1")).thenReturn(Optional.of(existing));
        when(materialRepository.existsByPartNumberAndMaterialIdNot("PN-001", "m1")).thenReturn(false);
        when(materialRepository.save(existing)).thenReturn(existing);

        MaterialResponse result = materialService.updateMaterial("m1", req);

        assertThat(result.getMaterialName()).isEqualTo("Updated Vitamin D3");
        assertThat(result.getMaterialType()).isEqualTo(MaterialType.EXCIPIENT);
    }

    @Test
    void updateMaterial_notFound_throwsResourceNotFoundException() {
        when(materialRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.updateMaterial("x", buildRequest("PN-001")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateMaterial_partNumberTakenByAnother_throwsBusinessException() {
        when(materialRepository.findById("m1")).thenReturn(Optional.of(buildMaterial("m1")));
        when(materialRepository.existsByPartNumberAndMaterialIdNot("TAKEN", "m1")).thenReturn(true);

        MaterialRequest req = buildRequest("TAKEN");

        assertThatThrownBy(() -> materialService.updateMaterial("m1", req))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("TAKEN");

        verify(materialRepository, never()).save(any());
    }

    // ── deleteMaterial ─────────────────────────────────────────────────────

    @Test
    void deleteMaterial_success_callsDeleteById() {
        when(materialRepository.findById("m1")).thenReturn(Optional.of(buildMaterial("m1")));
        when(inventoryLotRepository.existsByMaterial_MaterialId("m1")).thenReturn(false);

        materialService.deleteMaterial("m1");

        verify(materialRepository).deleteById("m1");
    }

    @Test
    void deleteMaterial_notFound_throwsResourceNotFoundException() {
        when(materialRepository.findById("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.deleteMaterial("x"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(materialRepository, never()).deleteById(any());
    }

    @Test
    void deleteMaterial_withExistingLots_throwsBusinessException() {
        when(materialRepository.findById("m1")).thenReturn(Optional.of(buildMaterial("m1")));
        when(inventoryLotRepository.existsByMaterial_MaterialId("m1")).thenReturn(true);

        assertThatThrownBy(() -> materialService.deleteMaterial("m1"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("existing inventory lots");

        verify(materialRepository, never()).deleteById(any());
    }
}
