package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.doctor.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorProfileService {

    private final DoctorRepository doctorRepository;
    private final AvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public DoctorProfileResponse getProfile(Long doctorId) {
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        return toResponse(d);
    }

    @Transactional
    public DoctorProfileResponse updateProfile(Long doctorId, DoctorProfileUpdateRequest req) {
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (req.getName() != null) d.setName(req.getName());
        if (req.getPhone() != null) d.setPhone(req.getPhone());
        if (req.getSpecialty() != null) d.setSpecialty(req.getSpecialty());
        if (req.getExperienceYears() != null) d.setExperienceYears(req.getExperienceYears());
        if (req.getClinicName() != null) d.setClinicName(req.getClinicName());
        if (req.getClinicAddress() != null) d.setClinicAddress(req.getClinicAddress());
        if (req.getCity() != null) d.setCity(req.getCity());
        if (req.getVirtualFee() != null) d.setVirtualFee(req.getVirtualFee());
        if (req.getPhysicalFee() != null) d.setPhysicalFee(req.getPhysicalFee());
        if (req.getBio() != null) d.setBio(req.getBio());
        if (req.getBankAccountTitle() != null) d.setBankAccountTitle(req.getBankAccountTitle().trim());
        if (req.getBankAccountNumber() != null) d.setBankAccountNumber(req.getBankAccountNumber().trim());
        if (req.getBankIban() != null) d.setBankIban(req.getBankIban().trim());
        if (req.getBankName() != null) d.setBankName(req.getBankName().trim());

        return toResponse(doctorRepository.save(d));
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId).stream()
                .map(a -> AvailabilityResponse.builder()
                        .id(a.getId())
                        .dayOfWeek(a.getDayOfWeek())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .available(a.isAvailable())
                        .maxPatients(a.getMaxPatients() != null ? a.getMaxPatients() : 20)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AvailabilityResponse> updateAvailability(Long doctorId, AvailabilityRequest req) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (req.getConsultationType() != null && !req.getConsultationType().isBlank()) {
            doctor.setConsultationType(req.getConsultationType().toUpperCase());
        }

        availabilityRepository.deleteByDoctorId(doctorId);

        List<Availability> slots = req.getSlots().stream()
                .map(s -> Availability.builder()
                        .doctor(doctor)
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .available(s.isAvailable())
                        .maxPatients(s.getMaxPatients() != null ? s.getMaxPatients() : 20)
                        .build())
                .collect(Collectors.toList());

        availabilityRepository.saveAll(slots);

        return getAvailability(doctorId);
    }

    private DoctorProfileResponse toResponse(Doctor d) {
        return DoctorProfileResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .phone(d.getPhone())
                .pmdcNumber(d.getPmdcNumber())
                .specialty(d.getSpecialty())
                .experienceYears(d.getExperienceYears())
                .clinicName(d.getClinicName())
                .clinicAddress(d.getClinicAddress())
                .city(d.getCity())
                .virtualFee(d.getVirtualFee())
                .physicalFee(d.getPhysicalFee())
                .bio(d.getBio())
                .consultationType(d.getConsultationType())
                .status(d.getStatus())
                .bankAccountTitle(d.getBankAccountTitle())
                .bankAccountNumber(d.getBankAccountNumber())
                .bankIban(d.getBankIban())
                .bankName(d.getBankName())
                .build();
    }
}
