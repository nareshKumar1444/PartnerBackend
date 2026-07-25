package com.partner.backend.mobile.doctor.dto;

import com.partner.backend.common.entity.AppointmentStatus;
import com.partner.backend.common.entity.AppointmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long id;
    private String patientName;
    private String patientPhone;
    /** From linked Patient when present */
    private Integer patientAge;
    private String patientGender;
    private LocalDate date;
    private String timeSlot;
    private AppointmentType type;
    private AppointmentStatus status;
    private BigDecimal fee;
    private LocalDateTime createdAt;
}
