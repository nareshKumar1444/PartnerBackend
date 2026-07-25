package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.patient.dto.portal.PatientNotificationResponse;
import com.partner.backend.patient.service.portal.PatientNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patient/notifications")
@RequiredArgsConstructor
public class PatientNotificationPortalController {

    private final PatientNotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientNotificationResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(notificationService.list(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        notificationService.markRead(userDetails.getProviderId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Marked as read"));
    }
}
