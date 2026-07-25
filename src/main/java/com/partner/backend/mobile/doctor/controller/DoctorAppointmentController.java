package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.dto.VideoCallSessionResponse;
import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.service.AppointmentVideoCallService;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.doctor.dto.AppointmentResponse;
import com.partner.backend.mobile.doctor.service.DoctorAppointmentService;
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
public class DoctorAppointmentController {

    private final DoctorAppointmentService appointmentService;
    private final AppointmentVideoCallService videoCallService;

    @GetMapping("/appointments")
    public ResponseEntity<ApiResponse<ResponseWrapper<AppointmentResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("date").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(appointmentService.list(userDetails.getProviderId(), pageable))));
    }

    @PutMapping("/appointments/{id}/start")
    public ResponseEntity<ApiResponse<AppointmentResponse>> startConsultation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Consultation started",
                appointmentService.startConsultation(userDetails.getProviderId(), id)));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelAppointment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Appointment cancelled",
                appointmentService.cancelAppointment(userDetails.getProviderId(), id)));
    }

    @GetMapping("/appointments/{id}/video-call")
    public ResponseEntity<ApiResponse<VideoCallSessionResponse>> videoCallSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean audioOnly) {
        return ResponseEntity.ok(ApiResponse.ok("Video call session ready",
                videoCallService.createDoctorSession(userDetails.getProviderId(), id, audioOnly)));
    }
}
