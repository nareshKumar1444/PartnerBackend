package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.dto.PatientQrShareResponse;
import com.partner.backend.common.service.PatientQrShareService;
import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorPatientQrController {

    private final PatientQrShareService patientQrShareService;

    @GetMapping("/qr-shares/{accessCode}")
    public ResponseEntity<ApiResponse<PatientQrShareResponse>> getByAccessCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String accessCode) {
        PatientQrShareResponse response = patientQrShareService.getByAccessCode(accessCode);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.ok("QR share not found", null));
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
