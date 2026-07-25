package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.security.JwtUtil;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.common.util.CnicNormalizer;
import com.partner.backend.mobile.lab.dto.LabRegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LabRegistrationService {

    private final UserRepository userRepository;
    private final LabRepository labRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(LabRegisterRequest req) {
        String cnic = CnicNormalizer.normalize(req.getCnic());
        if (!CnicNormalizer.isValid13(cnic)) {
            throw new BadRequestException("CNIC must be exactly 13 digits (e.g. 12345-1234567-1).");
        }
        if (userRepository.existsByNormalizedCnicAndRole(cnic, UserRole.LAB)) {
            throw new ConflictException("This CNIC is already registered as a Laboratory account.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("An account is already registered with this email address.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.LAB)
                .normalizedCnic(cnic)
                .build();
        userRepository.save(user);

        Lab lab = Lab.builder()
                .user(user)
                .name(req.getName())
                .ownerName(req.getOwnerName())
                .phone(req.getPhone())
                .email(req.getEmail())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .city(req.getCity())
                .dnlcLicense(req.getDnlcLicense())
                .status(ProviderStatus.PENDING)
                .build();
        lab = labRepository.save(lab);

        Wallet wallet = Wallet.builder()
                .providerId(lab.getId())
                .providerType(ProviderType.LAB)
                .balance(BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), lab.getId());
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .providerId(lab.getId())
                .providerName(lab.getName())
                .providerStatus(lab.getStatus())
                .build();
    }
}
