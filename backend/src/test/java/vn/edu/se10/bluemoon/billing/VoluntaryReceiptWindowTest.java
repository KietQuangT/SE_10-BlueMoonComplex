package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.billing.application.VoluntaryReceiptWindow;

class VoluntaryReceiptWindowTest {
    @Test
    void publicationAndEarlyCloseBoundTheInclusiveReceiptWindow() {
        var window = new VoluntaryReceiptWindow(date(1), date(20), date(5), date(15));
        assertFalse(window.includes(date(4)));
        assertTrue(window.includes(date(5)));
        assertTrue(window.includes(date(15)));
        assertFalse(window.includes(date(16)));
    }

    @Test
    void scheduledEndAndTodayBothLimitReceiptDates() {
        var window = new VoluntaryReceiptWindow(date(1), date(20), date(1), null);
        assertTrue(window.accepts(date(19), date(22)));
        assertFalse(window.accepts(date(21), date(22)));
        assertFalse(window.accepts(date(20), date(19)));
        assertTrue(window.requiresLateReason(date(22)));
        assertFalse(window.requiresLateReason(date(20)));
    }

    @Test
    void manualCloseRequiresLateReasonEvenOnClosingDay() {
        var window = new VoluntaryReceiptWindow(date(1), date(20), date(1), date(15));
        assertTrue(window.accepts(date(15), date(15)));
        assertTrue(window.requiresLateReason(date(15)));
    }

    private static LocalDate date(int day) {
        return LocalDate.of(2026, 11, day);
    }
}
