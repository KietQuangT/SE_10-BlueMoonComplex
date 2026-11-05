package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.identity.application.LoginLockPolicy;

class LoginLockPolicyTest {
    @Test void firstSixLevelsMatchApprovedDurations() {
        assertThat(java.util.stream.IntStream.rangeClosed(1, 6).mapToLong(level -> LoginLockPolicy.lockDuration(level).toMinutes()).toArray())
                .containsExactly(1, 5, 10, 15, 30, 60);
    }

    @Test void levelSevenAndBeyondIncreaseBySixtyMinutes() {
        assertThat(LoginLockPolicy.lockDuration(7).toMinutes()).isEqualTo(120);
        assertThat(LoginLockPolicy.lockDuration(8).toMinutes()).isEqualTo(180);
        assertThat(LoginLockPolicy.lockDuration(9).toMinutes()).isEqualTo(240);
    }

    @Test void thresholdIsFiveAtLevelZeroAndOneAfterLock() {
        assertThat(LoginLockPolicy.failureThreshold(0)).isEqualTo(5);
        assertThat(LoginLockPolicy.failureThreshold(1)).isEqualTo(1);
    }

    @Test void safelyHandlesLargeLevelAndDatetimeOverflow() {
        assertThat(LoginLockPolicy.lockDuration(Integer.MAX_VALUE).toMinutes()).isEqualTo(128_849_018_520L);
        assertThat(LoginLockPolicy.lockedUntil(Instant.parse("9999-12-31T23:59:59Z"), 1))
                .isEqualTo(LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_000));
    }

    @Test void lockUntilIsUtc() {
        assertThat(LoginLockPolicy.lockedUntil(Instant.parse("2026-11-05T01:00:00Z"), 1))
                .isEqualTo(LocalDateTime.of(2026, 11, 5, 1, 1).atOffset(ZoneOffset.UTC).toLocalDateTime());
    }
}
