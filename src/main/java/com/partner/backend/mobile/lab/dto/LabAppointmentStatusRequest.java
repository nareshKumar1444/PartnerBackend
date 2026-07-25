package com.partner.backend.mobile.lab.dto;

import com.partner.backend.common.entity.LabAppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LabAppointmentStatusRequest {
    @NotNull
    private LabAppointmentStatus status;
}
