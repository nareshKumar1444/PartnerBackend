package com.partner.backend.mobile.lab.service;

import com.partner.backend.common.entity.*;
import com.partner.backend.common.exception.*;
import com.partner.backend.common.repository.*;
import com.partner.backend.mobile.lab.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabRepository labRepository;

    @Transactional(readOnly = true)
    public Page<LabTestResponse> list(Long labId, String query, Pageable pageable) {
        Page<LabTest> page = (query != null && !query.isBlank())
                ? labTestRepository.searchByLabId(labId, query, pageable)
                : labTestRepository.findByLabId(labId, pageable);
        return page.map(this::toResponse);
    }

    @Transactional
    public LabTestResponse add(Long labId, LabTestRequest req) {
        Lab lab = labRepository.findById(labId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab", labId));

        LabTest test = LabTest.builder()
                .lab(lab)
                .testName(req.getTestName())
                .normalPrice(req.getNormalPrice())
                .discountedPrice(req.getDiscountedPrice())
                .reportTimeHours(req.getReportTimeHours())
                .description(req.getDescription())
                .category(req.getCategory())
                .build();
        return toResponse(labTestRepository.save(test));
    }

    @Transactional
    public LabTestResponse update(Long labId, Long testId, LabTestRequest req) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test", testId));

        if (!test.getLab().getId().equals(labId)) {
            throw new UnauthorizedException("Access denied");
        }

        test.setTestName(req.getTestName());
        if (req.getNormalPrice() != null) test.setNormalPrice(req.getNormalPrice());
        if (req.getDiscountedPrice() != null) test.setDiscountedPrice(req.getDiscountedPrice());
        if (req.getReportTimeHours() != null) test.setReportTimeHours(req.getReportTimeHours());
        if (req.getDescription() != null) test.setDescription(req.getDescription());
        if (req.getCategory() != null) test.setCategory(req.getCategory());

        return toResponse(labTestRepository.save(test));
    }

    @Transactional
    public void delete(Long labId, Long testId) {
        LabTest test = labTestRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Lab test", testId));

        if (!test.getLab().getId().equals(labId)) {
            throw new UnauthorizedException("Access denied");
        }
        labTestRepository.delete(test);
    }

    private LabTestResponse toResponse(LabTest t) {
        return LabTestResponse.builder()
                .id(t.getId())
                .testName(t.getTestName())
                .normalPrice(t.getNormalPrice())
                .discountedPrice(t.getDiscountedPrice())
                .reportTimeHours(t.getReportTimeHours())
                .description(t.getDescription())
                .category(t.getCategory())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
