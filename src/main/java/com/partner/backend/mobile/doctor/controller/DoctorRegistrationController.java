package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.doctor.dto.DoctorRegisterRequest;
import com.partner.backend.mobile.doctor.dto.PmdcVerifyRequest;
import com.partner.backend.mobile.doctor.dto.PmdcVerifyResponse;
import com.partner.backend.mobile.doctor.service.DoctorRegistrationService;
import com.partner.backend.common.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
public class DoctorRegistrationController {

    private final DoctorRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody DoctorRegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", registrationService.register(req)));
    }

    @PostMapping("/verify-pmdc")
    public ResponseEntity<ApiResponse<PmdcVerifyResponse>> verifyPmdc(@Valid @RequestBody PmdcVerifyRequest req) {
        PmdcVerifyResponse result = registrationService.verifyPmdc(req.getPmdcNumber());
        return ResponseEntity.ok(ApiResponse.ok(result.getMessage(), result));
    }
}
