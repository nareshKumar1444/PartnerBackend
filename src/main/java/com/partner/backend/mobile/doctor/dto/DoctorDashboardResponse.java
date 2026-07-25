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
public class DoctorDashboardResponse {
    private long totalAppointments;
    private long pendingAppointments;
    private long completedAppointments;
    private long activeConsultations;
    private BigDecimal totalEarnings;
    private List<AppointmentResponse> todaysAppointments;
}
