package com.partner.backend.admin.controller;

import com.partner.backend.admin.dto.CreateHealthMetricRequest;
import com.partner.backend.admin.dto.HealthMetricDefinitionResponse;
import com.partner.backend.admin.service.AdminHealthMetricService;
import com.partner.backend.common.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/health-metrics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminHealthMetricController {

    private final AdminHealthMetricService healthMetricService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthMetricDefinitionResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(healthMetricService.listAll()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<HealthMetricDefinitionResponse>> create(
            @Valid @RequestBody CreateHealthMetricRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Health metric added", healthMetricService.create(req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        healthMetricService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Health metric deleted", null));
    }
}
