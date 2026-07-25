package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.Consultation;
import com.partner.backend.common.entity.Doctor;
import com.partner.backend.common.entity.PrescriptionMedicine;
import com.partner.backend.common.repository.ConsultationRepository;
import com.partner.backend.patient.dto.portal.PatientPrescriptionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientPrescriptionService {

    private final ConsultationRepository consultationRepository;

    @Transactional(readOnly = true)
    public List<PatientPrescriptionResponse> list(Long patientId) {
        return consultationRepository.findDistinctByPatientId(patientId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private PatientPrescriptionResponse toDto(Consultation c) {
        Doctor d = c.getAppointment().getDoctor();
        List<String> meds = c.getMedicines().stream()
                .map(this::formatMedicine)
                .toList();
        return PatientPrescriptionResponse.builder()
                .id(c.getId())
                .doctorName(d.getName())
                .clinicName(d.getClinicName())
                .recordedAt(c.getCreatedAt())
                .diagnosis(c.getDiagnosis() != null ? c.getDiagnosis() : "")
                .notes(c.getNotes())
                .medicines(meds)
                .build();
    }

    private String formatMedicine(PrescriptionMedicine m) {
        StringBuilder sb = new StringBuilder(m.getName());
        if (m.getDosage() != null && !m.getDosage().isBlank()) {
            sb.append(" — ").append(m.getDosage().trim());
        }
        if (m.getFrequency() != null && !m.getFrequency().isBlank()) {
            sb.append(" (").append(m.getFrequency().trim()).append(")");
        }
        return sb.toString();
    }
}
