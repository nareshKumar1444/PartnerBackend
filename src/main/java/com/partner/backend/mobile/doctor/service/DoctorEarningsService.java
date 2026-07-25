package com.partner.backend.mobile.doctor.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.doctor.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorEarningsService {

    private final EarningRepository earningRepository;
    private final PharmaRewardRepository pharmaRewardRepository;
    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public EarningsResponse getEarnings(Long doctorId) {
        List<Earning> records = earningRepository.findByProviderIdAndProviderType(doctorId, ProviderType.DOCTOR);

        BigDecimal totalGross = records.stream()
                .map(Earning::getGrossAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = records.stream()
                .map(Earning::getCommissionAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = records.stream()
                .map(Earning::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<EarningsResponse.MonthlyEntry> monthly = records.stream()
                .map(e -> EarningsResponse.MonthlyEntry.builder()
                        .month(e.getMonth())
                        .year(e.getYear())
                        .gross(e.getGrossAmount())
                        .net(e.getNetAmount())
                        .build())
                .collect(Collectors.toList());

        return EarningsResponse.builder()
                .totalGross(totalGross)
                .totalCommission(totalCommission)
                .totalNet(totalNet)
                .monthly(monthly)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<PharmaRewardResponse> getPharmaRewards(Long doctorId, Pageable pageable) {
        return pharmaRewardRepository.findByDoctorIdOrderByCreatedAtDesc(doctorId, pageable)
                .map(r -> PharmaRewardResponse.builder()
                        .id(r.getId())
                        .pharmacyName(r.getPharmacy().getName())
                        .amount(r.getAmount())
                        .month(r.getMonth())
                        .year(r.getYear())
                        .description(r.getDescription())
                        .createdAt(r.getCreatedAt())
                        .build());
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long doctorId, Pageable pageable) {
        return notificationRepository
                .findByProviderIdAndProviderTypeOrderByCreatedAtDesc(doctorId, ProviderType.DOCTOR, pageable)
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    @Transactional
    public void markNotificationRead(Long doctorId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!doctorId.equals(n.getProviderId()) || n.getProviderType() != ProviderType.DOCTOR) {
            throw new UnauthorizedException("Access denied");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }
}
