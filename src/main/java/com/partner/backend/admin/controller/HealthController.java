package com.partner.backend.admin.controller;

import com.partner.backend.common.util.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/healthz")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health(
            @Value("${app.onesignal.enabled:false}") boolean pushEnabled,
            @Value("${app.onesignal.rest-api-key:}") String pushApiKey,
            @Value("${app.onesignal.partner-app-id:}") String partnerAppId,
            @Value("${app.onesignal.patient-app-id:}") String patientAppId) {
        boolean pushConfigured = pushEnabled
                && org.springframework.util.StringUtils.hasText(pushApiKey)
                && org.springframework.util.StringUtils.hasText(partnerAppId)
                && org.springframework.util.StringUtils.hasText(patientAppId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "status", "UP",
                "service", "Partner Backend",
                "pushEnabled", pushEnabled,
                "pushConfigured", pushConfigured,
                "partnerAppId", partnerAppId == null || partnerAppId.isBlank() ? "" : partnerAppId.trim(),
                "patientAppId", patientAppId == null || patientAppId.isBlank() ? "" : patientAppId.trim(),
                "pushApiKeySet", org.springframework.util.StringUtils.hasText(pushApiKey))));
    }
}
