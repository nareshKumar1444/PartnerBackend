package com.partner.backend.mobile.doctor.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.doctor.dto.PatientBriefResponse;
import com.partner.backend.mobile.doctor.service.DoctorPatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/doctor")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorPatientController {

    private final DoctorPatientService doctorPatientService;

    @GetMapping("/patients")
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientBriefResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        var pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(doctorPatientService.listForDoctor(userDetails.getProviderId(), pageable))));
    }
}
