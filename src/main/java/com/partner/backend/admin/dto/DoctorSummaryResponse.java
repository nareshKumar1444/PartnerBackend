package com.partner.backend.admin.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSummaryResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String specialty;
    private String city;
    private String pmdcNumber;
    private Integer experienceYears;
    private BigDecimal virtualFee;
    private BigDecimal physicalFee;
    private ProviderStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    /** Extra profile fields shown in detail */
    private String clinicName;
    private String clinicAddress;
    private String bio;
    /** Display form e.g. 41507-0395662-3 when owner's user has CNIC */
    private String normalizedCnic;
    private Long totalPatients;
    private ProviderEarningsDetailDto earnings;
    private ProviderBankAccountDto bankAccount;
}
