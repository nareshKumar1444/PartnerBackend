package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientHealthReadingResponse {
    private Long id;
    /** Lower/snake-ish key aligned with Expo app ({@code heart_rate}, …) */
    private String metricKey;
    private String value;
    private String unit;
    private LocalDate readingDate;
    private String note;
}
