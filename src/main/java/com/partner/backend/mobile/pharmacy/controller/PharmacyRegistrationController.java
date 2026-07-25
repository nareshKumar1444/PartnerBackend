package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.pharmacy.dto.DrapVerifyRequest;
import com.partner.backend.mobile.pharmacy.dto.DrapVerifyResponse;
import com.partner.backend.mobile.pharmacy.dto.PharmacyRegisterRequest;
import com.partner.backend.mobile.pharmacy.service.DrapVerificationService;
import com.partner.backend.mobile.pharmacy.service.PharmacyRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy")
@RequiredArgsConstructor
public class PharmacyRegistrationController {

    private final PharmacyRegistrationService registrationService;

    private final DrapVerificationService drapVerificationService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody PharmacyRegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", registrationService.register(req)));
    }

    @PostMapping("/verify-drap")
    public ResponseEntity<ApiResponse<DrapVerifyResponse>> verifyDrap(@Valid @RequestBody DrapVerifyRequest req) {
        DrapVerifyResponse result = drapVerificationService.verifyDrap(req.getDrapLicenseNumber());
        return ResponseEntity.ok(ApiResponse.ok(result.getMessage(), result));
    }
}
