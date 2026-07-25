package com.partner.backend.mobile.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabWalletResponse {
    private Long id;
    private BigDecimal balance;
    private BigDecimal totalEarnings;
}
