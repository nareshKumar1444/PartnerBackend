package com.partner.backend.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class OneSignalStartupLogger {

    @Value("${app.onesignal.enabled:false}")
    private boolean enabled;

    @Value("${app.onesignal.rest-api-key:}")
    private String restApiKey;

    @Value("${app.onesignal.partner-app-id:}")
    private String partnerAppId;

    @Value("${app.onesignal.patient-app-id:}")
    private String patientAppId;

    @EventListener(ApplicationReadyEvent.class)
    public void logPushConfig() {
        boolean keySet = StringUtils.hasText(restApiKey);
        if (!enabled) {
            log.warn("[PUSH] OneSignal disabled (ONESIGNAL_ENABLED=false)");
            return;
        }
        if (!keySet) {
            log.warn("[PUSH] OneSignal enabled but ONESIGNAL_REST_API_KEY is missing — push will be skipped");
            return;
        }
        if (!StringUtils.hasText(partnerAppId) || !StringUtils.hasText(patientAppId)) {
            log.warn(
                    "[PUSH] OneSignal API key set but app id missing — partnerAppId={} patientAppId={}",
                    blank(partnerAppId),
                    blank(patientAppId));
            return;
        }
        log.info(
                "[PUSH] OneSignal ready — partnerAppId={} patientAppId={} apiKey={}",
                partnerAppId.trim(),
                patientAppId.trim(),
                maskKey(restApiKey));
    }

    private static String blank(String value) {
        return StringUtils.hasText(value) ? "set" : "MISSING";
    }

    private static String maskKey(String key) {
        String trimmed = key.trim();
        if (trimmed.length() <= 8) {
            return "****";
        }
        return trimmed.substring(0, 8) + "...";
    }
}
