package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientPharmacySummaryResponse {
    private Long id;
    private String name;
    private String city;
    private String address;
    private String phone;
}
