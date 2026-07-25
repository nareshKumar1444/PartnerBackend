package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.ProviderBankAccountDto;
import com.partner.backend.admin.dto.ProviderEarningsDetailDto;
import com.partner.backend.common.entity.Doctor;
import com.partner.backend.common.entity.ProviderType;
import com.partner.backend.common.repository.AppointmentRepository;
import com.partner.backend.common.repository.EarningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorAdminMetricsService {

    private static final BigDecimal COMMISSION_RATE = new BigDecimal("0.05");

    private final EarningRepository earningRepository;
    private final AppointmentRepository appointmentRepository;

    public ProviderEarningsDetailDto earningsForDoctor(Long doctorId) {
        BigDecimal grossEarnings = nz(earningRepository.sumGrossByProvider(doctorId, ProviderType.DOCTOR));
        Double apptGross = appointmentRepository.sumFeeByDoctorId(doctorId);
        BigDecimal grossAppointments = apptGross != null ? BigDecimal.valueOf(apptGross) : BigDecimal.ZERO;
        BigDecimal gross = grossEarnings.max(grossAppointments);

        BigDecimal commissionEarnings = nz(earningRepository.sumCommissionByProvider(doctorId, ProviderType.DOCTOR));
        BigDecimal commission = commissionEarnings.compareTo(BigDecimal.ZERO) > 0
                ? commissionEarnings
                : gross.multiply(COMMISSION_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal net = gross.subtract(commission).max(BigDecimal.ZERO);

        LocalDate now = LocalDate.now();
        BigDecimal thisMonth = monthGross(doctorId, now.getYear(), now.getMonthValue());
        LocalDate last = now.minusMonths(1);
        BigDecimal lastMonth = monthGross(doctorId, last.getYear(), last.getMonthValue());

        return ProviderEarningsDetailDto.builder()
                .total(gross)
                .commission(commission)
                .net(net)
                .thisMonth(thisMonth)
                .lastMonth(lastMonth)
                .build();
    }

    public long patientCountForDoctor(Long doctorId) {
        return appointmentRepository.countDistinctPatientsByDoctorId(doctorId);
    }

    public ProviderBankAccountDto bankFrom(Doctor doctor) {
        return ProviderBankAccountDto.builder()
                .title(display(doctor.getBankAccountTitle()))
                .account(display(doctor.getBankAccountNumber()))
                .iban(display(doctor.getBankIban()))
                .bank(display(doctor.getBankName()))
                .build();
    }

    public BigDecimal monthGrossForDoctor(Long doctorId, int year, int month) {
        return monthGross(doctorId, year, month);
    }

    private BigDecimal monthGross(Long doctorId, int year, int month) {
        BigDecimal fromEarnings = nz(earningRepository.sumGrossByProviderAndMonth(
                doctorId, ProviderType.DOCTOR, month, year));
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1);
        BigDecimal fromAppointments = nz(appointmentRepository.sumCompletedFeesByDoctorBetween(
                doctorId, start, end));
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
