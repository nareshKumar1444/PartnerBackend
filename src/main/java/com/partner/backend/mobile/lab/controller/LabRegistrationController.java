package com.partner.backend.mobile.lab.controller;

import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.lab.dto.LabRegisterRequest;
import com.partner.backend.mobile.lab.service.LabRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/lab")
@RequiredArgsConstructor
public class LabRegistrationController {

    private final LabRegistrationService registrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody LabRegisterRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", registrationService.register(req)));
    }
}
