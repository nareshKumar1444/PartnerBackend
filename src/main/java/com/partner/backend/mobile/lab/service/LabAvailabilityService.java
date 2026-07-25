package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.Lab;
import com.partner.backend.common.entity.LabAvailability;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.LabAvailabilityRepository;
import com.partner.backend.common.repository.LabRepository;
import com.partner.backend.mobile.doctor.dto.AvailabilityRequest;
import com.partner.backend.mobile.doctor.dto.AvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabAvailabilityService {

    private final LabRepository labRepository;
    private final LabAvailabilityRepository labAvailabilityRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityResponse> getAvailability(Long labId) {
        return labAvailabilityRepository.findByLabId(labId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<AvailabilityResponse> updateAvailability(Long labId, AvailabilityRequest req) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));

        labAvailabilityRepository.deleteByLabId(labId);

        if (req.getSlots() != null && !req.getSlots().isEmpty()) {
            List<LabAvailability> slots = req.getSlots().stream()
                    .map(s -> LabAvailability.builder()
                            .lab(lab)
                            .dayOfWeek(s.getDayOfWeek())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .available(s.isAvailable())
                            .build())
                    .collect(Collectors.toList());
            labAvailabilityRepository.saveAll(slots);
        }

        return getAvailability(labId);
    }

    private AvailabilityResponse toResponse(LabAvailability a) {
        return AvailabilityResponse.builder()
                .id(a.getId())
                .dayOfWeek(a.getDayOfWeek())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .available(a.isAvailable())
                .build();
    }
}
