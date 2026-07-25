package com.partner.backend.admin.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedProviderResponse {
    private Long id;
    /** doctor | pharmacy | lab */
    private String providerType;
    private String name;
    private String email;
    private String city;
    private ProviderStatus status;
    private LocalDateTime deletedAt;
}
