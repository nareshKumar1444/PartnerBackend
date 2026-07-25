package com.partner.backend.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddDoctorRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @NotBlank
    private String name;
    @NotBlank
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
    /** CNIC digits or dashed — optional; normalized on server */
    private String cnic;
}
