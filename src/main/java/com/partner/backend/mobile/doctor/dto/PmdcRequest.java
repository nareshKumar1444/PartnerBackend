package com.partner.backend.mobile.doctor.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PmdcRequest {
    private String RegistrationNo;
    private String Name;
    private String FatherName;
}
