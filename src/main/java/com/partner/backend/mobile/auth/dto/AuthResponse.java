package com.partner.backend.mobile.auth.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String role;
    private String email;
    private Long providerId;
    private String providerName;
    private ProviderStatus providerStatus;
}
