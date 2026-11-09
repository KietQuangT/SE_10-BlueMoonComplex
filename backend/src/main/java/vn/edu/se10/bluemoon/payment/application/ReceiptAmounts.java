package vn.edu.se10.bluemoon.payment.application;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Splits an integer VND receipt into invoice payment and household credit. */
public final class ReceiptAmounts {
    private ReceiptAmounts() { }

    public record Result(BigDecimal applied, BigDecimal remaining, BigDecimal credit) { }

    public static Result apply(BigDecimal debt, BigDecimal amount) {
        BigDecimal due = integer(debt);
        BigDecimal received = integer(amount);
        if (due.signum() < 0 || received.signum() <= 0 || received.precision() > 24) {
            throw new IllegalArgumentException("Invalid receipt amount or debt");
        }
        BigDecimal applied = due.min(received);
        return new Result(applied, due.subtract(applied), received.subtract(applied));
    }

    static BigDecimal integer(BigDecimal value) {
        if (value == null) throw new IllegalArgumentException("Amount is required");
        if (value.signum() == 0) return BigDecimal.ZERO;
        long integerDigits = (long) value.precision() - value.scale();
        if (integerDigits <= 0 || integerDigits > 24) {
            throw new IllegalArgumentException("Amount must be an integer of at most 24 digits");
        }
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() > 0) {
            throw new IllegalArgumentException("Amount must be an integer of at most 24 digits");
        }
        return normalized.setScale(0, RoundingMode.UNNECESSARY);
    }
}
