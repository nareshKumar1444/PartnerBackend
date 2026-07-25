package com.partner.backend.admin.dto;

import com.partner.backend.common.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderEarningsResponse {
    private Long providerId;
    private String providerName;
    private ProviderType providerType;
    private String city;
    private BigDecimal grossAmount;
    private BigDecimal commissionAmount;
    private BigDecimal netAmount;
    private BigDecimal thisMonth;
}
