package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PatientPrescriptionResponse;
import com.partner.backend.patient.service.portal.PatientPrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient/prescriptions")
@RequiredArgsConstructor
public class PatientPrescriptionPortalController {

    private final PatientPrescriptionService patientPrescriptionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientPrescriptionResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                patientPrescriptionService.list(userDetails.getProviderId())));
    }
}
