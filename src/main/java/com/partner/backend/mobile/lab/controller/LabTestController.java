package com.partner.backend.mobile.lab.controller;

import com.partner.backend.common.security.CustomUserDetails;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.common.util.ResponseWrapper;
import com.partner.backend.mobile.lab.dto.*;
import com.partner.backend.mobile.lab.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mobile/lab/tests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LAB')")
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    public ResponseEntity<ApiResponse<ResponseWrapper<LabTestResponse>>> list(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("testName"));
        return ResponseEntity.ok(ApiResponse.ok(
                ResponseWrapper.from(labTestService.list(userDetails.getProviderId(), query, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LabTestResponse>> add(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LabTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test added", labTestService.add(userDetails.getProviderId(), req)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LabTestResponse>> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody LabTestRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Test updated",
                labTestService.update(userDetails.getProviderId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        labTestService.delete(userDetails.getProviderId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Test removed"));
    }
}
