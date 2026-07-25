package com.partner.backend.common.util;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extract 13-digit Pakistan CNIC sequences from OCR / pasted text. */
public final class CnicTextParser {

    private static final Pattern CNIC_LOOSE =
            Pattern.compile("\\b(\\d{5}[\\-\\s]?\\d{7}[\\-\\s]?\\d)\\b");

    private CnicTextParser() {}

    public static String findFirstNormalized(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        Set<String> seen = new LinkedHashSet<>();
        Matcher m = CNIC_LOOSE.matcher(text);
        while (m.find()) {
            String n = CnicNormalizer.normalize(m.group(1));
            if (n != null && seen.add(n)) {
                return n;
            }
        }
        String digitsOnly = text.replaceAll("[^0-9]", "");
        if (digitsOnly.length() >= 13) {
            for (int i = 0; i <= digitsOnly.length() - 13; i++) {
                String chunk = digitsOnly.substring(i, i + 13);
                if (chunk.matches("\\d{13}")) {
                    return chunk;
                }
            }
        }
        return null;
    }
}
