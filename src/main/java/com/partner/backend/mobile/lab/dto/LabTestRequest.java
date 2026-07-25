package com.partner.backend.mobile.lab.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LabTestRequest {
    @NotBlank
    private String testName;
    @DecimalMin("0.0")
    private BigDecimal normalPrice;
    @DecimalMin("0.0")
    private BigDecimal discountedPrice;
    private Integer reportTimeHours;
    private String description;
    private String category;
}
