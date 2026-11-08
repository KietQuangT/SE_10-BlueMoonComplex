package vn.edu.se10.bluemoon.billing.application;

import java.time.LocalDate;
import java.util.Objects;

/** Date-only receipt window for an already published campaign. */
public record VoluntaryReceiptWindow(
        LocalDate opensOn, LocalDate closesOn, LocalDate publishedOn, LocalDate closedOn) {

    public VoluntaryReceiptWindow {
        Objects.requireNonNull(opensOn);
        Objects.requireNonNull(closesOn);
        Objects.requireNonNull(publishedOn);
    }

    public boolean includes(LocalDate paidOn) {
        return !paidOn.isBefore(opensOn)
                && !paidOn.isBefore(publishedOn)
                && !paidOn.isAfter(closesOn)
                && (closedOn == null || !paidOn.isAfter(closedOn));
    }

    public boolean accepts(LocalDate paidOn, LocalDate today) {
        return !paidOn.isAfter(today) && includes(paidOn);
    }

    public boolean requiresLateReason(LocalDate today) {
        return closedOn != null || today.isAfter(closesOn);
    }
}
