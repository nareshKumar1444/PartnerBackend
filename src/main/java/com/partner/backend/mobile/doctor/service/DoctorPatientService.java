package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.mobile.doctor.dto.PatientBriefResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorPatientService {

    private final PatientRepository patientRepository;

    public Page<PatientBriefResponse> listForDoctor(Long doctorId, Pageable pageable) {
        return patientRepository.findPatientsByDoctorId(doctorId, pageable).map(this::toBrief);
    }

    private PatientBriefResponse toBrief(Patient p) {
        return PatientBriefResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .age(p.getAge())
                .gender(p.getGender())
                .build();
    }
}
