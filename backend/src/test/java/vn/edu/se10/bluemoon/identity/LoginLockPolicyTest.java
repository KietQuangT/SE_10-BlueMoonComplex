package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.TimeZone;
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
                .isEqualTo(Instant.parse("9999-12-31T23:59:59.999999Z"));
    }

    @Test void lockUntilIsUtc() {
        assertThat(LoginLockPolicy.lockedUntil(Instant.parse("2026-11-05T01:00:00Z"), 1))
                .isEqualTo(Instant.parse("2026-11-05T01:01:00Z"));
    }
    @Test void lockComparisonDoesNotDependOnJvmTimezone() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            Instant now = Instant.parse("2026-11-05T12:00:00Z");
            Instant until = LoginLockPolicy.lockedUntil(now, 1);
            assertThat(until).isEqualTo(Instant.parse("2026-11-05T12:01:00Z"));
            assertThat(until.isAfter(now)).isTrue();
            assertThat(until.isAfter(now.plusSeconds(60))).isFalse();
        } finally { TimeZone.setDefault(original); }
    }
}
