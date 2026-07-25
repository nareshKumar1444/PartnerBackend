package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDoctorSummaryResponse {
    private Long id;
    private String name;
    private String specialty;
    private String city;
    private String clinicName;
    private BigDecimal physicalFee;
    private BigDecimal virtualFee;
    private String consultationType;
}
