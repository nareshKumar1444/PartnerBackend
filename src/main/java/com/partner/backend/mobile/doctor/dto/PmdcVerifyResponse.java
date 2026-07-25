package com.partner.backend.mobile.doctor.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PmdcVerifyResponse {
    private boolean verified;
    private String message;
}
