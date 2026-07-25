package com.partner.backend.mobile.pharmacy.service;

import com.partner.backend.common.entity.Earning;
import com.partner.backend.common.entity.Notification;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.exception.UnauthorizedException;
import com.partner.backend.common.repository.EarningRepository;
import com.partner.backend.common.repository.NotificationRepository;
import com.partner.backend.mobile.doctor.dto.EarningsResponse;
import com.partner.backend.mobile.doctor.dto.NotificationResponse;
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
@Transactional(readOnly = true)
public class PharmacyEarningsService {

    private final EarningRepository earningRepository;
    private final NotificationRepository notificationRepository;

    public EarningsResponse getEarnings(Long pharmacyId) {
        List<Earning> records = earningRepository.findByProviderIdAndProviderType(pharmacyId, ProviderType.PHARMACY);

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

    public Page<NotificationResponse> getNotifications(Long pharmacyId, Pageable pageable) {
        return notificationRepository
                .findByProviderIdAndProviderTypeOrderByCreatedAtDesc(pharmacyId, ProviderType.PHARMACY, pageable)
                .map(n -> NotificationResponse.builder()
                        .id(n.getId())
                        .title(n.getTitle())
                        .message(n.getMessage())
                        .read(n.isRead())
                        .createdAt(n.getCreatedAt())
                        .build());
    }

    @Transactional
    public void markNotificationRead(Long pharmacyId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));
        if (!pharmacyId.equals(n.getProviderId()) || n.getProviderType() != ProviderType.PHARMACY) {
            throw new UnauthorizedException("Access denied");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }
}
