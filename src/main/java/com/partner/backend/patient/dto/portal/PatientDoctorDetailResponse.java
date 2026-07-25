package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDoctorDetailResponse {
    private Long id;
    private String name;
    private String specialty;
    private String city;
    private String clinicName;
    private String clinicAddress;
    private BigDecimal physicalFee;
    private BigDecimal virtualFee;
    private String consultationType;
    private String bio;
    private List<String> slots;
}
