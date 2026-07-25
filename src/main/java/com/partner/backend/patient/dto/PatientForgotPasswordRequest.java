package com.partner.backend.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Step 1 of patient forgot-password: identify the account by its registered mobile number. */
@Data
public class PatientForgotPasswordRequest {
    @NotBlank(message = "Mobile number is required")
    private String phone;
}
