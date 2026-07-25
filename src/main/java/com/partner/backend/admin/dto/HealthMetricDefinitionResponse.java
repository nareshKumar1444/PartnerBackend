package com.partner.backend.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HealthMetricDefinitionResponse {
    private Long id;
    private String metricKey;
    private String label;
    private String unit;
    private String icon;
    private String color;
    private String bgColor;
    private String normalRange;
    private String description;
    private String detailDescription;
    private Double normalLow;
    private Double normalHigh;
    private Integer sortOrder;
    private Boolean active;
    private Boolean builtin;
}
