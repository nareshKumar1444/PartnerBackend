package com.partner.backend.mobile.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabDashboardResponse {
    private long totalTests;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private BigDecimal totalRevenue;
    private BigDecimal walletBalance;
}
