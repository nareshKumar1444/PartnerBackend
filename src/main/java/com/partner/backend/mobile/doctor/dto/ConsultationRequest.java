package com.partner.backend.mobile.doctor.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConsultationRequest {
    private Long appointmentId;
    private String symptoms;
    private String diagnosis;
    private String notes;
    private List<MedicineRequest> medicines;

    @Data
    public static class MedicineRequest {
        private String name;
        private String dosage;
        private String frequency;
        private String duration;
        private String instructions;
    }
}
