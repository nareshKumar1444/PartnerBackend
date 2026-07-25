package com.partner.backend.mobile.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CnicAvailabilityResponse {
    private String normalizedCnic;
    private boolean available;
    private String message;
}
