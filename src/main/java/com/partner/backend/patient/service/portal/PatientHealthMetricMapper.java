package com.partner.backend.patient.service.portal;

import com.partner.backend.common.entity.HealthMetricKey;
import com.partner.backend.common.exception.BadRequestException;

final class PatientHealthMetricMapper {

    private PatientHealthMetricMapper() {}

    static String normalizeStoredKey(String stored) {
        if (stored == null || stored.isBlank()) {
            throw new BadRequestException("metricKey is required");
        }
        String trimmed = stored.trim();
        try {
            return toApiKey(HealthMetricKey.valueOf(trimmed));
        } catch (IllegalArgumentException ignored) {
            return trimmed.toLowerCase();
        }
    }

    static String toApiKey(HealthMetricKey k) {
        return switch (k) {
            case HEART_RATE -> "heart_rate";
            case BLOOD_PRESSURE -> "blood_pressure";
            case BLOOD_SUGAR -> "blood_sugar";
            case BMI -> "bmi";
            case LIVER_HEALTH -> "liver_health";
            case KIDNEY_HEALTH -> "kidney_health";
        };
    }
}
