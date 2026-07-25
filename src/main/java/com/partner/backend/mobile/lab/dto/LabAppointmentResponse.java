package com.partner.backend.mobile.lab.dto;

import com.partner.backend.common.entity.LabAppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabAppointmentResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    private String testName;
    private BigDecimal testPrice;
    private LocalDate scheduledDate;
    private String scheduledTimeSlot;
    private LabAppointmentStatus status;
    private LocalDateTime createdAt;
}
