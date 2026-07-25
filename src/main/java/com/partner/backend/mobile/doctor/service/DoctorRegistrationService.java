package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.doctor.dto.DoctorRegisterRequest;
import com.partner.backend.mobile.doctor.dto.PmdcVerifyResponse;
import com.partner.backend.common.security.JwtUtil;
import com.partner.backend.common.util.CnicNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DoctorRegistrationService {
    private static final String HARDCODED_PMDC = "3842-A";

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final PmdcApiService pmdcApiService;
    @Transactional
    public AuthResponse register(DoctorRegisterRequest req) {
        Integer step = req.getStep();
        if (step != null && (step < 1 || step > 3)) {
            throw new BadRequestException("Invalid registration step. Must be 1, 2, or 3");
        }
        // Mobile collects data across wizard screens but submits once — step is often omitted.
        // Any omitted or valid step (1–3) performs full registration in one request.
        return handleStep1(req);
    }

    public PmdcVerifyResponse verifyPmdc(String pmdcNumber) {
//        String normalizedInput = normalizePmdc(pmdcNumber);
//        String normalizedExpected = normalizePmdc(HARDCODED_PMDC);
//        boolean isVerified = normalizedExpected.equals(normalizedInput);
        boolean isVerified =
                pmdcApiService.verifyByRegistrationNo(pmdcNumber);
        return PmdcVerifyResponse.builder()
                .verified(isVerified)
                .message(isVerified
                        ? "PMDC number verified successfully."
                        : "PMDC number is not valid.")
                .build();
    }

    private String normalizePmdc(String value) {
        if (value == null) return "";
        return value.trim().toUpperCase().replaceAll("\\s+", "");
    }

    private AuthResponse handleStep1(DoctorRegisterRequest req) {
        String cnic = CnicNormalizer.normalize(req.getCnic());
        if (!CnicNormalizer.isValid13(cnic)) {
            throw new BadRequestException("CNIC must be exactly 13 digits (e.g. 12345-1234567-1).");
        }
        if (userRepository.existsByNormalizedCnicAndRole(cnic, UserRole.DOCTOR)) {
            throw new ConflictException("This CNIC is already registered as a Doctor.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new ConflictException("An account is already registered with this email address.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.DOCTOR)
                .normalizedCnic(cnic)
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
                .status(ProviderStatus.PENDING)
                .build();
        doctor = doctorRepository.save(doctor);

        Wallet wallet = Wallet.builder()
                .providerId(doctor.getId())
                .providerType(ProviderType.DOCTOR)
                .balance(java.math.BigDecimal.ZERO)
                .build();
        walletRepository.save(wallet);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), doctor.getId());

        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .providerId(doctor.getId())
                .providerName(doctor.getName())
                .providerStatus(doctor.getStatus())
                .build();
    }
}
