package com.partner.backend.patient.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PatientRegisterRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String name;

    /** 11-digit mobile e.g. 03XXXXXXXXX */
    @NotBlank
    private String phone;

    /** CNIC digits or formatted */
    @NotBlank
    private String cnic;

    @NotNull
    private Integer age;

    @NotBlank
    private String city;

    @NotBlank
    private String bloodGroup;

    private List<String> conditions = new ArrayList<>();
}
