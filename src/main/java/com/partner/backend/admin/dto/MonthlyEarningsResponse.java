package com.partner.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyEarningsResponse {
    private int month;
    private int year;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
}
