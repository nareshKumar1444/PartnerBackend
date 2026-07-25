package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.*;
import com.partner.backend.patient.dto.portal.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.partner.backend.common.util.InventoryItemExpiryRules;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientCatalogService {

    private static final DateTimeFormatter SLOT_OUT = DateTimeFormatter.ofPattern("hh:mm a", Locale.US);

    private final DoctorRepository doctorRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final PharmacyRepository pharmacyRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final AvailabilityRepository availabilityRepository;
    private final LabAvailabilityRepository labAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final LabAppointmentRepository labAppointmentRepository;

    @Transactional(readOnly = true)
    public Page<PatientDoctorSummaryResponse> listDoctors(Pageable pageable) {
        return doctorRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED, pageable)
                .map(this::toDoctorSummary);
    }

    @Transactional(readOnly = true)
    public PatientDoctorDetailResponse getDoctor(Long id) {
        Doctor d = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        if (d.getStatus() != ProviderStatus.APPROVED || d.isDeleted()) {
            throw new ResourceNotFoundException("Doctor", id);
        }
        return toDoctorDetail(d, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<String> slotsForDoctor(Long doctorId, LocalDate date) {
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        if (d.getStatus() != ProviderStatus.APPROVED || d.isDeleted()) {
            throw new ResourceNotFoundException("Doctor", doctorId);
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Date must be today or in the future.");
        }
        return buildSlotsForDate(d, date);
    }

    @Transactional(readOnly = true)
    public List<String> slotsForLab(Long labId, LocalDate date) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));
        if (lab.getStatus() != ProviderStatus.APPROVED || lab.isDeleted()) {
            throw new ResourceNotFoundException("Lab", labId);
        }
        if (date.isBefore(LocalDate.now())) {
            throw new BadRequestException("Date must be today or in the future.");
        }
        return buildLabSlotsForDate(lab, date);
    }

    @Transactional(readOnly = true)
    public Page<PatientLabSummaryResponse> listLabs(Pageable pageable) {
        return labRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED, pageable).map(this::toLabSummary);
    }

    @Transactional(readOnly = true)
    public List<PatientLabTestResponse> listTestsForLab(Long labId) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));
        if (lab.getStatus() != ProviderStatus.APPROVED || lab.isDeleted()) {
            throw new ResourceNotFoundException("Lab", labId);
        }
        return labTestRepository.findByLabId(labId, Pageable.unpaged())
                .map(t -> PatientLabTestResponse.builder()
                        .id(t.getId())
                        .testName(t.getTestName())
                        .normalPrice(t.getNormalPrice())
                        .discountedPrice(t.getDiscountedPrice())
                        .category(t.getCategory())
                        .description(t.getDescription())
                        .build())
                .getContent();
    }

    @Transactional(readOnly = true)
    public Page<PatientPharmacySummaryResponse> listPharmacies(Pageable pageable) {
        return pharmacyRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED, pageable).map(this::toPhSummary);
    }

    @Transactional(readOnly = true)
    public Page<PatientInventoryItemResponse> inventory(Long pharmacyId, String query, Pageable pageable) {
        Pharmacy ph = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", pharmacyId));
        if (ph.getStatus() != ProviderStatus.APPROVED || ph.isDeleted()) {
            throw new ResourceNotFoundException("Pharmacy", pharmacyId);
        }
        LocalDate today = InventoryItemExpiryRules.todayInPakistan();
        Page<InventoryItem> page = (query != null && !query.isBlank())
                ? inventoryItemRepository.searchSellableByPharmacyId(
                        pharmacyId, query.trim(), InventoryItemStatus.ACTIVE, today, pageable)
                : inventoryItemRepository.findSellableByPharmacyId(
                        pharmacyId, InventoryItemStatus.ACTIVE, today, pageable);
        return page.map(PatientCatalogService.this::toInv);
    }

    private PatientInventoryItemResponse toInv(InventoryItem i) {
        return PatientInventoryItemResponse.builder()
                .id(i.getId())
                .medicineName(i.getMedicineName())
                .quantityAvailable(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .category(i.getCategory())
                .expiryDate(i.getExpiryDate())
                .build();
    }

    private PatientDoctorSummaryResponse toDoctorSummary(Doctor d) {
        return PatientDoctorSummaryResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .specialty(d.getSpecialty())
                .city(d.getCity())
                .clinicName(d.getClinicName())
                .physicalFee(d.getPhysicalFee())
                .virtualFee(d.getVirtualFee())
                .consultationType(d.getConsultationType())
                .build();
    }

    private PatientDoctorDetailResponse toDoctorDetail(Doctor d, LocalDate date) {
        return PatientDoctorDetailResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .specialty(d.getSpecialty())
                .city(d.getCity())
                .clinicName(d.getClinicName())
                .clinicAddress(d.getClinicAddress())
                .physicalFee(d.getPhysicalFee())
                .virtualFee(d.getVirtualFee())
                .consultationType(d.getConsultationType())
                .bio(d.getBio())
                .slots(buildSlotsForDate(d, date))
                .build();
    }

    private PatientLabSummaryResponse toLabSummary(Lab l) {
        return PatientLabSummaryResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .city(l.getCity())
                .address(l.getAddress())
                .phone(l.getPhone())
                .build();
    }

    private PatientPharmacySummaryResponse toPhSummary(Pharmacy p) {
        return PatientPharmacySummaryResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .city(p.getCity())
                .address(p.getAddress())
                .phone(p.getPhone())
                .build();
    }

    private List<String> buildSlotsForDate(Doctor doctor, LocalDate date) {
        WeekDay wd = toWeekDay(date);
        List<Availability> dayAvailability = availabilityRepository.findByDoctorId(doctor.getId()).stream()
                .filter(a -> a.getDayOfWeek() == wd && a.isAvailable())
                .sorted(Comparator.comparing(a -> parseLoose(a.getStartTime() != null ? a.getStartTime() : "09:00")))
                .toList();

        List<String> generatedSlots = dayAvailability.stream()
                .flatMap(a -> generateHalfHourSlots(a.getStartTime(), a.getEndTime()).stream())
                .distinct()
                .toList();

        Set<String> takenSlots = appointmentRepository.findByDoctorIdAndDate(doctor.getId(), date).stream()
                .map(Appointment::getTimeSlot)
                .filter(slot -> slot != null && !slot.isBlank())
                .collect(Collectors.toSet());

        return generatedSlots.stream()
                .filter(slot -> !takenSlots.contains(slot))
                .filter(slot -> isFutureSlotForDate(date, slot))
                .limit(maxPatientsForDay(dayAvailability))
                .toList();
    }

    private List<String> buildLabSlotsForDate(Lab lab, LocalDate date) {
        WeekDay wd = toWeekDay(date);
        List<LabAvailability> dayAvailability = labAvailabilityRepository.findByLabId(lab.getId()).stream()
                .filter(a -> a.getDayOfWeek() == wd && a.isAvailable())
                .sorted(Comparator.comparing(a -> parseLoose(a.getStartTime() != null ? a.getStartTime() : "09:00")))
                .toList();

        if (dayAvailability.isEmpty()) {
            return List.of();
        }

        List<String> generatedSlots = dayAvailability.stream()
                .flatMap(a -> generateHalfHourSlots(a.getStartTime(), a.getEndTime()).stream())
                .distinct()
                .toList();

        Set<String> takenSlots = labAppointmentRepository.findByLabIdAndScheduledDate(lab.getId(), date).stream()
                .map(LabAppointment::getScheduledTimeSlot)
                .filter(slot -> slot != null && !slot.isBlank())
                .collect(Collectors.toSet());

        return generatedSlots.stream()
                .filter(slot -> !takenSlots.contains(slot))
                .filter(slot -> isFutureSlotForDate(date, slot))
                .toList();
    }

    private int maxPatientsForDay(List<Availability> dayAvailability) {
        return dayAvailability.stream()
                .map(Availability::getMaxPatients)
                .filter(max -> max != null && max > 0)
                .findFirst()
                .orElse(Integer.MAX_VALUE);
    }

//    private WeekDay toWeekDay(LocalDate d) {
//        return switch (d.getDayOfWeek()) {
//            case MONDAY -> WeekDay.MON;
//            case TUESDAY -> WeekDay.TUE;
//            case WEDNESDAY -> WeekDay.WED;
//            case THURSDAY -> WeekDay.THU;
//            case FRIDAY -> WeekDay.FRI;
//            case SATURDAY -> WeekDay.SAT;
//            case SUNDAY -> WeekDay.SUN;
//        };
//    }
private WeekDay toWeekDay(LocalDate d) {
    switch (d.getDayOfWeek()) {
        case MONDAY:
            return WeekDay.MON;
        case TUESDAY:
            return WeekDay.TUE;
        case WEDNESDAY:
            return WeekDay.WED;
        case THURSDAY:
            return WeekDay.THU;
        case FRIDAY:
            return WeekDay.FRI;
        case SATURDAY:
            return WeekDay.SAT;
        case SUNDAY:
            return WeekDay.SUN;
        default:
            throw new IllegalArgumentException("Invalid day");
    }
}
    private List<String> generateHalfHourSlots(String startRaw, String endRaw) {
        LocalTime start = parseLoose(startRaw != null ? startRaw : "09:00");
        LocalTime end = parseLoose(endRaw != null ? endRaw : "17:00");
        if (!end.isAfter(start)) {
            return List.of("09:00 AM", "10:00 AM", "11:00 AM");
        }
        List<String> out = new ArrayList<>();
        LocalTime cur = start;
        while (cur.plusMinutes(30).compareTo(end) <= 0) {
            out.add(cur.format(SLOT_OUT));
            cur = cur.plusMinutes(30);
        }
        if (out.isEmpty()) {
            out.add(start.format(SLOT_OUT));
        }
        return out;
    }

    private LocalTime parseLoose(String s) {
        String t = s.trim();
        List<DateTimeFormatter> formats = List.of(
                DateTimeFormatter.ofPattern("HH:mm", Locale.US),
                DateTimeFormatter.ofPattern("hh:mm a", Locale.US),
                DateTimeFormatter.ofPattern("h:mm a", Locale.US));
        for (DateTimeFormatter fmt : formats) {
            try {
                return LocalTime.parse(t.toUpperCase(Locale.US), fmt);
            } catch (DateTimeParseException ignored) {
                // Try the next common mobile time format.
            }
        }
        return LocalTime.parse("09:00");
    }

    private boolean isFutureSlotForDate(LocalDate date, String slot) {
        if (!date.isEqual(LocalDate.now())) {
            return true;
        }
        return parseLoose(slot).isAfter(LocalTime.now());
    }
}
