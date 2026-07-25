package com.partner.backend.common.push;

import com.partner.backend.common.entity.ProviderType;

/**
 * Stable OneSignal External User IDs shared with mobile/web clients via {@code OneSignal.login()}.
 */
public final class PushExternalUserId {

    private PushExternalUserId() {
    }

    public static String forProvider(ProviderType providerType, Long providerId) {
        if (providerType == null || providerId == null) {
            return null;
        }
        return providerType.name() + ":" + providerId;
    }

    public static String forAdminEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return "ADMIN:" + email.trim().toLowerCase();
    }
}
