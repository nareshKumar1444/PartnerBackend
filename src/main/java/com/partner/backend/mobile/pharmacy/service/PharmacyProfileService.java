package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.pharmacy.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PharmacyProfileService {

    private final PharmacyRepository pharmacyRepository;

    @Transactional(readOnly = true)
    public PharmacyProfileResponse getProfile(Long pharmacyId) {
        Pharmacy p = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", pharmacyId));
        return toResponse(p);
    }

    @Transactional
    public PharmacyProfileResponse updateProfile(Long pharmacyId, PharmacyProfileUpdateRequest req) {
        Pharmacy p = pharmacyRepository.findById(pharmacyId)
                .orElseThrow(() -> new ResourceNotFoundException("Pharmacy", pharmacyId));

        if (req.getName() != null) p.setName(req.getName());
        if (req.getOwnerName() != null) p.setOwnerName(req.getOwnerName());
        if (req.getPhone() != null) p.setPhone(req.getPhone());
        if (req.getAddress() != null) p.setAddress(req.getAddress());
        if (req.getCity() != null) p.setCity(req.getCity());
        if (req.getBankAccountTitle() != null) p.setBankAccountTitle(req.getBankAccountTitle().trim());
        if (req.getBankAccountNumber() != null) p.setBankAccountNumber(req.getBankAccountNumber().trim());
        if (req.getBankIban() != null) p.setBankIban(req.getBankIban().trim());
        if (req.getBankName() != null) p.setBankName(req.getBankName().trim());

        return toResponse(pharmacyRepository.save(p));
    }

    private PharmacyProfileResponse toResponse(Pharmacy p) {
        return PharmacyProfileResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .ownerName(p.getOwnerName())
                .email(p.getEmail())
                .phone(p.getPhone())
                .address(p.getAddress())
                .city(p.getCity())
                .drapLicense(p.getDrapLicense())
                .status(p.getStatus())
                .bankAccountTitle(p.getBankAccountTitle())
                .bankAccountNumber(p.getBankAccountNumber())
                .bankIban(p.getBankIban())
                .bankName(p.getBankName())
                .build();
    }
}
