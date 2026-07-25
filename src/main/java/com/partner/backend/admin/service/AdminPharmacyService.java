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
public class AdminPharmacyService {

    private final PharmacyRepository pharmacyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PharmacyAdminMetricsService pharmacyAdminMetricsService;
    private final AdminProviderDeletionService providerDeletionService;

    @Transactional(readOnly = true)
    public Page<PharmacySummaryResponse> list(ProviderStatus status, Pageable pageable) {
        Page<Pharmacy> page = (status != null)
                ? pharmacyRepository.findByStatusAndDeletedFalse(status, pageable)
                : pharmacyRepository.findByDeletedFalse(pageable);
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public PharmacySummaryResponse getById(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", id));
        return toSummary(pharmacy);
    }

    @Transactional
    public PharmacySummaryResponse add(AddPharmacyRequest req) {
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
            if (userRepository.existsByNormalizedCnicAndRole(cnicNorm, UserRole.PHARMACY)) {
                throw new ConflictException("This CNIC is already registered as a Pharmacy.");
            }
        }
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.PHARMACY)
                .normalizedCnic(cnicNorm)
                .build();
        userRepository.save(user);

        Pharmacy pharmacy = Pharmacy.builder()
                .user(user)
                .name(req.getName())
                .ownerName(req.getOwnerName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .drapLicense(req.getDrapLicense())
                .status(ProviderStatus.APPROVED)
                .build();
        return toSummary(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public PharmacySummaryResponse approve(Long id) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", id));
        pharmacy.setStatus(ProviderStatus.APPROVED);
        pharmacy.setRejectionReason(null);
        return toSummary(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public PharmacySummaryResponse updateBankAccount(Long id, UpdateBankAccountRequest req) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", id));
        pharmacy.setBankAccountTitle(req.getTitle().trim());
        pharmacy.setBankAccountNumber(req.getAccount().trim());
        pharmacy.setBankIban(req.getIban() != null ? req.getIban().trim() : null);
        pharmacy.setBankName(req.getBank().trim());
        return toSummary(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public PharmacySummaryResponse reject(Long id, RejectRequest req) {
        Pharmacy pharmacy = pharmacyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", id));
        pharmacy.setStatus(ProviderStatus.REJECTED);
        pharmacy.setRejectionReason(req.getReason());
        return toSummary(pharmacyRepository.save(pharmacy));
    }

    @Transactional
    public void delete(Long id) {
        providerDeletionService.deletePharmacy(id);
    }

    private PharmacySummaryResponse toSummary(Pharmacy p) {
        User owner = p.getUser();
        String cnicDisplay = (owner != null && owner.getNormalizedCnic() != null)
                ? CnicNormalizer.formatPakistanDisplay(owner.getNormalizedCnic())
                : null;
        return PharmacySummaryResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .ownerName(p.getOwnerName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .city(p.getCity())
                .address(p.getAddress())
                .drapLicense(p.getDrapLicense())
                .status(p.getStatus())
                .rejectionReason(p.getRejectionReason())
                .createdAt(p.getCreatedAt())
                .normalizedCnic(cnicDisplay)
                .totalOrders(pharmacyAdminMetricsService.orderCountForPharmacy(p.getId()))
                .earnings(pharmacyAdminMetricsService.earningsForPharmacy(p.getId()))
                .bankAccount(pharmacyAdminMetricsService.bankFrom(p))
                .build();
    }
}
