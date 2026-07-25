package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.AdminLoginRequest;
import com.partner.backend.admin.dto.AuthTokenResponse;
import com.partner.backend.admin.service.AdminAuthService;
import com.partner.backend.common.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthTokenResponse>> login(@Valid @RequestBody AdminLoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(adminAuthService.login(req)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        adminAuthService.logout(authHeader);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
}
