package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.mobile.doctor.dto.AppointmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DoctorAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> list(Long doctorId, Pageable pageable) {
        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::toResponse);
    }

    @Transactional
    public AppointmentResponse startConsultation(Long doctorId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("You are not authorized to access this appointment");
        }

        ensureToday(appointment);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only SCHEDULED appointments can be started");
        }

        appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long doctorId, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));

        if (!appointment.getDoctor().getId().equals(doctorId)) {
            throw new UnauthorizedException("You are not authorized to access this appointment");
        }

        ensureToday(appointment);

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BadRequestException("Only SCHEDULED appointments can be cancelled");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        providerNotificationService.notifyPatientAppointmentCancelled(appointment);
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment a) {
        Integer age = null;
        String gender = null;
        if (a.getPatient() != null) {
            age = a.getPatient().getAge();
            gender = a.getPatient().getGender();
        }

        return AppointmentResponse.builder()
                .id(a.getId())
                .patientName(a.getPatientName())
                .patientPhone(a.getPatientPhone())
                .patientAge(age)
                .patientGender(gender)
                .date(a.getDate())
                .timeSlot(a.getTimeSlot())
                .type(a.getType())
                .status(a.getStatus())
                .fee(a.getFee())
                .createdAt(a.getCreatedAt())
                .build();
    }

    private void ensureToday(Appointment appointment) {
        if (appointment.getDate() == null || !appointment.getDate().isEqual(LocalDate.now())) {
            throw new BadRequestException("This appointment can only be managed on the appointment date.");
        }
    }
}
