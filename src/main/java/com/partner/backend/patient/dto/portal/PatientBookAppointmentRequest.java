package com.partner.backend.patient.dto.portal;

import com.partner.backend.common.entity.AppointmentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientBookAppointmentRequest {
    @NotNull
    private Long doctorId;
    @NotNull
    private LocalDate date;
    @NotNull
    private String timeSlot;
    @NotNull
    private AppointmentType type;
}
