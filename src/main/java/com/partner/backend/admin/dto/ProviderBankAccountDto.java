package com.partner.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderBankAccountDto {
    private String title;
    private String account;
    private String iban;
    private String bank;
}
