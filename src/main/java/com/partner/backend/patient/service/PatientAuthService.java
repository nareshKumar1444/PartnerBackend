package com.partner.backend.patient.service;

import com.partner.backend.common.entity.Patient;
import com.partner.backend.common.entity.User;
import com.partner.backend.common.entity.UserRole;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ConflictException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.OtpRepository;
import com.partner.backend.common.repository.PatientRepository;
import com.partner.backend.common.repository.UserRepository;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.push.PushExternalUserId;
import com.partner.backend.common.security.JwtUtil;
import com.partner.backend.common.util.CnicNormalizer;
import com.partner.backend.mobile.auth.dto.AuthResponse;
import com.partner.backend.mobile.auth.service.OtpService;
import com.partner.backend.patient.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientAuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final OtpRepository otpRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private static boolean isPakMobile(String phone) {
        return phone != null && phone.matches("^03\\d{9}$");
    }

    @Transactional
    public PatientSessionResponse register(PatientRegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (!otpService.isEmailVerified(email)) {
            throw new BadRequestException("Please verify the OTP sent to your email before completing registration.");
        }

        String cnic = CnicNormalizer.normalize(req.getCnic());
        if (!CnicNormalizer.isValid13(cnic)) {
            throw new BadRequestException("CNIC must be exactly 13 digits.");
        }

        String phone = normalizePhone(req.getPhone());
        if (!isPakMobile(phone)) {
            throw new BadRequestException("Mobile must be a valid Pakistani number (03XXXXXXXXX).");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("An account is already registered with this email.");
        }
        if (userRepository.existsByNormalizedCnicAndRole(cnic, UserRole.PATIENT)) {
            throw new ConflictException("A patient account is already registered with this CNIC.");
        }
        if (patientRepository.existsRegisteredAppPatientByPhone(phone)) {
            throw new ConflictException("An account is already registered with this mobile number.");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.PATIENT)
                .normalizedCnic(cnic)
                .build();

        String conditionsCsv = stringifyConditions(req.getConditions());

        Patient patient = Patient.builder()
                .user(user)
                .name(req.getName().trim())
                .phone(phone)
                .email(email)
                .age(req.getAge())
                .city(req.getCity().trim())
                .bloodGroup(req.getBloodGroup().trim())
                .healthConditions(conditionsCsv.isEmpty() ? "None" : conditionsCsv)
                .build();

        patientRepository.save(patient);
        otpRepository.deleteByEmail(email);

        return buildSession(email, patient);
    }

    @Transactional(readOnly = true)
    public PatientSessionResponse login(PatientLoginRequest req) {
        String phone = normalizePhone(req.getPhone());
        Patient patient = patientRepository.findByPhoneAndUserIsNotNull(phone)
                .orElseThrow(() -> new UnauthorizedException("Invalid mobile number or password"));

        User user = patient.getUser();
        if (user.getRole() != UserRole.PATIENT || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid mobile number or password");
        }

        return buildSession(user.getEmail(), patient);
    }

    /** Step 1 of forgot-password: look up the account by its registered mobile number and email an OTP. */
    @Transactional
    public String forgotPassword(String rawPhone) {
        String phone = normalizePhone(rawPhone);
        Patient patient = patientRepository.findByPhoneAndUserIsNotNull(phone)
                .orElseThrow(() -> new BadRequestException("No account found with this mobile number."));

        User user = patient.getUser();
        if (user == null || user.getEmail() == null) {
            throw new BadRequestException("No account found with this mobile number.");
        }

        otpService.sendOtpEmail(user.getEmail(), patient.getName());
        return user.getEmail();
    }

    /** Step 3 of forgot-password: requires a verified OTP for this email (see {@link OtpService#isEmailVerified}). */
    @Transactional
    public void resetPassword(String rawEmail, String newPassword) {
        String email = rawEmail.trim().toLowerCase();
        if (!otpService.isEmailVerified(email)) {
            throw new BadRequestException("Please verify the OTP sent to your email before resetting your password.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No account found with this email."));
        if (user.getRole() != UserRole.PATIENT) {
            throw new BadRequestException("No account found with this email.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.deleteByEmail(email);
    }

    @Transactional(readOnly = true)
    public PatientProfileResponse getProfile(Long userId) {
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UnauthorizedException("Patient profile not found"));
        return toProfileResponse(patient);
    }

    @Transactional
    public PatientProfileResponse patchProfile(Long userId, @Valid PatientProfilePatchRequest req) {
        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UnauthorizedException("Patient profile not found"));
        patient.setName(req.getName().trim());
        patient.setAge(req.getAge());
        patient.setCity(req.getCity().trim());
        patient.setBloodGroup(req.getBloodGroup().trim());
        String csv = stringifyConditions(req.getConditions());
        patient.setHealthConditions(csv.isEmpty() ? "None" : csv);
        return toProfileResponse(patientRepository.save(patient));
    }

    private PatientSessionResponse buildSession(String email, Patient patient) {
        User user = patient.getUser();
        String token = jwtUtil.generateToken(email, UserRole.PATIENT.name(), patient.getId());
        redisTemplate.opsForValue().set("LOGIN_USER:" + user.getEmail(), token);
        AuthResponse auth = AuthResponse.builder()
                .token(token)
                .role(UserRole.PATIENT.name())
                .email(email)
                .providerId(patient.getId())
                .providerName(patient.getName())
                .providerStatus(null)
                .pushExternalUserId(PushExternalUserId.forProvider(ProviderType.PATIENT, patient.getId()))
                .build();

        PatientProfileResponse profile = toProfileResponse(patient);

        return PatientSessionResponse.builder()
                .token(token)
                .auth(auth)
                .profile(profile)
                .build();
    }

    public static PatientProfileResponse toProfileResponse(Patient patient) {
        User user = patient.getUser();
        return PatientProfileResponse.builder()
                .patientId(patient.getId())
                .email(user != null ? user.getEmail() : patient.getEmail())
                .normalizedCnic(user != null ? user.getNormalizedCnic() : null)
                .name(patient.getName())
                .phone(patient.getPhone())
                .age(patient.getAge())
                .city(patient.getCity())
                .bloodGroup(patient.getBloodGroup())
                .conditions(splitConditions(patient.getHealthConditions()))
                .build();
    }

    private static String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        String digits = raw.replaceAll("\\D", "");
        return digits.startsWith("0") ? digits : ("0" + digits);
    }

    private static String stringifyConditions(List<String> parts) {
        if (parts == null || parts.isEmpty()) {
            return "";
        }
        return String.join(",", parts.stream().map(String::trim).filter(s -> !s.isEmpty()).toList());
    }

    private static List<String> splitConditions(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
