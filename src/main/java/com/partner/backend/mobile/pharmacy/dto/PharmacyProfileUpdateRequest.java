package com.partner.backend.mobile.pharmacy.dto;

import lombok.Data;

@Data
public class PharmacyProfileUpdateRequest {
    private String name;
    private String ownerName;
    private String phone;
    private String address;
    private String city;
    private String bankAccountTitle;
    private String bankAccountNumber;
    private String bankIban;
    private String bankName;
}
