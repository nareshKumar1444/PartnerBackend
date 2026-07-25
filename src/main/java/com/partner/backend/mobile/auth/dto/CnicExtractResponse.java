package com.partner.backend.mobile.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CnicExtractResponse {

    /** 13-digit normalized CNIC, or null if not found */
    private String suggestedCnic;
    /** Hint for client UI */
    private String message;
    /** Whether OCR service is configured (API key present) */
    private boolean ocrConfigured;
}
