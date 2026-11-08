package vn.edu.se10.bluemoon.billing.application;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;
import vn.edu.se10.bluemoon.common.error.BadRequestException;

public record ContributionInput(long campaignId, long residentId, BigDecimal amount, LocalDate paidOn,
                                String lateReason, String idempotencyKey, String fingerprint) {
    public static ContributionInput parse(long campaignId, long residentId, String amount, LocalDate paidOn,
                                          String lateReason, String key) {
        if (amount == null || !amount.matches("[0-9]{1,18}")) throw new BadRequestException("INVALID_AMOUNT");
        BigDecimal money = new BigDecimal(amount);
        if (money.signum() <= 0) throw new BadRequestException("INVALID_AMOUNT");
        if (paidOn == null) throw new BadRequestException("INVALID_DATE");
        if (key == null || !key.matches("(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")) {
            throw new BadRequestException("INVALID_IDEMPOTENCY_KEY");
        }
        String normalizedReason = lateReason == null || lateReason.isBlank() ? null : lateReason.strip();
        if (normalizedReason != null && normalizedReason.codePointCount(0, normalizedReason.length()) > 255) throw new BadRequestException("REASON_REQUIRED");
        String normalizedKey = UUID.fromString(key).toString();
        return new ContributionInput(campaignId, residentId, money, paidOn, normalizedReason, normalizedKey,
                fingerprint(campaignId, residentId, money, paidOn, normalizedReason));
    }

    private static String fingerprint(long campaignId, long residentId, BigDecimal amount, LocalDate paidOn, String reason) {
        try {
            var bytes = new ByteArrayOutputStream();
            try (var data = new DataOutputStream(bytes)) {
                data.writeLong(campaignId);
                data.writeLong(residentId);
                data.writeUTF(amount.toPlainString());
                data.writeUTF(paidOn.toString());
                data.writeBoolean(reason != null);
                if (reason != null) data.writeUTF(reason);
            }
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes.toByteArray()));
        } catch (IOException | NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Cannot calculate contribution fingerprint", exception);
        }
    }
}
