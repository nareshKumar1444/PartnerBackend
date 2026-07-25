package com.partner.backend.patient.service.portal;

import com.partner.backend.patient.dto.portal.PrescriptionMedicationDto;
import com.partner.backend.patient.dto.portal.PrescriptionScanResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrescriptionScanService {

    private final GroqPrescriptionVisionService groqVisionService;

    public PrescriptionScanResponse scan(byte[] imageBytes, String filename) {
        if (!groqVisionService.isConfigured()) {
            return PrescriptionScanResponse.builder()
                    .ocrConfigured(false)
                    .medicalDocument(false)
                    .message("Prescription scan is not available right now. Set GROQ_API_KEY on the server.")
                    .build();
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return PrescriptionScanResponse.builder()
                    .ocrConfigured(true)
                    .medicalDocument(false)
                    .message("Choose a prescription photo first.")
                    .build();
        }

        Optional<List<PrescriptionMedicationDto>> medicationsOpt =
                groqVisionService.extractMedications(imageBytes, filename);
        if (medicationsOpt.isEmpty()) {
            return PrescriptionScanResponse.builder()
                    .ocrConfigured(true)
                    .medicalDocument(false)
                    .message("Could not read medicines from this image. Use a clear photo of a medicine slip or list.")
                    .build();
        }

        List<PrescriptionMedicationDto> medications = medicationsOpt.get();
        String rawText = GroqPrescriptionVisionService.buildRawTextFromMedications(medications);
        return PrescriptionScanResponse.builder()
                .ocrConfigured(true)
                .medicalDocument(true)
                .rawText(rawText)
                .medications(medications)
                .message("Medicine slip scanned successfully.")
                .build();
    }
}
