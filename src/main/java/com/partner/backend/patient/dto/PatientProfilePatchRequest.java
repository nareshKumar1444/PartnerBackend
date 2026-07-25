package com.partner.backend.patient.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PatientProfilePatchRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @Min(1)
    @Max(130)
    private Integer age;

    @NotBlank
    @Size(max = 120)
    private String city;

    @NotBlank
    @Size(max = 32)
    private String bloodGroup;

    /** May be empty; stored as CSV on {@code Patient.healthConditions} */
    private List<String> conditions;
}
