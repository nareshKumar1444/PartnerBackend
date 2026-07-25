package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.doctor.dto.*;
import com.partner.backend.mobile.doctor.service.DoctorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorProfileController {

    private final DoctorProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(userDetails.getProviderId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<DoctorProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DoctorProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                profileService.updateProfile(userDetails.getProviderId(), req)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> getAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getAvailability(userDetails.getProviderId())));
    }

    @PutMapping("/availability")
    public ResponseEntity<ApiResponse<List<AvailabilityResponse>>> updateAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AvailabilityRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Availability updated",
                profileService.updateAvailability(userDetails.getProviderId(), req)));
    }
}
