package com.partner.backend.common.util;

/** Pakistan CNIC: 13 digits (often shown as 12345-1234567-1). */
public final class CnicNormalizer {

    private CnicNormalizer() {}

    /**
     * Strips non-digits; returns 13-digit string or null if invalid.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        return digits.length() == 13 ? digits : null;
    }

    public static boolean isValid13(String normalized) {
        return normalized != null && normalized.matches("\\d{13}");
    }

    /** Human-friendly form e.g. {@code 41507-0395662-3} for a 13-digit normalized value. */
    public static String formatPakistanDisplay(String thirteenDigits) {
        if (!isValid13(thirteenDigits)) {
            return null;
        }
        return thirteenDigits.substring(0, 5)
                + "-" + thirteenDigits.substring(5, 12)
                + "-" + thirteenDigits.substring(12);
    }
}
