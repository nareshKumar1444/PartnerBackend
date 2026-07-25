package com.partner.backend.common.service;

import com.partner.backend.common.entity.Appointment;
import com.partner.backend.common.entity.AppointmentStatus;
import com.partner.backend.common.entity.AppointmentType;
import com.partner.backend.common.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentLifecycleService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Karachi");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter SLOT_FMT = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final ProviderNotificationService providerNotificationService;

    @Scheduled(fixedDelayString = "${app.appointments.lifecycle-check-ms:60000}")
    public void processLifecycle() {
        LocalDateTime now = LocalDateTime.now(APP_ZONE);
        List<Appointment> scheduled = appointmentRepository.findByStatusWithRelations(AppointmentStatus.SCHEDULED);
        for (Appointment appointment : scheduled) {
            try {
                processOne(appointment, now);
            } catch (Exception e) {
                log.warn("Appointment lifecycle processing failed for appointment {}: {}", appointment.getId(), e.getMessage());
            }
        }
    }

    protected void processOne(Appointment appointment, LocalDateTime now) {
        LocalDateTime start = appointmentStart(appointment);
        if (start == null) {
            return;
        }
        LocalDateTime end = appointmentEnd(appointment, start);
        if (end.isBefore(now)) {
            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointmentRepository.save(appointment);
            sendAutoCancelEmail(appointment);
            providerNotificationService.notifyPatientAppointmentAutoCancelled(appointment);
            return;
        }
        if (appointment.getReminderSentAt() == null && !start.isAfter(now.plusMinutes(5))) {
            appointment.setReminderSentAt(now);
            appointmentRepository.save(appointment);
            sendReminderEmails(appointment);
        }
    }

    private void sendReminderEmails(Appointment appointment) {
        String dateLabel = appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "—";
        String timeSlot = normalizeTimeSlot(appointment.getTimeSlot());
        String venueLabel = buildVenueLabel(appointment);

        String patientEmail = appointment.getPatient() != null ? appointment.getPatient().getEmail() : null;
        String patientName = appointment.getPatientName();
        String doctorEmail = appointment.getDoctor() != null ? appointment.getDoctor().getEmail() : null;
        String doctorName = appointment.getDoctor() != null ? appointment.getDoctor().getName() : null;

        if (patientEmail != null && !patientEmail.isBlank()) {
            emailService.sendAppointmentReminderToPatient(
                    patientEmail,
                    patientName,
                    doctorName,
                    dateLabel,
                    timeSlot,
                    appointment.getDoctor() != null ? appointment.getDoctor().getPhone() : "",
                    venueLabel);
        }
        if (doctorEmail != null && !doctorEmail.isBlank()) {
            emailService.sendAppointmentReminderToDoctor(
                    doctorEmail,
                    doctorName,
                    patientName,
                    dateLabel,
                    timeSlot,
                    appointment.getPatientPhone(),
                    venueLabel);
        }
    }

    private void sendAutoCancelEmail(Appointment appointment) {
        String patientEmail = appointment.getPatient() != null ? appointment.getPatient().getEmail() : null;
        if (patientEmail == null || patientEmail.isBlank()) {
            return;
        }
        emailService.sendAppointmentAutoCancelledToPatient(
                patientEmail,
                appointment.getPatientName(),
                appointment.getDoctor() != null ? appointment.getDoctor().getName() : "Doctor",
                appointment.getDate() != null ? appointment.getDate().format(DATE_FMT) : "—",
                normalizeTimeSlot(appointment.getTimeSlot()));
    }

    private LocalDateTime appointmentStart(Appointment appointment) {
        if (appointment.getDate() == null || appointment.getTimeSlot() == null || appointment.getTimeSlot().isBlank()) {
            return null;
        }
        LocalTime time = parseSlotStart(appointment.getTimeSlot());
        return LocalDateTime.of(appointment.getDate(), time);
    }

    private LocalDateTime appointmentEnd(Appointment appointment, LocalDateTime start) {
        LocalTime end = parseSlotEnd(appointment.getTimeSlot());
        if (end == null) {
            return start.plusMinutes(30);
        }
        LocalDate endDate = appointment.getDate();
        if (!end.isAfter(start.toLocalTime())) {
            endDate = endDate.plusDays(1);
        }
        return LocalDateTime.of(endDate, end);
    }

    private LocalTime parseSlotStart(String raw) {
        return parseLooseTime(splitTimeSlot(raw)[0]);
    }

    private LocalTime parseSlotEnd(String raw) {
        String[] parts = splitTimeSlot(raw);
        if (parts.length < 2) {
            return null;
        }
        return parseLooseTime(parts[1]);
    }

    private String[] splitTimeSlot(String raw) {
        return raw.split("\\s*(?:-|–|—|to)\\s*", 2);
    }

    private LocalTime parseLooseTime(String raw) {
        String value = raw.trim().toUpperCase(Locale.US);
        for (DateTimeFormatter fmt : List.of(
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm", Locale.US))) {
            try {
                return LocalTime.parse(value, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new IllegalArgumentException("Invalid appointment time: " + raw);
    }

    private String normalizeTimeSlot(String raw) {
        try {
            String[] parts = splitTimeSlot(raw);
            String start = parseLooseTime(parts[0]).format(SLOT_FMT);
            if (parts.length < 2) {
                return start;
            }
            return start + " - " + parseLooseTime(parts[1]).format(SLOT_FMT);
        } catch (Exception e) {
            return raw;
        }
    }

    private String buildVenueLabel(Appointment appointment) {
        if (appointment.getType() == AppointmentType.VIRTUAL) {
            return "Virtual consultation";
        }
        String clinicName = appointment.getDoctor() != null ? appointment.getDoctor().getClinicName() : null;
        String clinicAddress = appointment.getDoctor() != null ? appointment.getDoctor().getClinicAddress() : null;
        if (clinicName != null && !clinicName.isBlank() && clinicAddress != null && !clinicAddress.isBlank()) {
            return clinicName + ", " + clinicAddress;
        }
        if (clinicName != null && !clinicName.isBlank()) {
            return clinicName;
        }
        if (clinicAddress != null && !clinicAddress.isBlank()) {
            return clinicAddress;
        }
        return "Physical consultation";
    }
}
