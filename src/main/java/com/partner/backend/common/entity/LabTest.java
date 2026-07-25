package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @Column(name = "test_name", nullable = false)
    private String testName;

    @Column(name = "normal_price", precision = 10, scale = 2)
    private BigDecimal normalPrice;

    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    @Column(name = "report_time_hours")
    private Integer reportTimeHours;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "category")
    private String category;
}
