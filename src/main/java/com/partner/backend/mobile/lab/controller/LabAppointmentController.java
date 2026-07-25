package com.partner.backend.mobile.lab.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.lab.dto.*;
import com.partner.backend.mobile.lab.service.LabAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/lab/appointments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB')")
public class LabAppointmentController {

    private final LabAppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<LabAppointmentResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(appointmentService.list(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<LabAppointmentResponse>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LabAppointmentStatusRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                appointmentService.updateStatus(userDetails.getProviderId(), id, req)));
    }
}
