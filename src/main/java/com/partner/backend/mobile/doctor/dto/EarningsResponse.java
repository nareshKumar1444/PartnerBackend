package com.partner.backend.mobile.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningsResponse {
    private BigDecimal totalGross;
    private BigDecimal totalCommission;
    private BigDecimal totalNet;
    private List<MonthlyEntry> monthly;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEntry {
        private int month;
        private int year;
        private BigDecimal gross;
        private BigDecimal net;
    }
}
