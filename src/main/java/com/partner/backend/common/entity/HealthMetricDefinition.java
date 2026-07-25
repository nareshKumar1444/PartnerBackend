package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "health_metric_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthMetricDefinition extends BaseEntity {

    @Column(name = "metric_key", nullable = false, unique = true, length = 64)
    private String metricKey;

    @Column(name = "label", nullable = false, length = 128)
    private String label;

    @Column(name = "unit", length = 64)
    private String unit;

    @Column(name = "icon", length = 64)
    private String icon;

    @Column(name = "color", length = 16)
    private String color;

    @Column(name = "bg_color", length = 16)
    private String bgColor;

    @Column(name = "normal_range", length = 128)
    private String normalRange;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "detail_description", length = 2000)
    private String detailDescription;

    @Column(name = "normal_low")
    private Double normalLow;

    @Column(name = "normal_high")
    private Double normalHigh;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "builtin", nullable = false)
    @Builder.Default
    private Boolean builtin = false;
}
