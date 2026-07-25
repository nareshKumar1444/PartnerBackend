package com.partner.backend.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientQrShareResponse {
    private Long id;
    private Long patientId;
    private String accessCode;
    private String payloadJson;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
