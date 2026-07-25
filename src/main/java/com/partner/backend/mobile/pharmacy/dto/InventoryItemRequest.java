package com.partner.backend.mobile.pharmacy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryItemRequest {
    @NotBlank
    private String medicineName;
    @NotNull @Min(0)
    private Integer quantity;
    @NotNull @DecimalMin("0.0")
    private BigDecimal unitPrice;
    private LocalDate expiryDate;
    private String category;
    private String manufacturer;
}
