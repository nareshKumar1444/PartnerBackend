package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientInventoryItemResponse {
    private Long id;
    private String medicineName;
    private Integer quantityAvailable;
    private BigDecimal unitPrice;
    private String category;
    private LocalDate expiryDate;
}
