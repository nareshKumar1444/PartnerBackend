package com.partner.backend.mobile.auth.controller;

import com.partner.backend.mobile.auth.dto.*;
import com.partner.backend.mobile.auth.service.AuthService;
import com.partner.backend.mobile.auth.service.OtpService;
import com.partner.backend.common.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/provider")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    /** Provider login with email + password. */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody ProviderLoginRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(req)));
    }
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        authService.logout(authHeader);

        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully"));
    }
    /** Send OTP to email (used during registration — step 1). */
    @PostMapping("/otp/email/send")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendEmailOtp(
            @Valid @RequestBody EmailOtpRequest req) {
        String name = req.getName() != null ? req.getName() : req.getEmail().split("@")[0];
        otpService.sendOtpEmail(req.getEmail(), name);
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to " + req.getEmail(),
                Map.of("message", "OTP sent to " + req.getEmail())));
    }

    /** Verify email OTP. */
    @PostMapping("/otp/email/verify")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyEmailOtp(
            @Valid @RequestBody EmailOtpVerifyRequest req) {
        boolean verified = otpService.verifyOtpEmail(req.getEmail(), req.getOtpCode());
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully", Map.of("verified", verified)));
    }

    /** Legacy phone OTP send (kept for internal testing). */
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendOtp(@Valid @RequestBody OtpRequest req) {
        otpService.sendOtp(req.getPhone());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent",
                Map.of("message", "OTP sent to " + req.getPhone())));
    }

    /** Legacy phone OTP verify. */
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        boolean verified = otpService.verifyOtp(req.getPhone(), req.getOtpCode());
        return ResponseEntity.ok(ApiResponse.ok("OTP verified", Map.of("verified", verified)));
    }

    /** Forgot password — step 1: confirm the provider account exists and email an OTP. */
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(@Valid @RequestBody EmailOtpRequest req) {
        authService.forgotPassword(req.getEmail());
        String msg = "OTP sent to " + req.getEmail().trim().toLowerCase();
        return ResponseEntity.ok(ApiResponse.ok(msg, Map.of("message", msg)));
    }

    /** Forgot password — step 3: set a new password after the OTP (sent in step 1) has been verified. */
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully"));
    }
}
