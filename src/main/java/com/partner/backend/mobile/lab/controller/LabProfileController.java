package com.partner.backend.mobile.lab.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.lab.dto.*;
import com.partner.backend.mobile.lab.service.LabAvailabilityService;
import com.partner.backend.mobile.lab.service.LabProfileService;
import com.partner.backend.mobile.doctor.dto.AvailabilityRequest;
import com.partner.backend.mobile.doctor.dto.AvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/lab")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB')")
public class LabProfileController {

    private final LabProfileService profileService;
    private final LabAvailabilityService availabilityService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<LabProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(userDetails.getProviderId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<LabProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LabProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                profileService.updateProfile(userDetails.getProviderId(), req)));
    }

    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<java.util.List<AvailabilityResponse>>> getAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                availabilityService.getAvailability(userDetails.getProviderId())));
    }

    @PutMapping("/availability")
    public ResponseEntity<ApiResponse<java.util.List<AvailabilityResponse>>> updateAvailability(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AvailabilityRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Availability updated",
                availabilityService.updateAvailability(userDetails.getProviderId(), req)));
    }
}
