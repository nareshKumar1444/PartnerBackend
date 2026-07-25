package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.pharmacy.dto.PharmacyDashboardResponse;
import com.partner.backend.mobile.pharmacy.service.PharmacyDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACY')")
public class PharmacyDashboardController {

    private final PharmacyDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<PharmacyDashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard(userDetails.getProviderId())));
    }
}
