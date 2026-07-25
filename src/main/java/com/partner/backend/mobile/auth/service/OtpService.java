package com.partner.backend.mobile.auth.service;

import com.partner.backend.common.entity.Otp;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.repository.OtpRepository;
import com.partner.backend.common.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Send a 6-digit OTP to the given email address. */
    @Transactional
    public void sendOtpEmail(String email, String recipientName) {
        // Delete any previous OTP for this email
        otpRepository.deleteByEmail(email);

        String code = generateCode();

        Otp otp = Otp.builder()
                .phone(email)   // keep phone as identifier for backwards compat
                .email(email)
                .otpCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .build();
        otpRepository.save(otp);

        scheduleSendOtpEmailAfterCommit(email, recipientName, code);
        log.info("[OTP] Committed email OTP for {}; mail queued async", email);
    }

    private void scheduleSendOtpEmailAfterCommit(String email, String recipientName, String code) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendOtpAsync(email, recipientName, code, OTP_EXPIRY_MINUTES);
                }
            });
        } else {
            emailService.sendOtpAsync(email, recipientName, code, OTP_EXPIRY_MINUTES);
        }
    }

    @Transactional
    public boolean verifyOtpEmail(String email, String code) {
        Otp otp = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("No OTP found for this email"));

        if (otp.isExpired()) {
            throw new BadRequestException("OTP has expired. Please request a new one");
        }

        if (!otp.getOtpCode().equals(code)) {
            throw new BadRequestException("Invalid OTP code");
        }

        otp.setVerified(true);
        otpRepository.save(otp);
        return true;
    }

    /** Returns true if the email's latest OTP was verified (and not yet consumed). */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .map(o -> o.isVerified() && !o.isExpired())
                .orElse(false);
    }

    /** Legacy phone-based OTP — kept for internal use only. */
    @Transactional
    public String sendOtp(String phone) {
        String code = generateCode();
        otpRepository.deleteByPhone(phone);
        Otp otp = Otp.builder()
                .phone(phone)
                .otpCode(code)
                .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                .verified(false)
                .build();
        otpRepository.save(otp);
        log.info("[OTP-PHONE] Stored OTP for {}", phone);
        return code;
    }

    @Transactional
    public boolean verifyOtp(String phone, String code) {
        Otp otp = otpRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
                .orElseThrow(() -> new BadRequestException("No OTP found for this phone number"));
        if (otp.isExpired()) {
            throw new BadRequestException("OTP has expired. Please request a new one");
        }
        if (!otp.getOtpCode().equals(code)) {
            throw new BadRequestException("Invalid OTP code");
        }
        otp.setVerified(true);
        otpRepository.save(otp);
        return true;
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
