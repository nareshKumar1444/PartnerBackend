package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.lab.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabProfileService {

    private final LabRepository labRepository;

    @Transactional(readOnly = true)
    public LabProfileResponse getProfile(Long labId) {
        Lab l = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));
        return toResponse(l);
    }

    @Transactional
    public LabProfileResponse updateProfile(Long labId, LabProfileUpdateRequest req) {
        Lab l = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));

        if (req.getName() != null) l.setName(req.getName());
        if (req.getOwnerName() != null) l.setOwnerName(req.getOwnerName());
        if (req.getEmail() != null) l.setEmail(req.getEmail());
        if (req.getPhone() != null) l.setPhone(req.getPhone());
        if (req.getAddress() != null) l.setAddress(req.getAddress());
        if (req.getCity() != null) l.setCity(req.getCity());
        if (req.getBankAccountTitle() != null) l.setBankAccountTitle(req.getBankAccountTitle().trim());
        if (req.getBankAccountNumber() != null) l.setBankAccountNumber(req.getBankAccountNumber().trim());
        if (req.getBankIban() != null) l.setBankIban(req.getBankIban().trim());
        if (req.getBankName() != null) l.setBankName(req.getBankName().trim());

        return toResponse(labRepository.save(l));
    }

    private LabProfileResponse toResponse(Lab l) {
        return LabProfileResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .ownerName(l.getOwnerName())
                .email(l.getEmail())
                .phone(l.getPhone())
                .address(l.getAddress())
                .city(l.getCity())
                .dnlcLicense(l.getDnlcLicense())
                .status(l.getStatus())
                .bankAccountTitle(l.getBankAccountTitle())
                .bankAccountNumber(l.getBankAccountNumber())
                .bankIban(l.getBankIban())
                .bankName(l.getBankName())
                .build();
    }
}
