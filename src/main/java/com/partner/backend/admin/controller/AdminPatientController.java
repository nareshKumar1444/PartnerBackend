package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.PatientResponse;
import com.partner.backend.admin.service.AdminPatientService;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPatientController {

    private final AdminPatientService patientService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<PatientResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(ResponseWrapper.from(patientService.list(pageable))));
    }
}
