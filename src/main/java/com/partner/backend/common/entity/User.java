package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_users_normalized_cnic_role",
                columnNames = {"normalized_cnic", "role"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    /**
     * 13-digit CNIC (nullable). Same person may use the same CNIC once per {@link UserRole} (DOCTOR, LAB, PHARMACY, PATIENT).
     * Composite unique (normalized_cnic, role) — not globally unique across roles.
     */
    @Column(name = "normalized_cnic", length = 13)
    private String normalizedCnic;

    @Column(name = "is_active")
    @Builder.Default
    private boolean active = true;
}
