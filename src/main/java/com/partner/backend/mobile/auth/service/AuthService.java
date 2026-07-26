package com.partner.backend.mobile.auth.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.*;
import com.partner.backend.common.push.PushExternalUserId;
import com.partner.backend.common.security.JwtUtil;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.auth.dto.ProviderLoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final OtpRepository otpRepository;


    private final RedisTemplate<String, String> redisTemplate;
    @Transactional(readOnly = true)
    public AuthResponse login(ProviderLoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.getRole() == UserRole.ADMIN) {
            throw new UnauthorizedException("Admin accounts must use the admin login endpoint");
        }
        if (user.getRole() == UserRole.PATIENT) {
            throw new UnauthorizedException("Patient accounts must use the patient login endpoint");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!user.isActive() || isProviderDeleted(user)) {
            throw new UnauthorizedException("This account has been deactivated.");
        }

        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String name;
        Long providerId;
        ProviderStatus status;

        UserRole role = user.getRole();
        if (role == UserRole.DOCTOR) {
            Doctor d = doctorRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UnauthorizedException("Provider profile not found"));
            name = d.getName();
            providerId = d.getId();
            status = d.getStatus();
        } else if (role == UserRole.PHARMACY) {
            Pharmacy p = pharmacyRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UnauthorizedException("Provider profile not found"));
            name = p.getName();
            providerId = p.getId();
            status = p.getStatus();
        } else if (role == UserRole.LAB) {
            Lab l = labRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new UnauthorizedException("Provider profile not found"));
            name = l.getName();
            providerId = l.getId();
            status = l.getStatus();
        } else {
            throw new UnauthorizedException("Unknown role");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), providerId);
        redisTemplate.opsForValue().set("LOGIN_USER:" + user.getEmail(), token);
        ProviderType providerType = toProviderType(role);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .email(user.getEmail())
                .providerId(providerId)
                .providerName(name)
                .providerStatus(status)
                .pushExternalUserId(PushExternalUserId.forProvider(providerType, providerId))
                .build();
    }
    private static ProviderType toProviderType(UserRole role) {
        return switch (role) {
            case DOCTOR -> ProviderType.DOCTOR;
            case PHARMACY -> ProviderType.PHARMACY;
            case LAB -> ProviderType.LAB;
            case PATIENT -> ProviderType.PATIENT;
            case ADMIN -> null;
        };
    }

    /** Forgot password — step 1: confirm the email belongs to a provider account and email an OTP. */
    @Transactional
    public void forgotPassword(String rawEmail) {
        String email = rawEmail.trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email."));
        if (!isProviderRole(user.getRole())) {
            throw new BadRequestException("No account found with this email.");
        }
        if (!user.isActive() || isProviderDeleted(user)) {
            throw new BadRequestException("No account found with this email.");
        }
        otpService.sendOtpEmail(email, email.split("@")[0]);
    }

    /** Forgot password — step 3: requires a verified OTP for this email (see {@link OtpService#isEmailVerified}). */
    @Transactional
    public void resetPassword(String rawEmail, String newPassword) {
        String email = rawEmail.trim().toLowerCase();
        if (!otpService.isEmailVerified(email)) {
            throw new BadRequestException("Please verify the OTP sent to your email before resetting your password.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email."));
        if (!isProviderRole(user.getRole())) {
            throw new BadRequestException("No account found with this email.");
        }
        if (!user.isActive() || isProviderDeleted(user)) {
            throw new BadRequestException("No account found with this email.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.deleteByEmail(email);
    }

    private static boolean isProviderRole(UserRole role) {
        return role == UserRole.DOCTOR || role == UserRole.PHARMACY || role == UserRole.LAB;
    }

    private boolean isProviderDeleted(User user) {
        return switch (user.getRole()) {
            case DOCTOR -> doctorRepository.findByUserId(user.getId())
                    .map(Doctor::isDeleted)
                    .orElse(false);
            case PHARMACY -> pharmacyRepository.findByUserId(user.getId())
                    .map(Pharmacy::isDeleted)
                    .orElse(false);
            case LAB -> labRepository.findByUserId(user.getId())
                    .map(Lab::isDeleted)
                    .orElse(false);
            default -> false;
        };
    }

    public void logout(String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return; // don't crash
            }

            String token = authHeader.substring(7);

            String email = jwtUtil.extractEmail(token);

            if (email != null) {
                redisTemplate.delete("LOGIN_USER:" + email);
            }

        } catch (Exception e) {
            // IMPORTANT: never break logout
            System.out.println("Logout error: " + e.getMessage());
        }
    }
}
