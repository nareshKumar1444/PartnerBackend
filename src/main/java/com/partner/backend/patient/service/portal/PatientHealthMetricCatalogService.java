package com.partner.backend.patient.service.portal;

import com.partner.backend.admin.dto.HealthMetricDefinitionResponse;
import com.partner.backend.common.entity.HealthMetricDefinition;
import com.partner.backend.common.repository.HealthMetricDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientHealthMetricCatalogService {

    private final HealthMetricDefinitionRepository repository;

    @Transactional(readOnly = true)
    public List<HealthMetricDefinitionResponse> listActive() {
        return repository.findByActiveTrueOrderBySortOrderAscLabelAsc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isActiveMetricKey(String metricKey) {
        return repository.findByMetricKeyAndActiveTrue(metricKey.trim().toLowerCase()).isPresent();
    }

    @Transactional(readOnly = true)
    public String resolveUnit(String metricKey, String requestedUnit) {
        if (requestedUnit != null && !requestedUnit.isBlank()) {
            return requestedUnit.trim();
        }
        return repository.findByMetricKeyAndActiveTrue(metricKey.trim().toLowerCase())
                .map(HealthMetricDefinition::getUnit)
                .filter(u -> u != null && !u.isBlank())
                .orElse("—");
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
}
