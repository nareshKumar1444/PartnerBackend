package com.partner.backend.patient.dto;

import com.partner.backend.mobile.auth.dto.AuthResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class  PatientSessionResponse {
    private String token;
    private AuthResponse auth;
    private PatientProfileResponse profile;
}
