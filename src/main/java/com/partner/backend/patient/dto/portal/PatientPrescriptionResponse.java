package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientPrescriptionResponse {
    /** Consultation entity id — safe public id for history */
    private Long id;
    private String doctorName;
    private String clinicName;
    private LocalDateTime recordedAt;
    private String diagnosis;
    private String notes;
    private List<String> medicines;
}
