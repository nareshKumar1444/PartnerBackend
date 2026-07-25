package com.partner.backend;

import com.partner.backend.admin.service.AdminAuthService;
import com.partner.backend.common.service.HealthMetricDefinitionSeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminAuthService adminAuthService;
    private final DemoProviderSeedService demoProviderSeedService;
    private final HealthMetricDefinitionSeedService healthMetricDefinitionSeedService;

    @Override
    public void run(String... args) {
        adminAuthService.seedAdmin("admin@healthwallet.pk", "admin123");
        log.info("Admin account ensured: admin@healthwallet.pk");
        demoProviderSeedService.seedDemoProvidersIfAbsent();
        healthMetricDefinitionSeedService.seedBuiltinMetricsIfAbsent();
    }
}
