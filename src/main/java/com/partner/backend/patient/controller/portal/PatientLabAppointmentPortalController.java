package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PatientBookLabRequest;
import com.partner.backend.patient.dto.portal.PatientLabBookingSummaryResponse;
import com.partner.backend.patient.service.portal.PatientLabBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/lab-appointments")
@RequiredArgsConstructor
public class PatientLabAppointmentPortalController {

    private final PatientLabBookingService labBookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientLabBookingSummaryResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                labBookingService.list(userDetails.getProviderId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientLabBookingSummaryResponse>> book(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientBookLabRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Booked",
                labBookingService.book(userDetails.getProviderId(), body)));
    }
}
