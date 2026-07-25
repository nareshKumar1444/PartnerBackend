package com.partner.backend.patient.controller.portal;

import com.partner.backend.admin.dto.HealthMetricDefinitionResponse;
import com.partner.backend.common.util.ApiResponse;
import com.partner.backend.patient.service.portal.PatientHealthMetricCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/health-metrics")
@RequiredArgsConstructor
public class PublicHealthMetricCatalogController {

    private final PatientHealthMetricCatalogService catalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<HealthMetricDefinitionResponse>>> listActive() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.listActive()));
    }
}
