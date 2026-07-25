package com.partner.backend.patient.controller.portal;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.dto.portal.PatientOrderSummaryResponse;
import com.partner.backend.patient.dto.portal.PatientPlaceOrderRequest;
import com.partner.backend.patient.service.portal.PatientOrderBookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/orders")
@RequiredArgsConstructor
public class PatientOrderPortalController {

    private final PatientOrderBookingService patientOrderBookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientOrderSummaryResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(
                patientOrderBookingService.list(userDetails.getProviderId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PatientOrderSummaryResponse>> place(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PatientPlaceOrderRequest body) {
        return ResponseEntity.ok(ApiResponse.ok("Order placed",
                patientOrderBookingService.place(userDetails.getProviderId(), body)));
    }
}
