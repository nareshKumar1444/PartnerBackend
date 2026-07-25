package com.partner.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update does not remove SQLite CHECK constraints when a column
 * changes from enum to free-form string. Legacy DBs only allow the six built-in enum keys.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class SqlitePatientHealthReadingsMetricKeyMigration implements CommandLineRunner {

    private static final String LEGACY_CHECK_MARKER =
            "check (metric_key in ('heart_rate','blood_pressure','blood_sugar','bmi','liver_health','kidney_health'))";

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        if (!isSqlite()) {
            return;
        }
        String ddl = jdbc.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'patient_health_readings'",
                rs -> rs.next() ? rs.getString(1) : null);
        if (ddl == null) {
            return;
        }
        String ddlLower = ddl.toLowerCase();
        if (!ddlLower.contains(LEGACY_CHECK_MARKER)) {
            return;
        }

        log.warn("Migrating patient_health_readings: removing legacy metric_key CHECK constraint");
        jdbc.execute("PRAGMA foreign_keys = OFF");
        try {
            jdbc.execute("""
                    CREATE TABLE patient_health_readings_mig (
                        id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
                        created_at timestamp,
                        updated_at timestamp,
                        metric_key varchar(64) NOT NULL,
                        note varchar(2000),
                        reading_date date NOT NULL,
                        unit varchar(64) NOT NULL,
                        value_text varchar(128) NOT NULL,
                        patient_id bigint NOT NULL
                    )
                    """);
            jdbc.execute("""
                    INSERT INTO patient_health_readings_mig (
                        id, created_at, updated_at, metric_key, note, reading_date, unit, value_text, patient_id
                    )
                    SELECT
                        id,
                        created_at,
                        updated_at,
                        CASE metric_key
                            WHEN 'HEART_RATE' THEN 'heart_rate'
                            WHEN 'BLOOD_PRESSURE' THEN 'blood_pressure'
                            WHEN 'BLOOD_SUGAR' THEN 'blood_sugar'
                            WHEN 'BMI' THEN 'bmi'
                            WHEN 'LIVER_HEALTH' THEN 'liver_health'
                            WHEN 'KIDNEY_HEALTH' THEN 'kidney_health'
                            ELSE lower(metric_key)
                        END,
                        note,
                        reading_date,
                        unit,
                        value_text,
                        patient_id
                    FROM patient_health_readings
                    """);
            jdbc.execute("DROP TABLE patient_health_readings");
            jdbc.execute("ALTER TABLE patient_health_readings_mig RENAME TO patient_health_readings");
            log.info("patient_health_readings migrated — dynamic metric keys are now allowed");
        } finally {
            jdbc.execute("PRAGMA foreign_keys = ON");
        }
    }

    private boolean isSqlite() {
        try {
            String url = jdbc.getDataSource().getConnection().getMetaData().getURL();
            return url != null && url.toLowerCase().contains("sqlite");
        } catch (Exception e) {
            log.debug("Could not detect SQLite datasource: {}", e.getMessage());
            return false;
        }
    }
}
