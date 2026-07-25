package com.partner.backend.patient.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PatientLoginRequest {
    /** 11-digit mobile */
    @NotBlank
    private String phone;

    @NotBlank
    private String password;
}
