package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.lab.dto.LabDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabDashboardService {

    private final LabTestRepository labTestRepository;
    private final LabAppointmentRepository labAppointmentRepository;
    private final WalletRepository walletRepository;

    public LabDashboardResponse getDashboard(Long labId) {
        long totalTests = labTestRepository.countByLabId(labId);
        long pending = labAppointmentRepository.countByLabIdAndStatus(labId, LabAppointmentStatus.PENDING);
        long confirmed = labAppointmentRepository.countByLabIdAndStatus(labId, LabAppointmentStatus.CONFIRMED);
        long completed = labAppointmentRepository.countByLabIdAndStatus(labId, LabAppointmentStatus.COMPLETED);

        Double rawRevenue = labAppointmentRepository.sumCompletedAmountByLabId(labId);
        BigDecimal revenue = rawRevenue != null ? BigDecimal.valueOf(rawRevenue) : BigDecimal.ZERO;

        BigDecimal walletBalance = walletRepository
                .findByProviderIdAndProviderType(labId, ProviderType.LAB)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        return LabDashboardResponse.builder()
                .totalTests(totalTests)
                .pendingAppointments(pending)
                .confirmedAppointments(confirmed)
                .completedAppointments(completed)
                .totalRevenue(revenue)
                .walletBalance(walletBalance)
                .build();
    }
}
