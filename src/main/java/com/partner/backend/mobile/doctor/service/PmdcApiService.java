package com.partner.backend.mobile.doctor.service;

import com.partner.backend.mobile.doctor.dto.PmdcRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
@Service
@RequiredArgsConstructor
public class PmdcApiService {

    private final RestTemplate restTemplate;

    private static final String PMDC_URL =
            "https://hospitals-inspections.pmdc.pk/api/DRC/GetData";

    public boolean verifyByRegistrationNo(String registrationNo) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Accept", "application/json, text/javascript, */*; q=0.01");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("RegistrationNo", registrationNo);
        body.add("Name", "");
        body.add("FatherName", "");

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    PMDC_URL,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()
                    && response.getBody() != null) {

                String res = response.getBody();

                // simple validation (safe fallback)
                return res.contains(registrationNo);
            }

        } catch (Exception e) {
            System.out.println("PMDC API error: " + e.getMessage());
        }

        return false;
    }
}