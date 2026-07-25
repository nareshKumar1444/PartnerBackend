package com.partner.backend.mobile.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProviderLoginRequest {
    @NotBlank @Email
    private String email;
    @NotBlank
    private String password;
}
