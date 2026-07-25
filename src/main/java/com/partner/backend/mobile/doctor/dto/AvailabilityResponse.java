package com.partner.backend.mobile.doctor.dto;

import com.partner.backend.common.entity.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    private Long id;
    private WeekDay dayOfWeek;
    private String startTime;
    private String endTime;
    private boolean available;
    private Integer maxPatients;
}
