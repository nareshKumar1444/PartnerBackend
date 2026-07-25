package com.partner.backend.admin.service;

import com.partner.backend.admin.dto.CreateHealthMetricRequest;
import com.partner.backend.admin.dto.HealthMetricDefinitionResponse;
import com.partner.backend.common.entity.HealthMetricDefinition;
import com.partner.backend.common.exception.BadRequestException;
import com.partner.backend.common.exception.ConflictException;
import com.partner.backend.common.exception.ResourceNotFoundException;
import com.partner.backend.common.repository.HealthMetricDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHealthMetricService {

    private final HealthMetricDefinitionRepository repository;

    @Transactional(readOnly = true)
    public List<HealthMetricDefinitionResponse> listAll() {
        return repository.findAllByOrderBySortOrderAscLabelAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public HealthMetricDefinitionResponse create(CreateHealthMetricRequest req) {
        String key = req.getMetricKey().trim().toLowerCase();
        if (repository.existsByMetricKey(key)) {
            throw new ConflictException("Metric key already exists: " + key);
        }
        int sortOrder = req.getSortOrder() != null
                ? req.getSortOrder()
                : (int) repository.count() + 1;

        HealthMetricDefinition saved = repository.save(HealthMetricDefinition.builder()
                .metricKey(key)
                .label(req.getLabel().trim())
                .unit(trimOrNull(req.getUnit()))
                .icon(trimOrDefault(req.getIcon(), "pulse"))
                .color(trimOrDefault(req.getColor(), "#00BFA5"))
                .bgColor(trimOrDefault(req.getBgColor(), "#E0F7FA"))
                .normalRange(trimOrNull(req.getNormalRange()))
                .description(trimOrNull(req.getDescription()))
                .detailDescription(trimOrNull(req.getDetailDescription()))
                .normalLow(req.getNormalLow())
                .normalHigh(req.getNormalHigh())
                .sortOrder(sortOrder)
                .active(true)
                .builtin(false)
                .build());
        return toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        HealthMetricDefinition def = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("HealthMetric", id));
        if (Boolean.TRUE.equals(def.getBuiltin())) {
            throw new BadRequestException("Built-in metrics cannot be deleted.");
        }
        repository.delete(def);
    }

    private HealthMetricDefinitionResponse toResponse(HealthMetricDefinition d) {
        return HealthMetricDefinitionResponse.builder()
                .id(d.getId())
                .metricKey(d.getMetricKey())
                .label(d.getLabel())
                .unit(d.getUnit())
                .icon(d.getIcon())
                .color(d.getColor())
                .bgColor(d.getBgColor())
                .normalRange(d.getNormalRange())
                .description(d.getDescription())
                .detailDescription(d.getDetailDescription())
                .normalLow(d.getNormalLow())
                .normalHigh(d.getNormalHigh())
                .sortOrder(d.getSortOrder())
                .active(d.getActive())
                .builtin(d.getBuiltin())
                .build();
    }

    private static String trimOrNull(String v) {
        if (v == null || v.isBlank()) return null;
        return v.trim();
    }

    private static String trimOrDefault(String v, String fallback) {
        if (v == null || v.isBlank()) return fallback;
        return v.trim();
    }
}
