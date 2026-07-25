package com.partner.backend.mobile.doctor.dto;

import com.partner.backend.common.entity.ProviderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorProfileResponse {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String pmdcNumber;
    private String specialty;
    private Integer experienceYears;
    private String clinicName;
    private String clinicAddress;
    private String city;
    private BigDecimal virtualFee;
    private BigDecimal physicalFee;
    private String bio;
    private String consultationType;
    private ProviderStatus status;
    private String bankAccountTitle;
    private String bankAccountNumber;
    private String bankIban;
    private String bankName;
}
