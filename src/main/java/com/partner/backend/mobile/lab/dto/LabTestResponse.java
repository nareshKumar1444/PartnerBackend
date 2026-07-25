package com.partner.backend.mobile.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabTestResponse {
    private Long id;
    private String testName;
    private BigDecimal normalPrice;
    private BigDecimal discountedPrice;
    private Integer reportTimeHours;
    private String description;
    private String category;
    private LocalDateTime createdAt;
}
