package com.partner.backend.mobile.doctor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientBriefResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Integer age;
    private String gender;
}
