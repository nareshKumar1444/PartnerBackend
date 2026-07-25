package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.mobile.doctor.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionMedicineRepository medicineRepository;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public Page<ConsultationResponse> listConsultations(Long doctorId, Pageable pageable) {
        return consultationRepository.findByDoctorId(doctorId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ConsultationResponse getByAppointment(Long doctorId, Long appointmentId) {
        Consultation c = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found for appointment " + appointmentId));

        if (!c.getAppointment().getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("Access denied");
        }
        return mapToResponse(c);
    }

    @Transactional
    public ConsultationResponse saveConsultation(Long doctorId, ConsultationRequest req) {
        Appointment appointment = appointmentRepository.findById(req.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", req.getAppointmentId()));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("Access denied");
        }

        Consultation consultation = consultationRepository.findByAppointmentId(req.getAppointmentId())
                .orElse(Consultation.builder().appointment(appointment).build());

        consultation.setSymptoms(req.getSymptoms());
        consultation.setDiagnosis(req.getDiagnosis());
        consultation.setNotes(req.getNotes());

        if (consultation.getMedicines() != null) {
            consultation.getMedicines().clear();
        }

        if (req.getMedicines() != null) {
            List<PrescriptionMedicine> meds = req.getMedicines().stream()
                    .map(m -> {
                        PrescriptionMedicine med = new PrescriptionMedicine();
                        med.setConsultation(consultation);
                        med.setName(m.getName());
                        med.setDosage(m.getDosage());
                        med.setFrequency(m.getFrequency());
                        med.setDuration(m.getDuration());
                        med.setInstructions(m.getInstructions());
                        return med;
                    }).collect(Collectors.toList());
            consultation.getMedicines().addAll(meds);
        }

        Consultation saved = consultationRepository.save(consultation);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);
        providerNotificationService.notifyPatientAppointmentCompleted(appointment);

        return mapToResponse(saved);
    }

    private ConsultationResponse mapToResponse(Consultation c) {
        List<ConsultationResponse.MedicineResponse> meds = c.getMedicines().stream()
                .map(m -> ConsultationResponse.MedicineResponse.builder()
                        .id(m.getId())
                        .name(m.getName())
                        .dosage(m.getDosage())
                        .frequency(m.getFrequency())
                        .duration(m.getDuration())
                        .instructions(m.getInstructions())
                        .build())
                .collect(Collectors.toList());

        return ConsultationResponse.builder()
                .id(c.getId())
                .appointmentId(c.getAppointment().getId())
                .patientName(c.getAppointment().getPatientName())
                .symptoms(c.getSymptoms())
                .diagnosis(c.getDiagnosis())
                .notes(c.getNotes())
                .medicines(meds)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
