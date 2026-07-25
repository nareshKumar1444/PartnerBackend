package com.partner.backend.patient.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientBookLabRequest {
    @NotNull
    private Long labId;
    @NotNull
    private Long testId;
    @NotNull
    private LocalDate scheduledDate;
    @NotBlank
    private String scheduledTimeSlot;
}
