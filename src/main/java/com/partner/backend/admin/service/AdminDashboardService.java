package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.DashboardStatsResponse;
import com.partner.backend.common.entity.ProviderStatus;
import com.partner.backend.common.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PharmacyRepository pharmacyRepository;
    private final LabRepository labRepository;
    private final AppointmentRepository appointmentRepository;
    private final AdminEarningsService earningsService;

    public DashboardStatsResponse getStats() {
        var summary = earningsService.getSummary();
        BigDecimal gross = summary.getTotalGross() != null ? summary.getTotalGross() : BigDecimal.ZERO;
        BigDecimal commission = summary.getTotalCommission() != null ? summary.getTotalCommission() : BigDecimal.ZERO;

        return DashboardStatsResponse.builder()
                .totalPatients(patientRepository.count())
                .approvedDoctors(doctorRepository.countByStatusAndDeletedFalse(ProviderStatus.APPROVED))
                .pendingDoctors(doctorRepository.countByStatusAndDeletedFalse(ProviderStatus.PENDING))
                .approvedPharmacies(pharmacyRepository.countByStatusAndDeletedFalse(ProviderStatus.APPROVED))
                .pendingPharmacies(pharmacyRepository.countByStatusAndDeletedFalse(ProviderStatus.PENDING))
                .approvedLabs(labRepository.countByStatusAndDeletedFalse(ProviderStatus.APPROVED))
                .pendingLabs(labRepository.countByStatusAndDeletedFalse(ProviderStatus.PENDING))
                .grossRevenue(gross)
                .platformCommission(commission)
                .activeConsultations(appointmentRepository.countActiveConsultations())
                .build();
    }
}
