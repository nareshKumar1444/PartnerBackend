package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PatientHealthReadingCreateRequest;
import com.partner.backend.patient.dto.portal.PatientHealthReadingResponse;
import com.partner.backend.patient.service.portal.PatientHealthReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/health-readings")
@RequiredArgsConstructor
public class PatientHealthReadingPortalController {

    private final PatientHealthReadingService patientHealthReadingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientHealthReadingResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                patientHealthReadingService.list(userDetails.getProviderId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientHealthReadingResponse>> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientHealthReadingCreateRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Saved",
                patientHealthReadingService.create(userDetails.getProviderId(), body)));
    }
}
