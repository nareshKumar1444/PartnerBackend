package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.PatientResponse;
import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.entity.User;
import com.partner.backend.common.repository.ConsultationRepository;
import com.partner.backend.common.repository.LabAppointmentRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.common.util.CnicNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPatientService {

    private final PatientRepository patientRepository;
    private final LabAppointmentRepository labAppointmentRepository;
    private final ConsultationRepository consultationRepository;

    public Page<PatientResponse> list(Pageable pageable) {
        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    private PatientResponse toResponse(Patient p) {
        User u = p.getUser();
        String cnicDisplay = (u != null && u.getNormalizedCnic() != null)
                ? CnicNormalizer.formatPakistanDisplay(u.getNormalizedCnic())
                : null;
        return PatientResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .age(p.getAge())
                .gender(p.getGender())
                .city(p.getCity())
                .createdAt(p.getCreatedAt())
                .normalizedCnic(cnicDisplay)
                .recordsCount(labAppointmentRepository.countByPatient_Id(p.getId()))
                .prescriptionsCount(consultationRepository.countByPatientId(p.getId()))
                .build();
    }
}
