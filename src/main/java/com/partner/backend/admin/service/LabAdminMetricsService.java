package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.ProviderBankAccountDto;
import com.partner.backend.admin.dto.ProviderEarningsDetailDto;
import com.partner.backend.common.entity.Lab;
import com.partner.backend.common.entity.LabAppointmentStatus;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.repository.EarningRepository;
import com.partner.backend.common.repository.LabAppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LabAdminMetricsService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private final EarningRepository earningRepository;
    private final LabAppointmentRepository labAppointmentRepository;

    public ProviderEarningsDetailDto earningsForLab(Long labId) {
        BigDecimal grossEarnings = nz(earningRepository.sumGrossByProvider(labId, ProviderType.LAB));
        Double apptGross = labAppointmentRepository.sumCompletedAmountByLabId(labId);
        BigDecimal grossAppointments = apptGross != null ? BigDecimal.valueOf(apptGross) : BigDecimal.ZERO;
        BigDecimal gross = grossEarnings.max(grossAppointments);

        BigDecimal commissionEarnings = nz(earningRepository.sumCommissionByProvider(labId, ProviderType.LAB));
        BigDecimal commission = commissionEarnings.compareTo(BigDecimal.ZERO) > 0
                ? commissionEarnings
                : gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(commission).max(BigDecimal.ZERO);

        LocalDate now = LocalDate.now();
        BigDecimal thisMonth = monthGross(labId, now.getYear(), now.getMonthValue());
        LocalDate last = now.minusMonths(1);
        BigDecimal lastMonth = monthGross(labId, last.getYear(), last.getMonthValue());

        return ProviderEarningsDetailDto.builder()
                .total(gross)
                .commission(commission)
                .net(net)
                .thisMonth(thisMonth)
                .lastMonth(lastMonth)
                .build();
    }

    public long completedTestsForLab(Long labId) {
        return labAppointmentRepository.countByLabIdAndStatus(labId, LabAppointmentStatus.COMPLETED);
    }

    public ProviderBankAccountDto bankFrom(Lab lab) {
        return ProviderBankAccountDto.builder()
                .title(display(lab.getBankAccountTitle()))
                .account(display(lab.getBankAccountNumber()))
                .iban(display(lab.getBankIban()))
                .bank(display(lab.getBankName()))
                .build();
    }

    public BigDecimal monthGrossForLab(Long labId, int year, int month) {
        return monthGross(labId, year, month);
    }

    private BigDecimal monthGross(Long labId, int year, int month) {
        BigDecimal fromEarnings = nz(earningRepository.sumGrossByProviderAndMonth(
                labId, ProviderType.LAB, month, year));
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();
        BigDecimal fromAppointments = nz(labAppointmentRepository.sumCompletedAmountByLabBetween(labId, start, end));
        return fromEarnings.max(fromAppointments);
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static String display(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.trim();
    }
}
