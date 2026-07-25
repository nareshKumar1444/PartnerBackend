package com.partner.backend.patient.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientHealthReadingCreateRequest {

    /** Active metric key from health metric catalog (e.g. heart_rate, vitamin_d) */
    @NotBlank
    @Size(max = 64)
    private String metricKey;

    @NotBlank
    @Size(max = 128)
    private String value;

    @Size(max = 64)
    private String unit;

    @NotNull
    private LocalDate readingDate;

    @Size(max = 2000)
    private String note;
}
