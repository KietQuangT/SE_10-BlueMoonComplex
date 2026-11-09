package vn.edu.se10.bluemoon.billing.application;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.billing.api.PageResponse;
import vn.edu.se10.bluemoon.billing.api.ResidentAllocation;
import vn.edu.se10.bluemoon.billing.api.ResidentBillingSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceDetail;
import vn.edu.se10.bluemoon.billing.api.ResidentInvoiceSummary;
import vn.edu.se10.bluemoon.billing.api.ResidentReceipt;
import vn.edu.se10.bluemoon.billing.persistence.ResidentInvoiceReadStore;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

@Service
public class ResidentInvoiceService {
    private static final Set<String> PAYMENT_STATUSES = Set.of(
            "ALL", "OUTSTANDING", "UNPAID", "PARTIALLY_PAID", "PAID", "CANCELLED"
    );
    private static final Set<String> RECEIPT_STATUSES = Set.of("ALL", "POSTED", "VOIDED");
    private static final Set<String> ALLOCATION_STATUSES = Set.of("ALL", "ACTIVE", "REVERSED");

    private final ResidentInvoiceReadStore readStore;
    private final HouseholdLookup householdLookup;

    public ResidentInvoiceService(ResidentInvoiceReadStore readStore, HouseholdLookup householdLookup) {
        this.readStore = readStore;
        this.householdLookup = householdLookup;
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResponse<ResidentInvoiceSummary> invoices(AuthenticatedUser user, String period, String paymentStatus, int page, int size) {
        long householdId = validateResidentHousehold(user);
        validatePaging(page, size);
        String validPeriod = validatePeriod(period);
        String validPaymentStatus = validatePaymentStatus(paymentStatus);
        return readStore.invoices(householdId, validPeriod, validPaymentStatus, page, size);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ResidentInvoiceDetail invoiceDetail(AuthenticatedUser user, long invoiceId) {
        long householdId = validateResidentHousehold(user);
        if (invoiceId <= 0) {
            throw new BadRequestException("VALIDATION");
        }
        return readStore.invoiceDetail(householdId, invoiceId);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResponse<ResidentReceipt> receipts(AuthenticatedUser user, Long invoiceId, String status, int page, int size) {
        long householdId = validateResidentHousehold(user);
        validatePaging(page, size);
        if (invoiceId != null && invoiceId <= 0) {
            throw new BadRequestException("INVALID_FILTER");
        }
        String validStatus = validateReceiptStatus(status);
        return readStore.receipts(householdId, invoiceId, validStatus, page, size);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PageResponse<ResidentAllocation> creditAllocations(AuthenticatedUser user, Long invoiceId, String status, int page, int size) {
        long householdId = validateResidentHousehold(user);
        validatePaging(page, size);
        if (invoiceId != null && invoiceId <= 0) {
            throw new BadRequestException("INVALID_FILTER");
        }
        String validStatus = validateAllocationStatus(status);
        return readStore.creditAllocations(householdId, invoiceId, validStatus, page, size);
    }

    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ResidentBillingSummary billingSummary(AuthenticatedUser user) {
        long householdId = validateResidentHousehold(user);
        return readStore.billingSummary(householdId);
    }

    private long validateResidentHousehold(AuthenticatedUser user) {
        if (user == null || !"RESIDENT".equals(user.role()) || user.householdId() == null || user.householdId() <= 0) {
            throw new AccessDeniedException("Không có quyền truy cập.");
        }
        if (householdLookup.findStatus(user.householdId()).isEmpty()) {
            throw new AccessDeniedException("Không có quyền truy cập.");
        }
        return user.householdId();
    }

    private static void validatePaging(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new BadRequestException("INVALID_FILTER");
        }
    }

    private static String validatePeriod(String period) {
        if (period == null || period.isBlank()) {
            return null;
        }
        String trimmed = period.trim();
        if (!trimmed.matches("^[0-9]{4}-[0-9]{2}$")) {
            throw new BadRequestException("INVALID_FILTER");
        }
        try {
            YearMonth ym = YearMonth.parse(trimmed);
            if (ym.getYear() < 1000 || ym.getYear() > 9999) {
                throw new BadRequestException("INVALID_FILTER");
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return trimmed;
    }

    private static String validatePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.isBlank()) {
            return "ALL";
        }
        String trimmed = paymentStatus.trim();
        if (!PAYMENT_STATUSES.contains(trimmed)) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return trimmed;
    }

    private static String validateReceiptStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALL";
        }
        String trimmed = status.trim();
        if (!RECEIPT_STATUSES.contains(trimmed)) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return trimmed;
    }

    private static String validateAllocationStatus(String status) {
        if (status == null || status.isBlank()) {
            return "ALL";
        }
        String trimmed = status.trim();
        if (!ALLOCATION_STATUSES.contains(trimmed)) {
            throw new BadRequestException("INVALID_FILTER");
        }
        return trimmed;
    }
}
