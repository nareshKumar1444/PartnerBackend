package com.partner.backend.mobile.pharmacy.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.doctor.dto.EarningsResponse;
import com.partner.backend.mobile.doctor.dto.NotificationResponse;
import com.partner.backend.mobile.pharmacy.service.PharmacyEarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/pharmacy")
@RequiredArgsConstructor
@PreAuthorize("hasRole('PHARMACY')")
public class PharmacyEarningsController {

    private final PharmacyEarningsService pharmacyEarningsService;

    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(pharmacyEarningsService.getEarnings(userDetails.getProviderId())));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<ResponseWrapper<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(pharmacyEarningsService.getNotifications(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        pharmacyEarningsService.markNotificationRead(userDetails.getProviderId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read"));
    }
}
