package com.partner.backend.mobile.lab.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.mobile.lab.dto.LabDashboardResponse;
import com.partner.backend.mobile.lab.service.LabDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/lab")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB')")
public class LabDashboardController {

    private final LabDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<LabDashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard(userDetails.getProviderId())));
    }
}
