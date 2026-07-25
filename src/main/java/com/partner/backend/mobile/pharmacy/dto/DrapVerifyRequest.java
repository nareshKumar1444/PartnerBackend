package com.partner.backend.mobile.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DrapVerifyRequest {
    @NotBlank(message = "DRAP license number is required")
    private String drapLicenseNumber;
}
