package com.assetmanage.controller;

import com.assetmanage.dto.AssignmentDTO;
import com.assetmanage.entity.Assignment;
import com.assetmanage.service.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assets/{id}/assign")
    public ResponseEntity<Assignment> assign(@PathVariable Long id,
                                             @Valid @RequestBody AssignmentDTO dto) {
        dto.setAssetId(id);
        return new ResponseEntity<>(assignmentService.assign(dto), HttpStatus.CREATED);
    }

    @PostMapping("/assets/{id}/return")
    public ResponseEntity<Assignment> returnAsset(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.returnAsset(id));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<Assignment>> findAll() {
        return ResponseEntity.ok(assignmentService.findAll());
    }
}
