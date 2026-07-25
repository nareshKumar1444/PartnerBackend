package com.partner.backend.mobile.lab.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabProfileResponse {
    private Long id;
    private String name;
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String dnlcLicense;
    private ProviderStatus status;
    private String bankAccountTitle;
    private String bankAccountNumber;
    private String bankIban;
    private String bankName;
}
