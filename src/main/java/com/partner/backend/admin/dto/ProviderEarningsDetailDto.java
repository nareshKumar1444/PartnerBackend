package com.partner.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderEarningsDetailDto {
    private BigDecimal total;
    private BigDecimal commission;
    private BigDecimal net;
    private BigDecimal thisMonth;
    private BigDecimal lastMonth;
}
