package com.partner.backend.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientQrShareRequest {
    @NotBlank
    private String accessCode;

    @NotNull
    private JsonNode payload;

    /** Optional expiry in minutes. Defaults to 5 if null or ≤ 0. Max 10080 (7 days). */
    private Integer expiresMinutes;
}
