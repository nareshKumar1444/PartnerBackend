package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.dto.VideoCallSessionResponse;
import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.service.AppointmentVideoCallService;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PatientAppointmentSummaryResponse;
import com.partner.backend.patient.dto.portal.PatientBookAppointmentRequest;
import com.partner.backend.patient.service.portal.PatientAppointmentBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/appointments")
@RequiredArgsConstructor
public class PatientAppointmentPortalController {

    private final PatientAppointmentBookingService appointmentBookingService;
    private final AppointmentVideoCallService videoCallService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientAppointmentSummaryResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                appointmentBookingService.list(userDetails.getProviderId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientAppointmentSummaryResponse>> book(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientBookAppointmentRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Booked",
                appointmentBookingService.book(userDetails.getProviderId(), body)));
    }

    @GetMapping("/{id}/video-call")
    public ResponseEntity<ApiResponse<VideoCallSessionResponse>> videoCallSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean audioOnly) {
        return ResponseEntity.ok(ApiResponse.ok("Video call session ready",
                videoCallService.createPatientSession(userDetails.getProviderId(), id, audioOnly)));
    }
}
