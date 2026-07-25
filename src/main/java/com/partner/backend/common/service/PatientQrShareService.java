package com.partner.backend.common.service;

import com.partner.backend.common.dto.PatientQrShareRequest;
import com.partner.backend.common.dto.PatientQrShareResponse;
import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.entity.PatientQrShare;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.PatientQrShareRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PatientQrShareService {

    private final PatientRepository patientRepository;
    private final PatientQrShareRepository patientQrShareRepository;

    @Transactional
    public PatientQrShareResponse save(Long patientId, PatientQrShareRequest request) {
        if (request == null || request.getPayload() == null) {
            throw new BadRequestException("QR payload is required.");
        }
        String accessCode = normalizeAccessCode(request.getAccessCode());
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        PatientQrShare share = patientQrShareRepository.findByAccessCode(accessCode)
                .orElseGet(PatientQrShare::new);
        share.setPatient(patient);
        share.setAccessCode(accessCode);
        share.setPayloadJson(request.getPayload().toString());
        int minutes = (request.getExpiresMinutes() != null && request.getExpiresMinutes() > 0)
                ? Math.min(request.getExpiresMinutes(), 10080)
                : 5;
        share.setExpiresAt(LocalDateTime.now().plusMinutes(minutes));

        return toResponse(patientQrShareRepository.save(share));
    }

    @Transactional(readOnly = true)
    public PatientQrShareResponse getByAccessCode(String accessCode) {
        return patientQrShareRepository.findByAccessCode(normalizeAccessCode(accessCode))
                .filter(share -> share.getExpiresAt() == null
                        || share.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(this::toResponse)
                .orElse(null);
    }

    private PatientQrShareResponse toResponse(PatientQrShare share) {
        return PatientQrShareResponse.builder()
                .id(share.getId())
                .patientId(share.getPatient() != null ? share.getPatient().getId() : null)
                .accessCode(share.getAccessCode())
                .payloadJson(share.getPayloadJson())
                .createdAt(share.getCreatedAt())
                .expiresAt(share.getExpiresAt())
                .build();
    }

    private String normalizeAccessCode(String accessCode) {
        if (accessCode == null || accessCode.isBlank()) {
            throw new BadRequestException("Access code is required.");
        }
        return accessCode.trim().toUpperCase(Locale.US);
    }
}
