package com.partner.backend.patient.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionScanResponse {

    private boolean ocrConfigured;
    /** True when OCR returned readable text from the uploaded image. */
    private boolean medicalDocument;
    private String rawText;
    private String message;
    /** Structured medicines from Groq Vision (patient prescription scan). */
    private List<PrescriptionMedicationDto> medications;
}
