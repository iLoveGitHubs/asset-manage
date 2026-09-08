package com.assetmanage.dto;

import com.assetmanage.entity.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AssetDTO {

    private Long id;

    @NotBlank(message = "Asset name is required")
    private String name;

    @NotBlank(message = "Asset code is required")
    private String code;

    private AssetStatus status;

    private LocalDate purchaseDate;

    @PositiveOrZero(message = "Purchase value must be zero or positive")
    private BigDecimal purchaseValue;

    private BigDecimal currentValue;

    private Long categoryId;

    @PositiveOrZero(message = "Useful life years must be zero or positive")
    private int usefulLifeYears;
}
