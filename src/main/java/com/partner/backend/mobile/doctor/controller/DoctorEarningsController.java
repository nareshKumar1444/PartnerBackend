package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.doctor.dto.*;
import com.partner.backend.mobile.doctor.service.DoctorEarningsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorEarningsController {

    private final DoctorEarningsService earningsService;

    @GetMapping("/earnings")
    public ResponseEntity<ApiResponse<EarningsResponse>> getEarnings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(earningsService.getEarnings(userDetails.getProviderId())));
    }

    @GetMapping("/pharma-rewards")
    public ResponseEntity<ApiResponse<ResponseWrapper<PharmaRewardResponse>>> getPharmaRewards(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(earningsService.getPharmaRewards(userDetails.getProviderId(), pageable))));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<ResponseWrapper<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(earningsService.getNotifications(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markNotificationRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        earningsService.markNotificationRead(userDetails.getProviderId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read"));
    }
}
