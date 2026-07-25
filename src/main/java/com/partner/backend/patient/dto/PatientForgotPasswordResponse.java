package com.partner.backend.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Returned after a successful forgot-password lookup so the client knows which email the OTP was sent to. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientForgotPasswordResponse {
    private String email;
}
