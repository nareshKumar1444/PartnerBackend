package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.ProviderBankAccountDto;
import com.partner.backend.admin.dto.ProviderEarningsDetailDto;
import com.partner.backend.common.entity.Pharmacy;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.repository.EarningRepository;
import com.partner.backend.common.repository.OrderRepository;
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
public class PharmacyAdminMetricsService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private final EarningRepository earningRepository;
    private final OrderRepository orderRepository;

    public ProviderEarningsDetailDto earningsForPharmacy(Long pharmacyId) {
        BigDecimal grossEarnings = nz(earningRepository.sumGrossByProvider(pharmacyId, ProviderType.PHARMACY));
        Double orderGross = orderRepository.sumCompletedAmountByPharmacyId(pharmacyId);
        BigDecimal grossOrders = orderGross != null ? BigDecimal.valueOf(orderGross) : BigDecimal.ZERO;
        BigDecimal gross = grossEarnings.max(grossOrders);

        BigDecimal commissionEarnings = nz(earningRepository.sumCommissionByProvider(pharmacyId, ProviderType.PHARMACY));
        BigDecimal commission = commissionEarnings.compareTo(BigDecimal.ZERO) > 0
                ? commissionEarnings
                : gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(commission).max(BigDecimal.ZERO);

        LocalDate now = LocalDate.now();
        BigDecimal thisMonth = monthGross(pharmacyId, now.getYear(), now.getMonthValue());
        LocalDate last = now.minusMonths(1);
        BigDecimal lastMonth = monthGross(pharmacyId, last.getYear(), last.getMonthValue());

        return ProviderEarningsDetailDto.builder()
                .total(gross)
                .commission(commission)
                .net(net)
                .thisMonth(thisMonth)
                .lastMonth(lastMonth)
                .build();
    }

    public long orderCountForPharmacy(Long pharmacyId) {
        return orderRepository.countByPharmacyId(pharmacyId);
    }

    public ProviderBankAccountDto bankFrom(Pharmacy pharmacy) {
        return ProviderBankAccountDto.builder()
                .title(display(pharmacy.getBankAccountTitle()))
                .account(display(pharmacy.getBankAccountNumber()))
                .iban(display(pharmacy.getBankIban()))
                .bank(display(pharmacy.getBankName()))
                .build();
    }

    public BigDecimal monthGrossForPharmacy(Long pharmacyId, int year, int month) {
        return monthGross(pharmacyId, year, month);
    }

    private BigDecimal monthGross(Long pharmacyId, int year, int month) {
        BigDecimal fromEarnings = nz(earningRepository.sumGrossByProviderAndMonth(
                pharmacyId, ProviderType.PHARMACY, month, year));
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();
        BigDecimal fromOrders = nz(orderRepository.sumCompletedAmountByPharmacyBetween(pharmacyId, start, end));
        return fromEarnings.max(fromOrders);
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
