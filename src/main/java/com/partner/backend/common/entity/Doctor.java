package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Doctor extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "pmdc_number", unique = true)
    private String pmdcNumber;

    @Column(name = "specialty")
    private String specialty;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "clinic_name")
    private String clinicName;

    @Column(name = "clinic_address")
    private String clinicAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "virtual_fee", precision = 10, scale = 2)
    private BigDecimal virtualFee;

    @Column(name = "physical_fee", precision = 10, scale = 2)
    private BigDecimal physicalFee;

    @Column(name = "bio", length = 1000)
    private String bio;

    /** ONLINE | PHYSICAL | BOTH — set when doctor saves availability */
    @Column(name = "consultation_type", length = 16)
    private String consultationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ProviderStatus status = ProviderStatus.PENDING;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "deleted", nullable = false, columnDefinition = "INTEGER DEFAULT 0 NOT NULL")
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "bank_account_title")
    private String bankAccountTitle;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_iban", length = 34)
    private String bankIban;

    @Column(name = "bank_name")
    private String bankName;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Availability> availabilities = new ArrayList<>();
}
