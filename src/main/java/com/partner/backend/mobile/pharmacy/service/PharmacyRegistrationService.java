package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.security.JwtUtil;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.common.util.CnicNormalizer;
import com.partner.backend.mobile.pharmacy.dto.PharmacyRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PharmacyRegistrationService {

    private final UserRepository userRepository;
    private final PharmacyRepository pharmacyRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(PharmacyRegisterRequest req) {
        String cnic = CnicNormalizer.normalize(req.getCnic());
        if (!CnicNormalizer.isValid13(cnic)) {
            throw new BadRequestException("CNIC must be exactly 13 digits (e.g. 12345-1234567-1).");
        }
        if (userRepository.existsByNormalizedCnicAndRole(cnic, UserRole.PHARMACY)) {
            throw new ConflictException("This CNIC is already registered as a Pharmacy.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("An account is already registered with this email address.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.PHARMACY)
                .normalizedCnic(cnic)
                .build();
        userRepository.save(user);

        Pharmacy pharmacy = Pharmacy.builder()
                .user(user)
                .name(req.getName())
                .ownerName(req.getOwnerName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .city(req.getCity())
                .drapLicense(req.getDrapLicense())
                .status(ProviderStatus.PENDING)
                .build();
        pharmacy = pharmacyRepository.save(pharmacy);

        Wallet wallet = Wallet.builder()
                .providerId(pharmacy.getId())
                .providerType(ProviderType.PHARMACY)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), pharmacy.getId());
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .providerId(pharmacy.getId())
                .providerName(pharmacy.getName())
                .providerStatus(pharmacy.getStatus())
                .build();
    }
}
