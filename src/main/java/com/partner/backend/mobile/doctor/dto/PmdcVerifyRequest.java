package com.partner.backend.mobile.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PmdcVerifyRequest {
    @NotBlank(message = "PMDC number is required")
    private String pmdcNumber;
}
