package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.pharmacy.dto.*;
import com.partner.backend.mobile.pharmacy.service.PharmacyProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACY')")
public class PharmacyProfileController {

    private final PharmacyProfileService profileService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<PharmacyProfileResponse>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(profileService.getProfile(userDetails.getProviderId())));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<PharmacyProfileResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PharmacyProfileUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated",
                profileService.updateProfile(userDetails.getProviderId(), req)));
    }
}
