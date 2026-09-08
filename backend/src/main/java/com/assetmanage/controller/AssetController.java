package com.assetmanage.controller;

import com.assetmanage.dto.AssetDTO;
import com.assetmanage.entity.Asset;
import com.assetmanage.service.AssetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    public ResponseEntity<Asset> create(@Valid @RequestBody AssetDTO dto) {
        Asset created = assetService.create(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<Asset>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(assetService.findAll(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Asset>> search(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query parameter 'q' must not be empty");
        }
        return ResponseEntity.ok(assetService.search(q.trim()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Asset> findById(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Asset> update(@PathVariable Long id, @Valid @RequestBody AssetDTO dto) {
        return ResponseEntity.ok(assetService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assetService.delete(id);
    }

    @PutMapping("/{id}/category")
    public ResponseEntity<Asset> assignCategory(@PathVariable Long id,
                                                 @RequestBody Map<String, Long> body) {
        Long categoryId = body.get("categoryId");
        return ResponseEntity.ok(assetService.assignCategory(id, categoryId));
    }

    @GetMapping("/{id}/depreciation")
    public ResponseEntity<Map<String, Object>> depreciation(@PathVariable Long id) {
        return ResponseEntity.ok(assetService.calculateDepreciation(id));
    }
}
