package com.partner.backend.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Integer age;
    private String gender;
    private String city;
    private LocalDateTime createdAt;
    /** Display form when patient app registration linked a user CNIC */
    private String normalizedCnic;
    /** Total lab test bookings/reports for this patient. */
    private Long recordsCount;
    /** Total prescriptions (consultations) written for this patient. */
    private Long prescriptionsCount;
}
