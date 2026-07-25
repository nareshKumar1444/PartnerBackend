package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.*;
import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.util.CnicNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminDoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorAdminMetricsService doctorAdminMetricsService;
    private final AdminProviderDeletionService providerDeletionService;

    @Transactional(readOnly = true)
    public Page<DoctorSummaryResponse> list(ProviderStatus status, Pageable pageable) {
        Page<Doctor> page = (status != null)
                ? doctorRepository.findByStatusAndDeletedFalse(status, pageable)
                : doctorRepository.findByDeletedFalse(pageable);
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public DoctorSummaryResponse getById(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        return toSummary(doctor);
    }

    @Transactional
    public DoctorSummaryResponse add(AddDoctorRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("Email already in use: " + req.getEmail());
        }
        String cnicNorm = (req.getCnic() == null || req.getCnic().isBlank())
                ? null
                : CnicNormalizer.normalize(req.getCnic().trim());
        if (req.getCnic() != null && !req.getCnic().isBlank()) {
            if (!CnicNormalizer.isValid13(cnicNorm)) {
                throw new BadRequestException("CNIC must be exactly 13 digits.");
            }
            if (userRepository.existsByNormalizedCnicAndRole(cnicNorm, UserRole.DOCTOR)) {
                throw new ConflictException("This CNIC is already registered as a Doctor.");
            }
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.DOCTOR)
                .normalizedCnic(cnicNorm)
                .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .name(req.getName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .pmdcNumber(req.getPmdcNumber())
                .specialty(req.getSpecialty())
                .experienceYears(req.getExperienceYears())
                .clinicName(req.getClinicName())
                .clinicAddress(req.getClinicAddress())
                .city(req.getCity())
                .virtualFee(req.getVirtualFee())
                .physicalFee(req.getPhysicalFee())
                .bio(req.getBio())
                .status(ProviderStatus.APPROVED)
                .build();
        return toSummary(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorSummaryResponse approve(Long id) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        doctor.setStatus(ProviderStatus.APPROVED);
        doctor.setRejectionReason(null);
        return toSummary(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorSummaryResponse updateBankAccount(Long id, UpdateBankAccountRequest req) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        doctor.setBankAccountTitle(req.getTitle().trim());
        doctor.setBankAccountNumber(req.getAccount().trim());
        doctor.setBankIban(req.getIban() != null ? req.getIban().trim() : null);
        doctor.setBankName(req.getBank().trim());
        return toSummary(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorSummaryResponse reject(Long id, RejectRequest req) {
        Doctor doctor = doctorRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        doctor.setStatus(ProviderStatus.REJECTED);
        doctor.setRejectionReason(req.getReason());
        return toSummary(doctorRepository.save(doctor));
    }

    @Transactional
    public void delete(Long id) {
        providerDeletionService.deleteDoctor(id);
    }

    private DoctorSummaryResponse toSummary(Doctor d) {
        User owner = d.getUser();
        String cnicDisplay = (owner != null && owner.getNormalizedCnic() != null)
                ? CnicNormalizer.formatPakistanDisplay(owner.getNormalizedCnic())
                : null;
        return DoctorSummaryResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .phone(d.getPhone())
                .specialty(d.getSpecialty())
                .city(d.getCity())
                .pmdcNumber(d.getPmdcNumber())
                .experienceYears(d.getExperienceYears())
                .virtualFee(d.getVirtualFee())
                .physicalFee(d.getPhysicalFee())
                .status(d.getStatus())
                .rejectionReason(d.getRejectionReason())
                .createdAt(d.getCreatedAt())
                .clinicName(d.getClinicName())
                .clinicAddress(d.getClinicAddress())
                .bio(d.getBio())
                .normalizedCnic(cnicDisplay)
                .totalPatients(doctorAdminMetricsService.patientCountForDoctor(d.getId()))
                .earnings(doctorAdminMetricsService.earningsForDoctor(d.getId()))
                .bankAccount(doctorAdminMetricsService.bankFrom(d))
                .build();
    }
}
