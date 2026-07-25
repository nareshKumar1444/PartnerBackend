package com.partner.backend.patient.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.PatientProfilePatchRequest;
import com.partner.backend.patient.dto.PatientProfileResponse;
import com.partner.backend.patient.service.PatientAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientProfileController {

    private final PatientAuthService patientAuthService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> me(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PatientProfileResponse profile =
                patientAuthService.getProfile(userDetails.getUser().getId());
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<PatientProfileResponse>> updateMe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientProfilePatchRequest body) {
        PatientProfileResponse profile =
                patientAuthService.patchProfile(userDetails.getUser().getId(), body);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }
}
