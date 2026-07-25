package com.partner.backend.mobile.doctor.dto;

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
public class ConsultationResponse {
    private Long id;
    private Long appointmentId;
    /** Snapshot from appointment */
    private String patientName;
    private String symptoms;
    private String diagnosis;
    private String notes;
    private List<MedicineResponse> medicines;
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicineResponse {
        private Long id;
        private String name;
        private String dosage;
        private String frequency;
        private String duration;
        private String instructions;
    }
}
