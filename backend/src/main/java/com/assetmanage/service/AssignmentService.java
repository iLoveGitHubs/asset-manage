package com.assetmanage.service;

import com.assetmanage.dto.AssignmentDTO;
import com.assetmanage.entity.Asset;
import com.assetmanage.entity.AssetStatus;
import com.assetmanage.entity.Assignment;
import com.assetmanage.exception.ResourceNotFoundException;
import com.assetmanage.repository.AssetRepository;
import com.assetmanage.repository.AssignmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;

    public AssignmentService(AssignmentRepository assignmentRepository, AssetRepository assetRepository) {
        this.assignmentRepository = assignmentRepository;
        this.assetRepository = assetRepository;
    }

    @Transactional
    public Assignment assign(AssignmentDTO dto) {
        Asset asset = assetRepository.findById(dto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id " + dto.getAssetId()));

        if (asset.getStatus() != AssetStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Asset must be AVAILABLE to assign. Current status: " + asset.getStatus());
        }
        if (assignmentRepository.existsByAssetIdAndReturnDateIsNull(dto.getAssetId())) {
            throw new IllegalStateException("Asset is already actively assigned to an employee");
        }

        asset.setStatus(AssetStatus.IN_USE);
        assetRepository.save(asset);

        Assignment assignment = new Assignment();
        assignment.setAssetId(dto.getAssetId());
        assignment.setEmployeeId(dto.getEmployeeId());
        assignment.setAssignDate(dto.getAssignDate() == null ? LocalDate.now() : dto.getAssignDate());
        return assignmentRepository.save(assignment);
    }

    @Transactional
    public Assignment returnAsset(Long assetId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asset not found with id " + assetId));

        Assignment assignment = assignmentRepository.findByAssetId(assetId).stream()
                .filter(a -> a.getReturnDate() == null)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Asset " + assetId + " is not currently assigned"));

        assignment.setReturnDate(LocalDate.now());

        asset.setStatus(AssetStatus.AVAILABLE);
        assetRepository.save(asset);

        return assignmentRepository.save(assignment);
    }

    public List<Assignment> findAll() {
        return assignmentRepository.findAll();
    }
}
