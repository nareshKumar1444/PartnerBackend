package com.partner.backend.common.service;

import com.partner.backend.common.entity.HealthMetricDefinition;
import com.partner.backend.common.repository.HealthMetricDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HealthMetricDefinitionSeedService {

    private final HealthMetricDefinitionRepository repository;

    @Transactional
    public void seedBuiltinMetricsIfAbsent() {
        seedOneIfAbsent("heart_rate", "Heart Rate", "bpm", "heart", "#E53935", "#FFF5F5",
                "60-100 bpm", 60d, 100d, 1,
                "Your heart rate is the number of times your heart beats per minute.",
                "A resting heart rate between 60-100 beats per minute is considered healthy for adults.");
        seedOneIfAbsent("blood_pressure", "Blood Pressure", "mmHg", "fitness", "#0E7CCA", "#E8F4FD",
                "90/60 - 120/80 mmHg", null, null, 2,
                "Blood pressure measures the force of blood pushing against artery walls.",
                "Normal blood pressure is below 120/80 mmHg.");
        seedOneIfAbsent("blood_sugar", "Blood Sugar", "mg/dL", "water", "#FF9800", "#FFF8E1",
                "70-100 mg/dL (fasting)", 70d, 100d, 3,
                "Blood sugar (glucose) is your body's main source of energy.",
                "Fasting blood sugar below 100 mg/dL is normal.");
        seedOneIfAbsent("bmi", "BMI", "", "body", "#4CAF50", "#F1F8E9",
                "18.5 - 24.9", 18.5, 24.9, 4,
                "Body Mass Index (BMI) is a measure of body fat based on height and weight.",
                "BMI categories: Underweight (<18.5), Normal (18.5-24.9), Overweight (25-29.9).");
        seedOneIfAbsent("liver_health", "Liver Health", "U/L", "medical", "#8E24AA", "#F3E5F5",
                "ALT: 7-56 U/L", 7d, 56d, 5,
                "Liver health is assessed through enzyme levels like ALT.",
                "ALT is an enzyme found primarily in the liver.");
        seedOneIfAbsent("kidney_health", "Kidney Health", "mg/dL", "leaf", "#00897B", "#E0F2F1",
                "Creatinine: 0.7-1.3 mg/dL", 0.6, 1.3, 6,
                "Kidney health is assessed through creatinine levels in the blood.",
                "Creatinine is a waste product filtered by the kidneys.");
    }

    private void seedOneIfAbsent(
            String key, String label, String unit, String icon, String color, String bg,
            String normalRange, Double low, Double high, int sortOrder,
            String description, String detail) {
        if (repository.existsByMetricKey(key)) {
            return;
        }
        repository.save(HealthMetricDefinition.builder()
                .metricKey(key)
                .label(label)
                .unit(unit)
                .icon(icon)
                .color(color)
                .bgColor(bg)
                .normalRange(normalRange)
                .normalLow(low)
                .normalHigh(high)
                .sortOrder(sortOrder)
                .active(true)
                .builtin(true)
                .description(description)
                .detailDescription(detail)
                .build());
    }
}
