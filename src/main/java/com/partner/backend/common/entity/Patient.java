package com.partner.backend.common.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient extends BaseEntity {

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Column(name = "age")
    private Integer age;

    @Column(name = "gender")
    private String gender;

    /**
     * App user account; null for legacy patient rows created only via provider flows.
     * {@link ManyToOne} (not {@link OneToOne}): Hibernate still emits {@code ALTER TABLE ... ADD ... UNIQUE} for
     * one-to-one FK on SQLite, which fails; many-to-one never adds that column-level unique.
     */
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "city")
    private String city;

    @Column(name = "blood_group")
    private String bloodGroup;

    @Column(name = "health_conditions", columnDefinition = "TEXT")
    private String healthConditions;
}
