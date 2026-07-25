package com.partner.backend.common.repository;

import com.partner.backend.common.entity.HealthMetricDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HealthMetricDefinitionRepository extends JpaRepository<HealthMetricDefinition, Long> {
    List<HealthMetricDefinition> findAllByOrderBySortOrderAscLabelAsc();

    List<HealthMetricDefinition> findByActiveTrueOrderBySortOrderAscLabelAsc();

    Optional<HealthMetricDefinition> findByMetricKey(String metricKey);

    Optional<HealthMetricDefinition> findByMetricKeyAndActiveTrue(String metricKey);

    boolean existsByMetricKey(String metricKey);
}
