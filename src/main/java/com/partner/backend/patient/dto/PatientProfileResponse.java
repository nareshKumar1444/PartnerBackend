package com.partner.backend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientProfileResponse {
    private Long patientId;
    private String email;
    private String normalizedCnic;
    private String name;
    private String phone;
    private Integer age;
    private String city;
    private String bloodGroup;
    private List<String> conditions;
}
