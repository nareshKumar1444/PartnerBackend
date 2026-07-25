package com.partner.backend.mobile.doctor.dto;

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
public class PharmaRewardResponse {
    private Long id;
    private String pharmacyName;
    private BigDecimal amount;
    private int month;
    private int year;
    private String description;
    private LocalDateTime createdAt;
}
