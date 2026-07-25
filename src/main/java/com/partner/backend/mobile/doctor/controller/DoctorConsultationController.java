package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.doctor.dto.ConsultationRequest;
import com.partner.backend.mobile.doctor.dto.ConsultationResponse;
import com.partner.backend.mobile.doctor.service.DoctorConsultationService;
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
public class DoctorConsultationController {

    private final DoctorConsultationService consultationService;

    @GetMapping("/prescriptions")
    public ResponseEntity<ApiResponse<ResponseWrapper<ConsultationResponse>>> listPrescriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(consultationService.listConsultations(userDetails.getProviderId(), pageable))));
    }

    @GetMapping("/consultation/{appointmentId}")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(ApiResponse.ok(
                consultationService.getByAppointment(userDetails.getProviderId(), appointmentId)));
    }

    @PostMapping("/consultation")
    public ResponseEntity<ApiResponse<ConsultationResponse>> saveConsultation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ConsultationRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Consultation saved",
                consultationService.saveConsultation(userDetails.getProviderId(), req)));
    }
}
