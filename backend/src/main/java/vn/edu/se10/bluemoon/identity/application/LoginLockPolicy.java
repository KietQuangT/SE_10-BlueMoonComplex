package vn.edu.se10.bluemoon.identity.application;

import java.time.Duration;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** D17 stepped lock policy, including unbounded +60 minute levels after level six. */
public final class LoginLockPolicy {
    private static final long[] INITIAL_MINUTES = {1, 5, 10, 15, 30, 60};
    private static final Instant MYSQL_DATETIME_MAX = LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000).toInstant(ZoneOffset.UTC);
    public static final int INITIAL_FAILURE_THRESHOLD = 5;
    private LoginLockPolicy() {}

    public static int failureThreshold(int lockLevel) { return lockLevel == 0 ? INITIAL_FAILURE_THRESHOLD : 1; }

    public static Duration lockDuration(int resultingLockLevel) {
        if (resultingLockLevel < 1) throw new IllegalArgumentException("Bậc khóa phải dương");
        if (resultingLockLevel <= INITIAL_MINUTES.length) return Duration.ofMinutes(INITIAL_MINUTES[resultingLockLevel - 1]);
        try {
            return Duration.ofMinutes(Math.addExact(60L, Math.multiplyExact(60L, resultingLockLevel - 6L)));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Bậc khóa vượt giới hạn số an toàn", e);
        }
    }

    public static Instant lockedUntil(Instant now, int resultingLockLevel) {
        try {
            Instant until = now.plus(lockDuration(resultingLockLevel));
            if (until.isBefore(Instant.parse("1000-01-01T00:00:00Z"))) throw new IllegalArgumentException("Thời điểm khóa nhỏ hơn DATETIME UTC tối thiểu");
            return until.isAfter(MYSQL_DATETIME_MAX) ? MYSQL_DATETIME_MAX : until;
        } catch (ArithmeticException | DateTimeException e) {
            // MySQL DATETIME has a finite representable ceiling; saturating there avoids wraparound.
            return MYSQL_DATETIME_MAX;
        }
    }
}
