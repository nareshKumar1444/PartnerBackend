package com.partner.backend.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateHealthMetricRequest {

    @NotBlank
    @Size(max = 128)
    private String label;

    @NotBlank
    @Pattern(regexp = "[a-z][a-z0-9_]{1,62}", message = "Key must be lowercase snake_case (e.g. vitamin_d)")
    private String metricKey;

    @Size(max = 64)
    private String unit;

    @Size(max = 64)
    private String icon;

    @Size(max = 16)
    private String color;

    @Size(max = 16)
    private String bgColor;

    @Size(max = 128)
    private String normalRange;

    @Size(max = 500)
    private String description;

    @Size(max = 2000)
    private String detailDescription;

    private Double normalLow;

    private Double normalHigh;

    private Integer sortOrder;
}
