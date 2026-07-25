package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.entity.PatientHealthReading;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.PatientHealthReadingRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.patient.dto.portal.PatientHealthReadingCreateRequest;
import com.partner.backend.patient.dto.portal.PatientHealthReadingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class PatientHealthReadingService {

    private final PatientHealthReadingRepository readingRepository;
    private final PatientRepository patientRepository;
    private final PatientHealthMetricCatalogService catalogService;

    @Transactional(readOnly = true)
    public List<PatientHealthReadingResponse> list(Long patientId) {
        return readingRepository.findByPatient_IdOrderByReadingDateDescCreatedAtDesc(patientId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PatientHealthReadingResponse create(Long patientId, @Valid PatientHealthReadingCreateRequest req) {
        String metricKey = req.getMetricKey().trim().toLowerCase();
        if (!catalogService.isActiveMetricKey(metricKey)) {
            throw new BadRequestException("Unknown or inactive health metric: " + metricKey);
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String unit = catalogService.resolveUnit(metricKey, req.getUnit());

        PatientHealthReading e = PatientHealthReading.builder()
                .patient(patient)
                .metricKey(metricKey)
                .valueText(req.getValue().trim())
                .unit(unit)
                .readingDate(req.getReadingDate())
                .note(req.getNote() != null ? req.getNote().trim() : null)
                .build();
        e = readingRepository.save(e);
        return toDto(e);
    }

    private PatientHealthReadingResponse toDto(PatientHealthReading e) {
        return PatientHealthReadingResponse.builder()
                .id(e.getId())
                .metricKey(PatientHealthMetricMapper.normalizeStoredKey(e.getMetricKey()))
                .value(e.getValueText())
                .unit(e.getUnit())
                .readingDate(e.getReadingDate())
                .note(e.getNote())
                .build();
    }
}
