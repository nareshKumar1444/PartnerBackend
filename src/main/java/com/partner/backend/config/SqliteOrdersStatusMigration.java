package com.partner.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update does not widen SQLite CHECK constraints on existing columns.
 * Older databases only allowed PENDING/PROCESSING/COMPLETED/CANCELLED, which breaks REJECTED/ACCEPTED etc.
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SqliteOrdersStatusMigration implements CommandLineRunner {

    /** Legacy SQLite CHECK — compare case-insensitively (ddl is lowercased before contains). */
    private static final String LEGACY_CHECK_MARKER =
            "check (status in ('pending','processing','completed','cancelled'))";

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        if (!isSqlite()) {
            return;
        }
        String ddl = jdbc.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'orders'",
                rs -> rs.next() ? rs.getString(1) : null);
        if (ddl == null) {
            return;
        }
        String ddlLower = ddl.toLowerCase();
        boolean hasLegacyCheck = ddlLower.contains(LEGACY_CHECK_MARKER);
        boolean allowsRejected = ddlLower.contains("'rejected'") || !ddlLower.contains("check (status");
        if (!hasLegacyCheck || allowsRejected) {
            return;
        }

        log.warn("Migrating orders table: removing legacy status CHECK constraint");
        jdbc.execute("PRAGMA foreign_keys = OFF");
        try {
            jdbc.execute("""
                    CREATE TABLE orders_mig (
                        id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
                        created_at timestamp,
                        updated_at timestamp,
                        delivery_address varchar(500),
                        delivery_fee numeric(10,2),
                        patient_name varchar(255),
                        patient_phone varchar(255),
                        status varchar(255) NOT NULL,
                        total_amount numeric(10,2),
                        patient_id bigint,
                        pharmacy_id bigint NOT NULL
                    )
                    """);
            jdbc.execute("""
                    INSERT INTO orders_mig (
                        id, created_at, updated_at, delivery_address, delivery_fee,
                        patient_name, patient_phone, status, total_amount, patient_id, pharmacy_id
                    )
                    SELECT
                        id, created_at, updated_at, delivery_address, delivery_fee,
                        patient_name, patient_phone, status, total_amount, patient_id, pharmacy_id
                    FROM orders
                    """);
            jdbc.execute("DROP TABLE orders");
            jdbc.execute("ALTER TABLE orders_mig RENAME TO orders");
            log.info("orders table migrated — all OrderStatus values are now allowed");
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
