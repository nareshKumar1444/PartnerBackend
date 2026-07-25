package com.partner.backend.mobile.pharmacy.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DrapVerifyResponse {
    private boolean verified;
    private String message;
}
