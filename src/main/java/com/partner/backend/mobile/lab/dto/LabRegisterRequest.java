package com.partner.backend.mobile.lab.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class LabRegisterRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @NotBlank
    private String name;
    private String ownerName;
    @NotBlank
    private String phone;
    @NotBlank(message = "CNIC is required")
    private String cnic;
    private String address;
    private String city;
    @NotNull(message = "Please select a location on the map")
    private Double latitude;
    @NotNull(message = "Please select a location on the map")
    private Double longitude;
    private String dnlcLicense;
    private Integer step;
}
