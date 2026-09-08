package com.assetmanage.repository;

import com.assetmanage.entity.Asset;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    Optional<Asset> findByCode(String code);

    boolean existsByCode(String code);

    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(a.code) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Asset> search(@Param("q") String q);

    @Query("SELECT a FROM Asset a WHERE " +
           "LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(a.code) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Asset> searchPage(@Param("q") String q, Pageable pageable);

    List<Asset> findByCategoryId(Long categoryId);
}
