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
public class DashboardStatsResponse {
    private long totalPatients;
    private long approvedDoctors;
    private long pendingDoctors;
    private long approvedPharmacies;
    private long pendingPharmacies;
    private long approvedLabs;
    private long pendingLabs;
    private BigDecimal grossRevenue;
    private BigDecimal platformCommission;
    private long activeConsultations;
}
