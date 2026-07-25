package com.partner.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update does not widen SQLite CHECK constraints.
 * Patient notifications require provider_type = 'PATIENT'.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SqliteNotificationsProviderTypeMigration implements CommandLineRunner {

    private static final String LEGACY_CHECK_MARKER =
            "check (provider_type in ('doctor','pharmacy','lab'))";

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        if (!isSqlite()) {
            return;
        }
        String ddl = jdbc.query(
                "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'notifications'",
                rs -> rs.next() ? rs.getString(1) : null);
        if (ddl == null) {
            return;
        }
        String ddlLower = ddl.toLowerCase();
        boolean hasLegacyCheck = ddlLower.contains(LEGACY_CHECK_MARKER);
        boolean allowsPatient = ddlLower.contains("'patient'");
        if (!hasLegacyCheck || allowsPatient) {
            return;
        }

        log.warn("Migrating notifications table: adding PATIENT to provider_type CHECK constraint");
        jdbc.execute("PRAGMA foreign_keys = OFF");
        try {
            jdbc.execute("""
                    CREATE TABLE notifications_mig (
                        id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
                        created_at timestamp,
                        provider_id bigint NOT NULL,
                        provider_type varchar(255) NOT NULL,
                        is_read boolean,
                        message varchar(2000),
                        title varchar(255) NOT NULL
                    )
                    """);
            jdbc.execute("""
                    INSERT INTO notifications_mig (
                        id, created_at, provider_id, provider_type, is_read, message, title
                    )
                    SELECT id, created_at, provider_id, provider_type, is_read, message, title
                    FROM notifications
                    """);
            jdbc.execute("DROP TABLE notifications");
            jdbc.execute("ALTER TABLE notifications_mig RENAME TO notifications");
            log.info("notifications table migrated — PATIENT provider_type is now allowed");
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
