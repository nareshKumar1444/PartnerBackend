package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.AppointmentRepository;
import com.partner.backend.common.repository.DoctorRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.patient.dto.portal.PatientAppointmentSummaryResponse;
import com.partner.backend.patient.dto.portal.PatientBookAppointmentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Validated
@RequiredArgsConstructor
public class PatientAppointmentBookingService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ProviderNotificationService providerNotificationService;
    private final PatientCatalogService patientCatalogService;

    @Transactional(readOnly = true)
    public List<PatientAppointmentSummaryResponse> list(Long patientId) {
        return appointmentRepository.findByPatient_IdOrderByDateDesc(patientId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public PatientAppointmentSummaryResponse book(Long patientId, @Valid PatientBookAppointmentRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        Doctor doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", req.getDoctorId()));
        if (doctor.getStatus() != ProviderStatus.APPROVED) {
            throw new BadRequestException("This doctor is not available for booking.");
        }

        LocalDate date = req.getDate();
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Appointment date must be today or later.");
        }

        if (!patientCatalogService.slotsForDoctor(doctor.getId(), date).contains(req.getTimeSlot())) {
            throw new BadRequestException("This time slot is not available for the selected date.");
        }

        if (date.isEqual(LocalDate.now()) && !isFutureSlot(req.getTimeSlot())) {
            throw new BadRequestException("This time slot has already passed.");
        }

        if (appointmentRepository.existsByDoctor_IdAndDateAndTimeSlot(
                doctor.getId(), date, req.getTimeSlot())) {
            throw new BadRequestException("This time slot is already taken. Please pick another.");
        }

        BigDecimal fee = req.getType() == AppointmentType.VIRTUAL
                ? doctor.getVirtualFee()
                : doctor.getPhysicalFee();
        if (fee == null) {
            fee = BigDecimal.ZERO;
        }

        Appointment a = Appointment.builder()
                .doctor(doctor)
                .patient(patient)
                .patientName(patient.getName())
                .patientPhone(patient.getPhone())
                .date(date)
                .timeSlot(req.getTimeSlot())
                .type(req.getType())
                .status(AppointmentStatus.SCHEDULED)
                .fee(fee)
                .build();
        a = appointmentRepository.save(a);
        providerNotificationService.notifyDoctorNewAppointment(a);
        return toSummary(a);
    }

    private PatientAppointmentSummaryResponse toSummary(Appointment a) {
        Doctor d = a.getDoctor();
        return PatientAppointmentSummaryResponse.builder()
                .id(a.getId())
                .doctorId(d.getId())
                .doctorName(d.getName())
                .specialty(d.getSpecialty())
                .date(a.getDate())
                .timeSlot(a.getTimeSlot())
                .type(a.getType())
                .backendStatus(a.getStatus())
                .fee(a.getFee())
                .build();
    }

    private boolean isFutureSlot(String slot) {
        LocalTime slotTime = parseLooseTime(slot);
        return slotTime.isAfter(LocalTime.now());
    }

    private LocalTime parseLooseTime(String slot) {
        String value = slot == null ? "" : slot.trim().toUpperCase(Locale.US);
        List<DateTimeFormatter> formats = Arrays.asList(
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("h:mm a", Locale.US),
                DateTimeFormatter.ofPattern("HH:mm", Locale.US));
        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalTime.parse(value, fmt);
            } catch (DateTimeParseException ignored) {
                // try next format
            }
        }
        throw new BadRequestException("Invalid time slot format.");
    }
}
