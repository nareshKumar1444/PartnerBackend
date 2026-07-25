package com.partner.backend.patient.dto.portal;

import com.partner.backend.common.entity.LabAppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientLabBookingSummaryResponse {
    private Long id;
    private Long labId;
    private String labName;
    private Long testId;
    private String testName;
    private LocalDate scheduledDate;
    private String scheduledTimeSlot;
    private BigDecimal price;
    private LabAppointmentStatus backendStatus;
}
