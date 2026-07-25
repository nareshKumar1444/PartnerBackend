package com.partner.backend.patient.controller;

import com.partner.backend.common.dto.PatientQrShareRequest;
import com.partner.backend.common.dto.PatientQrShareResponse;
import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.service.PatientQrShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient/qr-shares")
@RequiredArgsConstructor
public class PatientQrShareController {

    private final PatientQrShareService patientQrShareService;

    @PostMapping
    public ResponseEntity<ApiResponse<PatientQrShareResponse>> save(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientQrShareRequest request) {
        PatientQrShareResponse response = patientQrShareService.save(userDetails.getProviderId(), request);
        return ResponseEntity.ok(ApiResponse.ok("QR code saved", response));
    }
}
