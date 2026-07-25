package com.partner.backend.mobile.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyRequest {
    @NotBlank
    private String phone;
    @NotBlank
    private String otpCode;
}
