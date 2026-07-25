package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PrescriptionScanResponse;
import com.partner.backend.patient.service.portal.PrescriptionScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/patient/prescription")
@RequiredArgsConstructor
public class PatientPrescriptionScanController {

    private final PrescriptionScanService prescriptionScanService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PrescriptionScanResponse>> scan(@RequestPart("file") MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.isEmpty() ? new byte[0] : file.getBytes();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Unable to read uploaded image."));
        }
        PrescriptionScanResponse result = prescriptionScanService.scan(bytes, file.getOriginalFilename());
        String message = result.getMessage() != null ? result.getMessage() : "Scan complete";
        return ResponseEntity.ok(ApiResponse.ok(message, result));
    }
}
