package com.partner.backend.patient.dto.portal;

import com.partner.backend.common.entity.AppointmentStatus;
import com.partner.backend.common.entity.AppointmentType;
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
public class PatientAppointmentSummaryResponse {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private String specialty;
    private LocalDate date;
    private String timeSlot;
    private AppointmentType type;
    private AppointmentStatus backendStatus;
    private BigDecimal fee;
}
