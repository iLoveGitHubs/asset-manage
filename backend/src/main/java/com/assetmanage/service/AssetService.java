package com.assetmanage.service;

import com.assetmanage.dto.AssetDTO;
import com.assetmanage.entity.Asset;
import com.assetmanage.entity.AssetStatus;
import com.assetmanage.exception.ResourceNotFoundException;
import com.assetmanage.repository.AssetRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Transactional
    public Asset create(AssetDTO dto) {
        if (assetRepository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("Asset with code '" + dto.getCode() + "' already exists");
        }
        Asset asset = new Asset();
        asset.setName(dto.getName());
        asset.setCode(dto.getCode());
        asset.setStatus(dto.getStatus() == null ? AssetStatus.AVAILABLE : dto.getStatus());
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setPurchaseValue(dto.getPurchaseValue());
        asset.setCurrentValue(dto.getCurrentValue() != null ? dto.getCurrentValue() : dto.getPurchaseValue());
        asset.setCategoryId(dto.getCategoryId());
        asset.setUsefulLifeYears(dto.getUsefulLifeYears());
        return assetRepository.save(asset);
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public Page<Asset> findAll(Pageable pageable) {
        return assetRepository.findAll(pageable);
    }

    public Asset findById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found with id " + id));
    }

    @Transactional
    public Asset update(Long id, AssetDTO dto) {
        Asset asset = findById(id);
        if (!asset.getCode().equals(dto.getCode()) && assetRepository.existsByCode(dto.getCode())) {
            throw new IllegalStateException("Asset with code '" + dto.getCode() + "' already exists");
        }
        asset.setName(dto.getName());
        asset.setCode(dto.getCode());
        if (dto.getStatus() != null) {
            validateStatusTransition(asset.getStatus(), dto.getStatus());
            asset.setStatus(dto.getStatus());
        }
        asset.setPurchaseDate(dto.getPurchaseDate());
        asset.setPurchaseValue(dto.getPurchaseValue());
        if (dto.getCurrentValue() != null) {
            asset.setCurrentValue(dto.getCurrentValue());
        }
        asset.setCategoryId(dto.getCategoryId());
        asset.setUsefulLifeYears(dto.getUsefulLifeYears());
        return assetRepository.save(asset);
    }

    @Transactional
    public void delete(Long id) {
        Asset asset = findById(id);
        assetRepository.delete(asset);
    }

    @Transactional
    public Asset assignCategory(Long id, Long categoryId) {
        Asset asset = findById(id);
        asset.setCategoryId(categoryId);
        return assetRepository.save(asset);
    }

    @Transactional
    public Map<String, Object> calculateDepreciation(Long id) {
        Asset asset = findById(id);

        BigDecimal purchaseValue = asset.getPurchaseValue() == null
                ? BigDecimal.ZERO : asset.getPurchaseValue();
        int usefulLife = asset.getUsefulLifeYears();
        LocalDate purchaseDate = asset.getPurchaseDate();

        BigDecimal accumulatedDepreciation;
        BigDecimal currentValue;
        int yearsElapsed = 0;

        if (purchaseDate != null && usefulLife > 0 && purchaseValue.compareTo(BigDecimal.ZERO) > 0) {
            yearsElapsed = Period.between(purchaseDate, LocalDate.now()).getYears();
            if (yearsElapsed < 0) {
                yearsElapsed = 0;
            }
            if (yearsElapsed > usefulLife) {
                yearsElapsed = usefulLife;
            }
            accumulatedDepreciation = purchaseValue
                    .multiply(BigDecimal.valueOf(yearsElapsed))
                    .divide(BigDecimal.valueOf(usefulLife), 2, RoundingMode.HALF_UP);
            currentValue = purchaseValue.subtract(accumulatedDepreciation);
            if (currentValue.compareTo(BigDecimal.ZERO) < 0) {
                currentValue = BigDecimal.ZERO;
            }
        } else {
            accumulatedDepreciation = BigDecimal.ZERO;
            currentValue = purchaseValue;
        }

        asset.setCurrentValue(currentValue);
        assetRepository.save(asset);

        Map<String, Object> result = new HashMap<>();
        result.put("assetId", asset.getId());
        result.put("code", asset.getCode());
        result.put("name", asset.getName());
        result.put("purchaseValue", purchaseValue);
        result.put("usefulLifeYears", usefulLife);
        result.put("yearsElapsed", yearsElapsed);
        result.put("accumulatedDepreciation", accumulatedDepreciation);
        result.put("currentValue", currentValue);
        return result;
    }

    public List<Asset> search(String q) {
        return assetRepository.search(q);
    }

    private void validateStatusTransition(AssetStatus current, AssetStatus target) {
        if (current == AssetStatus.DISPOSED) {
            throw new IllegalStateException("Cannot change status of a DISPOSED asset");
        }
        boolean valid = switch (current) {
            case AVAILABLE -> target == AssetStatus.IN_USE
                    || target == AssetStatus.DISPOSED
                    || target == AssetStatus.AVAILABLE;
            case IN_USE -> target == AssetStatus.AVAILABLE
                    || target == AssetStatus.IN_REPAIR
                    || target == AssetStatus.IN_USE;
            case IN_REPAIR -> target == AssetStatus.AVAILABLE
                    || target == AssetStatus.IN_USE
                    || target == AssetStatus.IN_REPAIR;
            case DISPOSED -> false;
        };
        if (!valid) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + target);
        }
    }
}
