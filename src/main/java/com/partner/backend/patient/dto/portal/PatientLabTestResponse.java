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
public class PatientLabTestResponse {
    private Long id;
    private String testName;
    private BigDecimal normalPrice;
    private BigDecimal discountedPrice;
    private String category;
    private String description;
}
