package com.partner.backend.admin.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PharmacySummaryResponse {
    private Long id;
    private String name;
    private String ownerName;
    private String email;
    private String phone;
    private String city;
    private String address;
    private String drapLicense;
    private ProviderStatus status;
    private String rejectionReason;
    private LocalDateTime createdAt;
    private String normalizedCnic;
    private Long totalOrders;
    private ProviderEarningsDetailDto earnings;
    private ProviderBankAccountDto bankAccount;
}
