package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionMedicationDto {

    private String name;
    private String strength;
    private String dosage;
    private String duration;
    private String quantity;
}
