package vn.edu.se10.bluemoon.identity.application;

import java.util.Locale;

public final class EmailNormalizer {
    private EmailNormalizer() {}
    public static String normalize(String email) {
        if (email == null || email.chars().anyMatch(Character::isISOControl)) throw new IllegalArgumentException("Email không hợp lệ");
        String value = email.trim().toLowerCase(Locale.ROOT);
        int at = value.indexOf('@');
        if (value.length() > 254 || at <= 0 || at != value.lastIndexOf('@') || at == value.length() - 1
                || value.substring(at + 1).indexOf('.') <= 0 || value.endsWith(".")
                || value.chars().anyMatch(c -> c <= 32 || c >= 127))
            throw new IllegalArgumentException("Email không hợp lệ");
        return value;
    }
}
