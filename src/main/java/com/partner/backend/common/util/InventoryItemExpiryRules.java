package com.partner.backend.common.util;

import com.partner.backend.common.entity.InventoryItem;
import com.partner.backend.common.entity.InventoryItemStatus;

import java.time.LocalDate;
import java.time.ZoneId;

public final class InventoryItemExpiryRules {

    public static final ZoneId PAKISTAN_ZONE = ZoneId.of("Asia/Karachi");

    private InventoryItemExpiryRules() {
    }

    public static LocalDate todayInPakistan() {
        return LocalDate.now(PAKISTAN_ZONE);
    }

    public static boolean isExpired(InventoryItem item, LocalDate today) {
        if (item.getStatus() == InventoryItemStatus.EXPIRED) {
            return true;
        }
        LocalDate expiryDate = item.getExpiryDate();
        return expiryDate != null && expiryDate.isBefore(today);
    }

    public static boolean isSellable(InventoryItem item) {
        LocalDate today = todayInPakistan();
        return item.getStatus() == InventoryItemStatus.ACTIVE
                && item.getQuantity() != null
                && item.getQuantity() > 0
                && !isExpired(item, today);
    }
}
