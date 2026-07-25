package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "patient_health_readings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientHealthReading extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "metric_key", nullable = false, length = 64)
    private String metricKey;

    @Column(name = "value_text", nullable = false, length = 128)
    private String valueText;

    @Column(name = "unit", nullable = false, length = 64)
    private String unit;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "note", length = 2000)
    private String note;
}
