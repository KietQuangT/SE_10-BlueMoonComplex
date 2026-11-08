package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.BillingPeriodRules;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

class BillingPeriodRulesTest {
    // This is 00:30 on 1 November in Vietnam, but still 31 October UTC.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-31T17:30:00Z"), ZoneOffset.UTC);
    private final BillingPeriodRules rules = new BillingPeriodRules(clock);

    @Test
    void creationUsesVietnameseMonthAndRequiresDueDateInThatMonth() {
        assertDoesNotThrow(() -> rules.validateCreation("2026-11", LocalDate.of(2026, 11, 30)));
        assertEquals("INVALID_PERIOD", assertThrows(BadRequestException.class,
                () -> rules.validateCreation("2026-10", LocalDate.of(2026, 10, 31))).code());
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> rules.validateCreation("2026-11", LocalDate.of(2026, 12, 1))).code());
    }

    @Test
    void pastPeriodsCannotChangeAndExpiredDueDatesCannotBeIssued() {
        assertEquals("BILLING_PERIOD_EXPIRED", assertThrows(ConflictException.class,
                () -> rules.requireCurrent("2026-10")).code());
        Clock later = Clock.fixed(Instant.parse("2026-11-15T00:00:00Z"), ZoneOffset.UTC);
        BillingPeriodRules laterRules = new BillingPeriodRules(later);
        assertEquals("INVALID_DATE", assertThrows(BadRequestException.class,
                () -> laterRules.requireDueDateNotPast(LocalDate.of(2026, 11, 14))).code());
        assertDoesNotThrow(() -> laterRules.requireDueDateNotPast(LocalDate.of(2026, 11, 15)));
    }
}
