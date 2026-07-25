package com.partner.backend.common.service;

import com.partner.backend.common.entity.LabAppointment;
import com.partner.backend.common.entity.LabAppointmentStatus;
import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.repository.LabAppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Runs daily at 01:00 AM Karachi time.
 * Any PENDING or CONFIRMED lab appointment whose scheduledDate is in the past
 * is automatically marked CANCELLED and the patient receives a no-show email.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LabAppointmentAutoCloseSchedulerService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final LabAppointmentRepository labAppointmentRepository;
    private final EmailService emailService;
    private final ProviderNotificationService providerNotificationService;

    @Scheduled(cron = "0 0 1 * * *", zone = "Asia/Karachi")
    @Transactional
    public void autoCancelExpiredAppointments() {
        runAutoCancelJob();
    }

    @Transactional
    public void runAutoCancelJob() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Karachi"));
        List<LabAppointment> expired = labAppointmentRepository.findExpiredPendingOrConfirmed(
                today, List.of(LabAppointmentStatus.PENDING, LabAppointmentStatus.CONFIRMED));

        if (expired.isEmpty()) {
            return;
        }

        log.info("[LAB-AUTO-CANCEL] Found {} expired appointment(s) to cancel (PKT {})", expired.size(), today);

        for (LabAppointment appt : expired) {
            appt.setStatus(LabAppointmentStatus.CANCELLED);
            labAppointmentRepository.save(appt);

            String patientEmail = resolvePatientEmail(appt);
            String patientName = (appt.getPatientName() != null && !appt.getPatientName().isBlank())
                    ? appt.getPatientName() : "Patient";
            String labName = (appt.getLab() != null && appt.getLab().getName() != null)
                    ? appt.getLab().getName() : "the lab";
            String testName = (appt.getTest() != null && appt.getTest().getTestName() != null)
                    ? appt.getTest().getTestName() : "Lab Test";
            String dateLabel = appt.getScheduledDate() != null
                    ? appt.getScheduledDate().format(DATE_FMT) : "—";
            String timeSlot = appt.getScheduledTimeSlot();

            emailService.sendLabBookingAutoCancelledToPatient(
                    patientEmail, patientName, labName, testName, dateLabel, timeSlot);
            providerNotificationService.notifyPatientLabAutoCancelled(appt);

            log.info("[LAB-AUTO-CANCEL] Cancelled appointment #{} (patient: {}, email: {})",
                    appt.getId(), patientName, patientEmail != null ? patientEmail : "none");
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
}
