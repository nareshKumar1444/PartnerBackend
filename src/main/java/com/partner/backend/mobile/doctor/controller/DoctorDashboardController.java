package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.mobile.doctor.dto.DoctorDashboardResponse;
import com.partner.backend.mobile.doctor.service.DoctorDashboardService;
import com.partner.backend.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorDashboardController {

    private final DoctorDashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DoctorDashboardResponse>> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getDashboard(userDetails.getProviderId())));
    }
}
