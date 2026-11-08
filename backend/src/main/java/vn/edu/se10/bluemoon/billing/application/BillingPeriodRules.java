package vn.edu.se10.bluemoon.billing.application;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;

public final class BillingPeriodRules {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final Clock clock;

    public BillingPeriodRules(Clock clock) {
        this.clock = clock;
    }

    public void validateCreation(String period, LocalDate dueDate) {
        YearMonth month = parse(period);
        if (!month.equals(currentMonth())) {
            throw new BadRequestException("INVALID_PERIOD");
        }
        requireDueDateInMonth(month, dueDate);
    }

    public void requireDueDateInMonth(YearMonth month, LocalDate dueDate) {
        if (dueDate == null || !YearMonth.from(dueDate).equals(month)) {
            throw new BadRequestException("INVALID_DATE");
        }
    }

    public void requireCurrent(String period) {
        if (!parse(period).equals(currentMonth())) {
            throw new ConflictException("BILLING_PERIOD_EXPIRED");
        }
    }

    public void requireDueDateNotPast(LocalDate dueDate) {
        if (dueDate == null || dueDate.isBefore(LocalDate.now(clock.withZone(BUSINESS_ZONE)))) {
            throw new BadRequestException("INVALID_DATE");
        }
    }

    private YearMonth currentMonth() {
        return YearMonth.now(clock.withZone(BUSINESS_ZONE));
    }

    private YearMonth parse(String value) {
        if (value == null || !value.matches("[0-9]{4}-(0[1-9]|1[0-2])")) {
            throw new BadRequestException("INVALID_PERIOD");
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new BadRequestException("INVALID_PERIOD");
        }
    }
}
