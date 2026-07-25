package com.partner.backend.mobile.doctor.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorProfileUpdateRequest {
    private String name;
    private String phone;
    private String specialty;
    private Integer experienceYears;
    private String clinicName;
    private String clinicAddress;
    private String city;
    private BigDecimal virtualFee;
    private BigDecimal physicalFee;
    private String bio;
    private String bankAccountTitle;
    private String bankAccountNumber;
    private String bankIban;
    private String bankName;
}
