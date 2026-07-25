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
public class AdminLabService {

    private final LabRepository labRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LabAdminMetricsService labAdminMetricsService;
    private final AdminProviderDeletionService providerDeletionService;

    @Transactional(readOnly = true)
    public Page<LabSummaryResponse> list(ProviderStatus status, Pageable pageable) {
        Page<Lab> page = (status != null)
                ? labRepository.findByStatusAndDeletedFalse(status, pageable)
                : labRepository.findByDeletedFalse(pageable);
        return page.map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public LabSummaryResponse getById(Long id) {
        Lab lab = labRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", id));
        return toSummary(lab);
    }

    @Transactional
    public LabSummaryResponse add(AddLabRequest req) {
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
            if (userRepository.existsByNormalizedCnicAndRole(cnicNorm, UserRole.LAB)) {
                throw new ConflictException("This CNIC is already registered as a Laboratory account.");
            }
        }
        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.LAB)
                .normalizedCnic(cnicNorm)
                .build();
        userRepository.save(user);

        Lab lab = Lab.builder()
                .user(user)
                .name(req.getName())
                .ownerName(req.getOwnerName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .city(req.getCity())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .dnlcLicense(req.getDnlcLicense())
                .status(ProviderStatus.APPROVED)
                .build();
        return toSummary(labRepository.save(lab));
    }

    @Transactional
    public LabSummaryResponse approve(Long id) {
        Lab lab = labRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", id));
        lab.setStatus(ProviderStatus.APPROVED);
        lab.setRejectionReason(null);
        return toSummary(labRepository.save(lab));
    }

    @Transactional
    public LabSummaryResponse updateBankAccount(Long id, UpdateBankAccountRequest req) {
        Lab lab = labRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", id));
        lab.setBankAccountTitle(req.getTitle().trim());
        lab.setBankAccountNumber(req.getAccount().trim());
        lab.setBankIban(req.getIban() != null ? req.getIban().trim() : null);
        lab.setBankName(req.getBank().trim());
        return toSummary(labRepository.save(lab));
    }

    @Transactional
    public LabSummaryResponse reject(Long id, RejectRequest req) {
        Lab lab = labRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", id));
        lab.setStatus(ProviderStatus.REJECTED);
        lab.setRejectionReason(req.getReason());
        return toSummary(labRepository.save(lab));
    }

    @Transactional
    public void delete(Long id) {
        providerDeletionService.deleteLab(id);
    }

    private LabSummaryResponse toSummary(Lab l) {
        User owner = l.getUser();
        String cnicDisplay = (owner != null && owner.getNormalizedCnic() != null)
                ? CnicNormalizer.formatPakistanDisplay(owner.getNormalizedCnic())
                : null;
        return LabSummaryResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .ownerName(l.getOwnerName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .city(l.getCity())
                .address(l.getAddress())
                .dnlcLicense(l.getDnlcLicense())
                .status(l.getStatus())
                .rejectionReason(l.getRejectionReason())
                .createdAt(l.getCreatedAt())
                .normalizedCnic(cnicDisplay)
                .testsCompleted(labAdminMetricsService.completedTestsForLab(l.getId()))
                .earnings(labAdminMetricsService.earningsForLab(l.getId()))
                .bankAccount(labAdminMetricsService.bankFrom(l))
                .build();
    }
}
