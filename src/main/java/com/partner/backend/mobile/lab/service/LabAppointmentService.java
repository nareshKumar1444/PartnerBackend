package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.service.EmailService;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.mobile.lab.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabAppointmentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final LabAppointmentRepository labAppointmentRepository;
    private final EmailService emailService;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public Page<LabAppointmentResponse> list(Long labId, Pageable pageable) {
        return labAppointmentRepository.findByLabId(labId, pageable).map(this::toResponse);
    }

    @Transactional
    public LabAppointmentResponse updateStatus(Long labId, Long appointmentId, LabAppointmentStatusRequest req) {
        LabAppointment appt = labAppointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab appointment", appointmentId));

        if (!appt.getLab().getId().equals(labId)) {
            throw new UnauthorizedException("Access denied");
        }

        LabAppointmentStatus previousStatus = appt.getStatus();
        appt.setStatus(req.getStatus());
        LabAppointment saved = labAppointmentRepository.save(appt);

        sendStatusChangeEmail(saved, req.getStatus());
        if (previousStatus != req.getStatus()) {
            providerNotificationService.notifyPatientLabStatus(saved, req.getStatus());
        }

        return toResponse(saved);
    }

    private void sendStatusChangeEmail(LabAppointment appt, LabAppointmentStatus newStatus) {
        String patientEmail = resolvePatientEmail(appt);
        if (patientEmail == null) {
            return;
        }
        String patientName = appt.getPatientName() != null && !appt.getPatientName().isBlank()
                ? appt.getPatientName() : "Patient";
        String labName = appt.getLab() != null && appt.getLab().getName() != null
                ? appt.getLab().getName() : "Laboratory";
        String testName = appt.getTest() != null && appt.getTest().getTestName() != null
                ? appt.getTest().getTestName() : "Lab Test";
        String dateLabel = appt.getScheduledDate() != null
                ? appt.getScheduledDate().format(DATE_FMT) : "—";
        String timeSlot = appt.getScheduledTimeSlot();

        switch (newStatus) {
            case CONFIRMED -> emailService.sendLabBookingConfirmedToPatient(
                    patientEmail, patientName, labName, testName, dateLabel, timeSlot);
            case CANCELLED -> emailService.sendLabBookingCancelledToPatient(
                    patientEmail, patientName, labName, testName, dateLabel, timeSlot);
            case COMPLETED -> emailService.sendLabTestCompletedToPatient(
                    patientEmail, patientName, labName, testName, dateLabel);
            default -> { /* PENDING — no notification needed */ }
        }
    }

    private String resolvePatientEmail(LabAppointment appt) {
        Patient patient = appt.getPatient();
        if (patient == null) return null;
        if (patient.getEmail() != null && !patient.getEmail().isBlank()) {
            return patient.getEmail().trim();
        }
        if (patient.getUser() != null
                && patient.getUser().getEmail() != null
                && !patient.getUser().getEmail().isBlank()) {
            return patient.getUser().getEmail().trim();
        }
        return null;
    }

    private LabAppointmentResponse toResponse(LabAppointment a) {
        return LabAppointmentResponse.builder()
                .id(a.getId())
                .patientName(a.getPatientName())
                .patientPhone(a.getPatientPhone())
                .testName(a.getTest().getTestName())
                .testPrice(a.getTest().getDiscountedPrice())
                .scheduledDate(a.getScheduledDate())
                .scheduledTimeSlot(a.getScheduledTimeSlot())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
