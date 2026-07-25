package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.mobile.pharmacy.dto.DrapVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DrapVerificationService {

    private final RestTemplate restTemplate;

    public DrapVerifyResponse verifyDrap(String drapLicenseNumber) {
        String normalized = drapLicenseNumber == null ? "" : drapLicenseNumber.trim();
        if (normalized.isEmpty()) {
            return notVerified();
        }

        String url = "https://eapp.dra.gov.pk/productView.php";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("webRegNo", normalized);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                request,
                String.class
        );

        boolean verified = isDrapLicenseFound(stripUtf8Bom(response.getBody()));

        return DrapVerifyResponse.builder()
                .verified(verified)
                .message(verified
                        ? "DRAP license verified successfully."
                        : "DRAP license number is not valid.")
                .build();
    }

    /** DRAP returns UTF-8 BOM + HTML for hits, or BOM + "No Data Found" for misses. */
    static String stripUtf8Bom(String body) {
        if (body == null) {
            return "";
        }
        return body.replace("\uFEFF", "").trim();
    }

    static boolean isDrapLicenseFound(String body) {
        if (body.isBlank()) {
            return false;
        }
        String lower = body.toLowerCase(Locale.ROOT);
        if (lower.contains("no data found")) {
            return false;
        }
        return lower.contains("product general details")
                || lower.contains("product_view_web.php");
    }

    private static DrapVerifyResponse notVerified() {
        return DrapVerifyResponse.builder()
                .verified(false)
                .message("DRAP license number is not valid.")
                .build();
    }
}
