package com.partner.backend.common.repository;

import com.partner.backend.common.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {
    Optional<Otp> findTopByPhoneOrderByCreatedAtDesc(String phone);
    Optional<Otp> findTopByEmailOrderByCreatedAtDesc(String email);
    void deleteByPhone(String phone);
    void deleteByEmail(String email);
}
