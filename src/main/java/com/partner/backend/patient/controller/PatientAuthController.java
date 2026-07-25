package com.partner.backend.patient.controller;

import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.auth.dto.EmailOtpRequest;
import com.partner.backend.mobile.auth.dto.EmailOtpVerifyRequest;
import com.partner.backend.mobile.auth.dto.ResetPasswordRequest;
import com.partner.backend.mobile.auth.service.OtpService;
import com.partner.backend.patient.dto.PatientForgotPasswordRequest;
import com.partner.backend.patient.dto.PatientForgotPasswordResponse;
import com.partner.backend.patient.dto.PatientLoginRequest;
import com.partner.backend.patient.dto.PatientRegisterRequest;
import com.partner.backend.patient.dto.PatientSessionResponse;
import com.partner.backend.patient.service.PatientAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/patient/auth")
@RequiredArgsConstructor
public class PatientAuthController {

    private final OtpService otpService;
    private final PatientAuthService patientAuthService;

    @PostMapping("/otp/email/send")
    public ResponseEntity<ApiResponse<Map<String, String>>> sendEmailOtp(@Valid @RequestBody EmailOtpRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        String name = req.getName() != null ? req.getName() : email.split("@")[0];
        otpService.sendOtpEmail(email, name);
        String msg = "OTP sent to " + email;
        return ResponseEntity.ok(ApiResponse.ok(msg, Map.of("message", msg)));
    }

    @PostMapping("/otp/email/verify")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyEmailOtp(
            @Valid @RequestBody EmailOtpVerifyRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        boolean verified = otpService.verifyOtpEmail(email, req.getOtpCode());
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully", Map.of("verified", verified)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<PatientSessionResponse>> register(@Valid @RequestBody PatientRegisterRequest req) {
        PatientSessionResponse session = patientAuthService.register(req);
        return ResponseEntity.ok(ApiResponse.ok("Registration completed", session));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<PatientSessionResponse>> login(@Valid @RequestBody PatientLoginRequest req) {
        PatientSessionResponse session = patientAuthService.login(req);
        return ResponseEntity.ok(ApiResponse.ok("Signed in", session));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.ok("Logged out"));
    }

    /** Forgot password — step 1: look up account by mobile number, email an OTP to the registered address. */
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<PatientForgotPasswordResponse>> forgotPassword(
            @Valid @RequestBody PatientForgotPasswordRequest req) {
        String email = patientAuthService.forgotPassword(req.getPhone());
        return ResponseEntity.ok(ApiResponse.ok("OTP sent to your registered email",
                PatientForgotPasswordResponse.builder().email(email).build()));
    }

    /** Forgot password — step 3: set a new password after the OTP (sent in step 1) has been verified. */
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        patientAuthService.resetPassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully"));
    }
}
