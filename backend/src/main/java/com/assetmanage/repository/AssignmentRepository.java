package com.assetmanage.repository;

import com.assetmanage.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByAssetId(Long assetId);

    boolean existsByAssetIdAndReturnDateIsNull(Long assetId);
}
