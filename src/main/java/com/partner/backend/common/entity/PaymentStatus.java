package com.partner.backend.common.entity;

public enum PaymentStatus {
    /** Cash on delivery — collect when order is delivered */
    PENDING,
    /** Online payment confirmed (simulated gateway) */
    PAID
}
