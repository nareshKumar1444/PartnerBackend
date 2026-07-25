package com.partner.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Ensures payment columns exist on SQLite orders table (Hibernate ddl-auto may skip some alters).
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SqliteOrdersPaymentMigration implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    @Override
    public void run(String... args) {
        if (!isSqlite()) {
            return;
        }
        addColumnIfMissing("payment_method", "varchar(32)");
        addColumnIfMissing("online_payment_channel", "varchar(32)");
        addColumnIfMissing("payment_status", "varchar(32)");
        addColumnIfMissing("payment_reference", "varchar(64)");
        addColumnIfMissing("payment_bank_name", "varchar(120)");
    }

    private void addColumnIfMissing(String column, String sqlType) {
        try {
            jdbc.queryForList("SELECT " + column + " FROM orders LIMIT 1");
        } catch (Exception e) {
            log.info("Adding orders.{} column", column);
            jdbc.execute("ALTER TABLE orders ADD COLUMN " + column + " " + sqlType);
        }
    }

    private boolean isSqlite() {
        try {
            String url = jdbc.getDataSource().getConnection().getMetaData().getURL();
            return url != null && url.toLowerCase().contains("sqlite");
        } catch (Exception e) {
            return false;
        }
    }
}
