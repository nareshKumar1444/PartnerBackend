package com.partner.backend.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Sends transactional email via Brevo's HTTPS API (port 443).
 * Use this on Railway Hobby/Free plans where outbound SMTP (465/587) is blocked.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BrevoApiEmailClient {

    private static final String BREVO_SEND_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;

    @Value("${app.mail.brevo-api-key:}")
    private String brevoApiKey;

    public boolean isConfigured() {
        return StringUtils.hasText(brevoApiKey);
    }

    public void sendHtmlEmail(String fromEmail, String fromName, String toEmail, String toName, String subject, String html) {
        if (!isConfigured()) {
            throw new IllegalStateException("Brevo API key is not configured");
        }
        if (!StringUtils.hasText(fromEmail)) {
            throw new IllegalArgumentException("Sender email (MAIL_FROM) is required for Brevo");
        }

        Map<String, Object> sender = new HashMap<>();
        sender.put("email", fromEmail.trim());
        if (StringUtils.hasText(fromName)) {
            sender.put("name", fromName.trim());
        }

        Map<String, Object> recipient = new HashMap<>();
        recipient.put("email", toEmail.trim());
        if (StringUtils.hasText(toName)) {
            recipient.put("name", toName.trim());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("sender", sender);
        body.put("to", List.of(recipient));
        body.put("subject", subject);
        body.put("htmlContent", html);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey.trim());

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BREVO_SEND_URL, new HttpEntity<>(body, headers), String.class);
            log.info("[EMAIL-BREVO] Sent '{}' to {} (HTTP {})", subject, toEmail, response.getStatusCode().value());
        } catch (RestClientException e) {
            log.error("[EMAIL-BREVO] Failed sending '{}' to {}", subject, toEmail, e);
            throw e;
        }
    }
}
