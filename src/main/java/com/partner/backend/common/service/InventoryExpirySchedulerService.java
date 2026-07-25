package com.partner.backend.common.service;

import com.partner.backend.common.entity.InventoryItemStatus;
import com.partner.backend.common.repository.InventoryItemRepository;
import com.partner.backend.common.util.InventoryItemExpiryRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryExpirySchedulerService {

    private final InventoryItemRepository inventoryItemRepository;

    @Scheduled(cron = "${app.inventory.expiry-cron:0 0 0 * * *}", zone = "Asia/Karachi")
    @Transactional
    public void markExpiredMedicinesAtMidnight() {
        runExpiryJob();
    }

    @Transactional
    public void runExpiryJob() {
        inventoryItemRepository.backfillNullStatus();
        LocalDate today = InventoryItemExpiryRules.todayInPakistan();
        int updated = inventoryItemRepository.markExpiredBefore(
                today, InventoryItemStatus.ACTIVE, InventoryItemStatus.EXPIRED);
        if (updated > 0) {
            log.info("Inventory expiry job (PKT {}): marked {} item(s) as EXPIRED", today, updated);
        }
    }
}
