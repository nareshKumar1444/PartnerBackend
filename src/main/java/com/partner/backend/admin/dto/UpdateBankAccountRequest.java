package com.partner.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateBankAccountRequest {
    @NotBlank(message = "Account title is required")
    private String title;

    @NotBlank(message = "Account number is required")
    private String account;

    private String iban;

    @NotBlank(message = "Bank name is required")
    private String bank;
}
