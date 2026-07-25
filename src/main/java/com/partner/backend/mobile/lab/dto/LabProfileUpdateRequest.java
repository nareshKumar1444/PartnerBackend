package com.partner.backend.mobile.lab.dto;

import lombok.Data;

@Data
public class LabProfileUpdateRequest {
    private String name;
    private String ownerName;
    private String email;
    private String phone;
    private String address;
    private String city;
    private String bankAccountTitle;
    private String bankAccountNumber;
    private String bankIban;
    private String bankName;
}
