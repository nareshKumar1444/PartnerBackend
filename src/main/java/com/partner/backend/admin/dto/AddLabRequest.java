package com.partner.backend.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddLabRequest {
    @NotBlank @Email
    private String email;
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    @NotBlank
    private String name;
    private String ownerName;
    @NotBlank
    private String phone;
    private String address;
    private String city;
    private Double latitude;
    private Double longitude;
    private String dnlcLicense;
    private String cnic;
}
