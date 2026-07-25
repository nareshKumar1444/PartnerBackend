package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.*;
import com.partner.backend.common.entity.*;
import com.partner.backend.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminEarningsService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final DoctorAdminMetricsService doctorMetrics;
    private final PharmacyAdminMetricsService pharmacyMetrics;
    private final LabAdminMetricsService labMetrics;

    public EarningsSummaryResponse getSummary() {
        List<ProviderEarningsResponse> all = buildAllProviderRows(null);
        BigDecimal gross = all.stream()
                .map(ProviderEarningsResponse::getGrossAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal commission = all.stream()
                .map(ProviderEarningsResponse::getCommissionAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = all.stream()
                .map(ProviderEarningsResponse::getNetAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EarningsSummaryResponse(gross, commission, net);
    }

    public List<MonthlyEarningsResponse> getMonthly() {
        Map<YearMonth, BigDecimal> grossByMonth = new TreeMap<>(Comparator.reverseOrder());
        LocalDate now = LocalDate.now();
        YearMonth start = YearMonth.from(now).minusMonths(11);

        for (Doctor doctor : doctorRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
            accumulateMonthly(grossByMonth, doctor.getId(), ProviderType.DOCTOR, start);
        }
        for (Pharmacy pharmacy : pharmacyRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
            accumulateMonthly(grossByMonth, pharmacy.getId(), ProviderType.PHARMACY, start);
        }
        for (Lab lab : labRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
            accumulateMonthly(grossByMonth, lab.getId(), ProviderType.LAB, start);
        }

        return grossByMonth.entrySet().stream()
                .map(e -> {
                    BigDecimal gross = e.getValue();
                    BigDecimal commission = gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
                    return MonthlyEarningsResponse.builder()
                            .month(e.getKey().getMonthValue())
                            .year(e.getKey().getYear())
                            .grossAmount(gross)
                            .commissionAmount(commission)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<ProviderEarningsResponse> getPerProvider(ProviderType type) {
        return buildAllProviderRows(type).stream()
                .sorted(Comparator.comparing(ProviderEarningsResponse::getGrossAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<ProviderEarningsResponse> buildAllProviderRows(ProviderType filter) {
        List<ProviderEarningsResponse> rows = new ArrayList<>();
        if (filter == null || filter == ProviderType.DOCTOR) {
            for (Doctor doctor : doctorRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
                rows.add(toProviderRow(doctor.getId(), doctor.getName(), doctor.getCity(), ProviderType.DOCTOR,
                        doctorMetrics.earningsForDoctor(doctor.getId())));
            }
        }
        if (filter == null || filter == ProviderType.PHARMACY) {
            for (Pharmacy pharmacy : pharmacyRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
                rows.add(toProviderRow(pharmacy.getId(), pharmacy.getName(), pharmacy.getCity(), ProviderType.PHARMACY,
                        pharmacyMetrics.earningsForPharmacy(pharmacy.getId())));
            }
        }
        if (filter == null || filter == ProviderType.LAB) {
            for (Lab lab : labRepository.findByStatusAndDeletedFalse(ProviderStatus.APPROVED)) {
                rows.add(toProviderRow(lab.getId(), lab.getName(), lab.getCity(), ProviderType.LAB,
                        labMetrics.earningsForLab(lab.getId())));
            }
        }
        return rows;
    }

    private ProviderEarningsResponse toProviderRow(
            Long id,
            String name,
            String city,
            ProviderType type,
            ProviderEarningsDetailDto earnings) {
        BigDecimal gross = nz(earnings.getTotal());
        BigDecimal commission = nz(earnings.getCommission());
        BigDecimal net = nz(earnings.getNet());
        return ProviderEarningsResponse.builder()
                .providerId(id)
                .providerName(name != null ? name : "Unknown")
                .providerType(type)
                .city(city != null && !city.isBlank() ? city.trim() : "—")
                .grossAmount(gross)
                .commissionAmount(commission)
                .netAmount(net)
                .thisMonth(nz(earnings.getThisMonth()))
                .build();
    }

    private void accumulateMonthly(
            Map<YearMonth, BigDecimal> grossByMonth,
            Long providerId,
            ProviderType type,
            YearMonth start) {
        YearMonth cursor = YearMonth.from(LocalDate.now());
        while (!cursor.isBefore(start)) {
            BigDecimal monthGross = monthGrossFor(providerId, type, cursor.getYear(), cursor.getMonthValue());
            if (monthGross.compareTo(BigDecimal.ZERO) > 0) {
                grossByMonth.merge(cursor, monthGross, BigDecimal::add);
            }
            cursor = cursor.minusMonths(1);
        }
    }

    private BigDecimal monthGrossFor(Long providerId, ProviderType type, int year, int month) {
        ProviderEarningsDetailDto current = switch (type) {
            case DOCTOR -> doctorMetrics.earningsForDoctor(providerId);
            case PHARMACY -> pharmacyMetrics.earningsForPharmacy(providerId);
            case LAB -> labMetrics.earningsForLab(providerId);
            case PATIENT -> new ProviderEarningsDetailDto();
        };
        LocalDate now = LocalDate.now();
        if (year == now.getYear() && month == now.getMonthValue()) {
            return nz(current.getThisMonth());
        }
        LocalDate last = now.minusMonths(1);
        if (year == last.getYear() && month == last.getMonthValue()) {
            return nz(current.getLastMonth());
        }
        return queryMonthGross(providerId, type, year, month);
    }

    private BigDecimal queryMonthGross(Long providerId, ProviderType type, int year, int month) {
        return switch (type) {
            case DOCTOR -> doctorMetrics.monthGrossForDoctor(providerId, year, month);
            case PHARMACY -> pharmacyMetrics.monthGrossForPharmacy(providerId, year, month);
            case LAB -> labMetrics.monthGrossForLab(providerId, year, month);
            case PATIENT -> BigDecimal.ZERO;
        };
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
