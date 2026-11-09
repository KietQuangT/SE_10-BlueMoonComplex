package vn.edu.se10.bluemoon.payment.application;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Allocates from sources supplied in receipt creation order. */
public final class CreditAllocationAmounts {
    private CreditAllocationAmounts() { }
    public record Source(long receiptId, BigDecimal available) { }
    public record Share(long receiptId, BigDecimal amount) { }

    public static List<Share> allocate(List<Source> sources, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0) {
            throw new IllegalArgumentException("Invalid allocation amount");
        }
        BigDecimal remaining = amount;
        List<Share> shares = new ArrayList<>();
        for (var source : sources) {
            if (source.available().signum() < 0) throw new IllegalArgumentException("Negative credit");
            BigDecimal used = source.available().min(remaining);
            if (used.signum() > 0) shares.add(new Share(source.receiptId(), used));
            remaining = remaining.subtract(used);
            if (remaining.signum() == 0) return List.copyOf(shares);
        }
        throw new IllegalArgumentException("Insufficient credit");
    }
}
