package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.*;
import com.partner.backend.admin.service.AdminEarningsService;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/earnings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminEarningsController {

    private final AdminEarningsService earningsService;

    @GetMapping
    public ResponseEntity<ApiResponse<EarningsSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(earningsService.getSummary()));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyEarningsResponse>>> getMonthly() {
        return ResponseEntity.ok(ApiResponse.ok(earningsService.getMonthly()));
    }

    @GetMapping("/providers")
    public ResponseEntity<ApiResponse<List<ProviderEarningsResponse>>> getProviders(
            @RequestParam(required = false) ProviderType type) {
        return ResponseEntity.ok(ApiResponse.ok(earningsService.getPerProvider(type)));
    }
}
