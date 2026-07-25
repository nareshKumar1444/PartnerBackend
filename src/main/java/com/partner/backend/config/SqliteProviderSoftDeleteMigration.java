package com.partner.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * SQLite cannot add {@code NOT NULL} columns without a DEFAULT on existing tables.
 * Runs before {@code entityManagerFactory} so Hibernate sees soft-delete columns.
 */
@Slf4j
@Component("sqliteProviderSoftDeleteMigration")
@RequiredArgsConstructor
class SqliteProviderSoftDeleteMigration implements InitializingBean {

    private final JdbcTemplate jdbc;

    @Override
    public void afterPropertiesSet() {
        if (!isSqlite()) {
            return;
        }
        migrateTable("doctors");
        migrateTable("pharmacies");
        migrateTable("labs");
    }

    /*
     * --- Previous implementation (kept for reference) ---
     *
     * private void migrateTable(String table) {
     *     addColumnIfMissing(table, "deleted", "INTEGER NOT NULL DEFAULT 0");
     *     addColumnIfMissing(table, "deleted_at", "timestamp");
     * }
     *
     * private void addColumnIfMissing(String table, String column, String sqlType) {
     *     try {
     *         jdbc.queryForList("SELECT " + column + " FROM " + table + " LIMIT 1");
     *     } catch (Exception e) {
     *         log.info("Adding {}.{} column for provider soft-delete", table, column);
     *         jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + sqlType);
     *     }
     * }
     *
     * Issue on fresh Railway DB: SELECT fails when table is missing, then ALTER TABLE
     * also fails with "no such table: doctors".
     */

    private void migrateTable(String table) {
        if (!tableExists(table)) {
            log.debug("Skipping soft-delete migration for {} — table not created yet (Hibernate will create it)", table);
            return;
        }
        addColumnIfMissing(table, "deleted", "INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(table, "deleted_at", "timestamp");
    }

    private boolean tableExists(String table) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?",
                Integer.class,
                table);
        return count != null && count > 0;
    }

    private boolean columnExists(String table, String column) {
        return jdbc.queryForList("PRAGMA table_info(" + table + ")").stream()
                .anyMatch(row -> column.equalsIgnoreCase(String.valueOf(row.get("name"))));
    }

    private void addColumnIfMissing(String table, String column, String sqlType) {
        if (columnExists(table, column)) {
            return;
        }
        log.info("Adding {}.{} column for provider soft-delete", table, column);
        jdbc.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + sqlType);
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

@Configuration
class SqliteProviderSoftDeleteMigrationOrdering {

    @Bean
    static BeanFactoryPostProcessor entityManagerFactoryDependsOnSoftDeleteMigration() {
        return beanFactory -> {
            if (!(beanFactory instanceof BeanDefinitionRegistry registry)) {
                return;
            }
            if (!registry.containsBeanDefinition("entityManagerFactory")) {
                return;
            }
            BeanDefinition def = registry.getBeanDefinition("entityManagerFactory");
            String[] existing = def.getDependsOn();
            if (existing != null && Arrays.asList(existing).contains("sqliteProviderSoftDeleteMigration")) {
                return;
            }
            String[] merged = existing == null
                    ? new String[] { "sqliteProviderSoftDeleteMigration" }
                    : Arrays.copyOf(existing, existing.length + 1);
            if (existing != null) {
                merged[existing.length] = "sqliteProviderSoftDeleteMigration";
            }
            def.setDependsOn(merged);
        };
    }
}
