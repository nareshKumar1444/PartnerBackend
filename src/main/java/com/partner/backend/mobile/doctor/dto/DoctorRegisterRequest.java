package com.partner.backend.mobile.doctor.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorRegisterRequest {

    /** Step 1 — Personal + PMDC */
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @NotBlank
    private String name;
    @NotBlank
    private String phone;
    /** Pakistan CNIC — 13 digits (with or without dashes); normalized server-side */
    @NotBlank(message = "CNIC is required")
    private String cnic;

    private String pmdcNumber;

    /** Step 2 — Clinic & Fees */
    private String specialty;
    private Integer experienceYears;
    private String clinicName;
    private String clinicAddress;
    private String city;
    private BigDecimal virtualFee;
    private BigDecimal physicalFee;
    private String bio;

    /** Registration step — optional (frontend-only progress indicator) */
    private Integer step;
}
