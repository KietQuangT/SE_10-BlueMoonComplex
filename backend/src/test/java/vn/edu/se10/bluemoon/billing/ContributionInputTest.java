package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.ContributionInput;
import vn.edu.se10.bluemoon.common.error.BadRequestException;

class ContributionInputTest {
    private static final String KEY = "6fbca292-b3b5-44de-bbb8-291b9ee1c213";

    @Test
    void fingerprintUsesNormalizedAmountAndReason() {
        var first = ContributionInput.parse(3, 4, "00100", LocalDate.of(2026, 11, 1), " reason ", KEY);
        var retry = ContributionInput.parse(3, 4, "100", LocalDate.of(2026, 11, 1), "reason", KEY);
        var changed = ContributionInput.parse(3, 4, "101", LocalDate.of(2026, 11, 1), "reason", KEY);
        assertEquals(first.fingerprint(), retry.fingerprint());
        assertNotEquals(first.fingerprint(), changed.fingerprint());
    }

    @Test
    void rejectsFractionZeroOversizeAndNonCanonicalUuid() {
        for (String amount : new String[]{"0", "-1", "1.5", "1000000000000000000"}) {
            assertEquals("INVALID_AMOUNT", assertThrows(BadRequestException.class,
                    () -> ContributionInput.parse(3, 4, amount, LocalDate.of(2026, 11, 1), null, KEY)).code());
        }
        assertEquals("INVALID_IDEMPOTENCY_KEY", assertThrows(BadRequestException.class,
                () -> ContributionInput.parse(3, 4, "1", LocalDate.of(2026, 11, 1), null, "1-1-1-1-1")).code());
    }
    @Test
    void reasonLimitCountsUnicodeCharactersRatherThanUtf16Units() {
        String reason = "😀".repeat(255);
        var input = ContributionInput.parse(3, 4, "1", LocalDate.of(2026, 11, 1), reason, KEY);
        assertEquals(reason, input.lateReason());
        assertEquals("REASON_REQUIRED", assertThrows(BadRequestException.class,
                () -> ContributionInput.parse(3, 4, "1", LocalDate.of(2026, 11, 1), reason + "😀", KEY)).code());
    }

}
