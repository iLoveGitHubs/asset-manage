package com.assetmanage.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AssignmentDTO {

    private Long id;

    @NotNull(message = "Asset id is required")
    private Long assetId;

    @NotNull(message = "Employee id is required")
    private Long employeeId;

    private LocalDate assignDate;

    private LocalDate returnDate;
}
