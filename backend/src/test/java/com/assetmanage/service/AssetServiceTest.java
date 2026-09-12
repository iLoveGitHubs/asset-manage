package com.assetmanage.service;

import com.assetmanage.dto.AssetDTO;
import com.assetmanage.entity.Asset;
import com.assetmanage.entity.AssetStatus;
import com.assetmanage.exception.ResourceNotFoundException;
import com.assetmanage.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class AssetServiceTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    private AssetDTO newAssetDTO(String code) {
        AssetDTO dto = new AssetDTO();
        dto.setName("Asset " + code);
        dto.setCode(code);
        dto.setStatus(AssetStatus.AVAILABLE);
        dto.setPurchaseDate(LocalDate.of(2022, 1, 1));
        dto.setPurchaseValue(new BigDecimal("1000.00"));
        dto.setCurrentValue(new BigDecimal("1000.00"));
        dto.setCategoryId(1L);
        dto.setUsefulLifeYears(5);
        return dto;
    }

    @Test
    void create_validData_returnsAssetWithId() {
        AssetDTO dto = newAssetDTO("SVC-CREATE-001");

        Asset created = assetService.create(dto);

        assertNotNull(created.getId());
        assertEquals("SVC-CREATE-001", created.getCode());
        assertEquals("Asset SVC-CREATE-001", created.getName());
        assertEquals(AssetStatus.AVAILABLE, created.getStatus());
    }

    @Test
    void create_duplicateCode_throwsException() {
        AssetDTO first = newAssetDTO("SVC-DUP-001");
        assetService.create(first);

        AssetDTO second = newAssetDTO("SVC-DUP-001");

        assertThrows(IllegalStateException.class, () -> assetService.create(second));
    }

    @Test
    void findById_validId_returnsAsset() {
        AssetDTO dto = newAssetDTO("SVC-FIND-001");
        Asset created = assetService.create(dto);

        Asset found = assetService.findById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("SVC-FIND-001", found.getCode());
        assertEquals("Asset SVC-FIND-001", found.getName());
    }

    @Test
    void findById_invalidId_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> assetService.findById(999999L));
    }

    @Test
    void update_asset_fieldsUpdatedCorrectly() {
        AssetDTO createDto = newAssetDTO("SVC-UPD-001");
        Asset created = assetService.create(createDto);

        AssetDTO updateDto = newAssetDTO("SVC-UPD-001");
        updateDto.setName("Updated Asset Name");
        updateDto.setPurchaseValue(new BigDecimal("1500.00"));
        updateDto.setCurrentValue(new BigDecimal("1500.00"));
        updateDto.setStatus(AssetStatus.IN_USE);

        Asset updated = assetService.update(created.getId(), updateDto);

        assertEquals("Updated Asset Name", updated.getName());
        assertEquals(0, new BigDecimal("1500.00").compareTo(updated.getPurchaseValue()));
        assertEquals(AssetStatus.IN_USE, updated.getStatus());
    }

    @Test
    void delete_asset_removedFromRepository() {
        AssetDTO dto = newAssetDTO("SVC-DEL-001");
        Asset created = assetService.create(dto);
        Long id = created.getId();
        assertTrue(assetRepository.existsById(id));

        assetService.delete(id);

        assertFalse(assetRepository.existsById(id));
    }

    @Test
    void calculateDepreciation_correctCurrentValue() {
        AssetDTO dto = newAssetDTO("SVC-DEP-001");
        LocalDate purchaseDate = LocalDate.of(2020, 1, 1);
        BigDecimal purchaseValue = new BigDecimal("1000.00");
        int usefulLife = 5;
        dto.setPurchaseDate(purchaseDate);
        dto.setPurchaseValue(purchaseValue);
        dto.setCurrentValue(purchaseValue);
        dto.setUsefulLifeYears(usefulLife);

        Asset created = assetService.create(dto);
        Map<String, Object> result = assetService.calculateDepreciation(created.getId());

        int yearsElapsed = Period.between(purchaseDate, LocalDate.now()).getYears();
        if (yearsElapsed < 0) {
            yearsElapsed = 0;
        }
        if (yearsElapsed > usefulLife) {
            yearsElapsed = usefulLife;
        }

        BigDecimal expectedAccumulated = purchaseValue
                .multiply(BigDecimal.valueOf(yearsElapsed))
                .divide(BigDecimal.valueOf(usefulLife), 2, RoundingMode.HALF_UP);
        BigDecimal expectedCurrent = purchaseValue.subtract(expectedAccumulated);

        BigDecimal actualCurrent = (BigDecimal) result.get("currentValue");
        BigDecimal actualAccumulated = (BigDecimal) result.get("accumulatedDepreciation");

        assertEquals(0, expectedCurrent.compareTo(actualCurrent));
        assertEquals(0, expectedAccumulated.compareTo(actualAccumulated));
        assertEquals(yearsElapsed, ((Number) result.get("yearsElapsed")).intValue());
        assertTrue(actualCurrent.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void calculateDepreciation_fullyDepreciated_currentValueZero() {
        AssetDTO dto = newAssetDTO("SVC-DEP-FULL-001");
        dto.setPurchaseDate(LocalDate.of(2000, 1, 1));
        dto.setPurchaseValue(new BigDecimal("1000.00"));
        dto.setCurrentValue(new BigDecimal("1000.00"));
        dto.setUsefulLifeYears(5);

        Asset created = assetService.create(dto);
        Map<String, Object> result = assetService.calculateDepreciation(created.getId());

        BigDecimal currentValue = (BigDecimal) result.get("currentValue");
        assertEquals(0, currentValue.compareTo(BigDecimal.ZERO));
    }

    @Test
    void validateStatusTransition_availableToInUse_allowed() {
        AssetDTO createDto = newAssetDTO("SVC-TRANS-AIU-001");
        Asset created = assetService.create(createDto);

        AssetDTO updateDto = newAssetDTO("SVC-TRANS-AIU-001");
        updateDto.setStatus(AssetStatus.IN_USE);

        Asset updated = assetService.update(created.getId(), updateDto);

        assertEquals(AssetStatus.IN_USE, updated.getStatus());
    }

    @Test
    void validateStatusTransition_disposedToAvailable_throwsIllegalStateException() {
        AssetDTO createDto = newAssetDTO("SVC-TRANS-DA-001");
        Asset created = assetService.create(createDto);

        AssetDTO disposeDto = newAssetDTO("SVC-TRANS-DA-001");
        disposeDto.setStatus(AssetStatus.DISPOSED);
        assetService.update(created.getId(), disposeDto);

        AssetDTO reactivateDto = newAssetDTO("SVC-TRANS-DA-001");
        reactivateDto.setStatus(AssetStatus.AVAILABLE);

        assertThrows(IllegalStateException.class,
                () -> assetService.update(created.getId(), reactivateDto));
    }

    @Test
    void validateStatusTransition_inRepairToInUse_allowed() {
        AssetDTO createDto = newAssetDTO("SVC-TRANS-RIU-001");
        createDto.setStatus(AssetStatus.IN_REPAIR);
        Asset created = assetService.create(createDto);

        AssetDTO updateDto = newAssetDTO("SVC-TRANS-RIU-001");
        updateDto.setStatus(AssetStatus.IN_USE);

        Asset updated = assetService.update(created.getId(), updateDto);

        assertEquals(AssetStatus.IN_USE, updated.getStatus());
    }
}
