package com.partner.backend.mobile.doctor.dto;

import com.partner.backend.common.entity.WeekDay;
import lombok.Data;

import java.util.List;

@Data
public class AvailabilityRequest {
    /** ONLINE | PHYSICAL | BOTH */
    private String consultationType;

    private List<SlotRequest> slots;

    @Data
    public static class SlotRequest {
        private WeekDay dayOfWeek;
        private String startTime;
        private String endTime;
        private boolean available = true;
        private Integer maxPatients;
    }
}
