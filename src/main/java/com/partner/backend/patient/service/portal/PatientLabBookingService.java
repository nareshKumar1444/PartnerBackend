package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.LabAppointmentRepository;
import com.partner.backend.common.repository.LabRepository;
import com.partner.backend.common.repository.LabTestRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.common.service.EmailService;
import com.partner.backend.common.service.ProviderNotificationService;
import com.partner.backend.patient.dto.portal.PatientBookLabRequest;
import com.partner.backend.patient.dto.portal.PatientLabBookingSummaryResponse;
import com.partner.backend.patient.service.portal.PatientCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class PatientLabBookingService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final LabAppointmentRepository labAppointmentRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final PatientRepository patientRepository;
    private final PatientCatalogService catalogService;
    private final EmailService emailService;
    private final ProviderNotificationService providerNotificationService;

    @Transactional(readOnly = true)
    public List<PatientLabBookingSummaryResponse> list(Long patientId) {
        return labAppointmentRepository.findByPatient_IdOrderByScheduledDateDesc(patientId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional
    public PatientLabBookingSummaryResponse book(Long patientId, @Valid PatientBookLabRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        Lab lab = labRepository.findById(req.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Lab", req.getLabId()));
        if (lab.getStatus() != ProviderStatus.APPROVED) {
            throw new BadRequestException("This lab is not available.");
        }

        LabTest test = labTestRepository.findById(req.getTestId())
                .orElseThrow(() -> new ResourceNotFoundException("LabTest", req.getTestId()));
        if (!test.getLab().getId().equals(lab.getId())) {
            throw new BadRequestException("Selected test does not belong to this lab.");
        }

        LocalDate when = req.getScheduledDate();
        if (when.isBefore(LocalDate.now())) {
            throw new BadRequestException("Scheduled date must be today or later.");
        }

        String slot = req.getScheduledTimeSlot() == null ? "" : req.getScheduledTimeSlot().trim();
        if (slot.isEmpty()) {
            throw new BadRequestException("Please select a time slot.");
        }
        if (!catalogService.slotsForLab(lab.getId(), when).contains(slot)) {
            throw new BadRequestException("Selected time slot is not available.");
        }

        LabAppointment la = LabAppointment.builder()
                .lab(lab)
                .patient(patient)
                .patientName(patient.getName())
                .patientPhone(patient.getPhone())
                .test(test)
                .scheduledDate(when)
                .scheduledTimeSlot(slot)
                .status(LabAppointmentStatus.PENDING)
                .build();
        la = labAppointmentRepository.save(la);

        // In-app notification to the lab
        providerNotificationService.notifyLabNewAppointment(la);

        // Email the lab about the new booking
        String labEmail = resolveLabEmail(lab);
        String labRecipientName = (lab.getOwnerName() != null && !lab.getOwnerName().isBlank())
                ? lab.getOwnerName() : lab.getName();
        String dateLabel = when.format(DATE_FMT);
        emailService.sendLabNewBookingToLab(
                labEmail, labRecipientName, patient.getName(),
                test.getTestName(), dateLabel, slot, patient.getPhone());

        return toSummary(la);
    }

    private String resolveLabEmail(Lab lab) {
        if (lab.getEmail() != null && !lab.getEmail().isBlank()) {
            return lab.getEmail().trim();
        }
        if (lab.getUser() != null
                && lab.getUser().getEmail() != null
                && !lab.getUser().getEmail().isBlank()) {
            return lab.getUser().getEmail().trim();
        }
        return null;
    }

    private PatientLabBookingSummaryResponse toSummary(LabAppointment la) {
        LabTest t = la.getTest();
        BigDecimal price = t.getDiscountedPrice() != null
                ? t.getDiscountedPrice()
                : (t.getNormalPrice() != null ? t.getNormalPrice() : BigDecimal.ZERO);
        return PatientLabBookingSummaryResponse.builder()
                .id(la.getId())
                .labId(la.getLab().getId())
                .labName(la.getLab().getName())
                .testId(t.getId())
                .testName(t.getTestName())
                .scheduledDate(la.getScheduledDate())
                .scheduledTimeSlot(la.getScheduledTimeSlot())
                .price(price)
                .backendStatus(la.getStatus())
                .build();
    }
}
