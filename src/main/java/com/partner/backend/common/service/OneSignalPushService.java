package com.partner.backend.common.service;

import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.push.PushExternalUserId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OneSignalPushService {

    private static final String ONESIGNAL_API_URL = "https://api.onesignal.com/notifications";

    private final RestTemplate restTemplate;

    @Value("${app.onesignal.rest-api-key:}")
    private String restApiKey;

    @Value("${app.onesignal.partner-app-id:}")
    private String partnerAppId;

    @Value("${app.onesignal.patient-app-id:}")
    private String patientAppId;

    @Value("${app.onesignal.admin-app-id:}")
    private String adminAppId;

    @Value("${app.onesignal.enabled:false}")
    private boolean enabled;

    @Async
    public void sendProviderPushAsync(Long providerId, ProviderType providerType, String title, String message) {
        if (!isConfigured()) {
            return;
        }
        String externalId = PushExternalUserId.forProvider(providerType, providerId);
        if (externalId == null) {
            return;
        }
        String appId = resolveAppId(providerType);
        if (!StringUtils.hasText(appId)) {
            log.debug("OneSignal app id missing for provider type {}", providerType);
            return;
        }
        sendToExternalUser(appId, externalId, title, message, providerType, providerId);
    }

    @Async
    public void sendAdminPushAsync(String adminEmail, String title, String message) {
        if (!isConfigured()) {
            return;
        }
        String externalId = PushExternalUserId.forAdminEmail(adminEmail);
        if (externalId == null || !StringUtils.hasText(adminAppId)) {
            return;
        }
        sendToExternalUser(adminAppId, externalId, title, message, null, null);
    }

    private boolean isConfigured() {
        return enabled && StringUtils.hasText(restApiKey);
    }

    private String resolveAppId(ProviderType providerType) {
        if (providerType == ProviderType.PATIENT) {
            return patientAppId;
        }
        if (providerType == ProviderType.DOCTOR
                || providerType == ProviderType.PHARMACY
                || providerType == ProviderType.LAB) {
            return partnerAppId;
        }
        return partnerAppId;
    }

    private void sendToExternalUser(
            String appId,
            String externalUserId,
            String title,
            String message,
            ProviderType providerType,
            Long providerId) {
        Map<String, Object> body = new HashMap<>();
        body.put("app_id", appId.trim());
        body.put("target_channel", "push");
        body.put("include_aliases", Map.of("external_id", List.of(externalUserId)));
        body.put("headings", Map.of("en", safe(title, "Health Wallet")));
        body.put("contents", Map.of("en", safe(message, "You have a new notification.")));

        Map<String, String> data = new HashMap<>();
        data.put("externalUserId", externalUserId);
        if (providerType != null) {
            data.put("providerType", providerType.name());
        }
        if (providerId != null) {
            data.put("providerId", String.valueOf(providerId));
        }
        body.put("data", data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Key " + restApiKey.trim());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    ONESIGNAL_API_URL,
                    new HttpEntity<>(body, headers),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("OneSignal push failed (HTTP {}): {}", response.getStatusCode(), response.getBody());
            }
        } catch (RestClientException ex) {
            log.warn("OneSignal push request failed for {}: {}", externalUserId, ex.getMessage());
        }
    }

    private static String safe(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }
}
