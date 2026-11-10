package vn.edu.se10.bluemoon.reporting.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptsReportRequest;
import vn.edu.se10.bluemoon.reporting.application.CashReceiptsReportService;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportRequest;
import vn.edu.se10.bluemoon.reporting.application.FeeCollectionReportService;
import vn.edu.se10.bluemoon.reporting.application.ResidencyReportService;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportExportController {
    private static final String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FILENAME_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(VIETNAM_ZONE);

    private final FeeCollectionReportService feeCollectionService;
    private final CashReceiptsReportService cashReceiptsService;
    private final ResidencyReportService residencyService;
    private final Clock clock;

    public ReportExportController(
        FeeCollectionReportService feeCollectionService,
        CashReceiptsReportService cashReceiptsService,
        ResidencyReportService residencyService,
        Clock clock
    ) {
        this.feeCollectionService = feeCollectionService;
        this.cashReceiptsService = cashReceiptsService;
        this.residencyService = residencyService;
        this.clock = clock;
    }

    @GetMapping("/fee-collection.xlsx")
    public ResponseEntity<byte[]> feeCollection(HttpServletRequest request) {
        validateExactQueryParams(request, Set.of("period"));
        String period = request.getParameter("period");
        if (period == null || !period.matches("^[0-9]{4}-(?:0[1-9]|1[0-2])$")) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        try {
            YearMonth ym = YearMonth.parse(period);
            if (ym.getYear() < 1000 || ym.getYear() > 9999) {
                throw new BadRequestException("INVALID_REPORT_FILTER");
            }
        } catch (DateTimeParseException e) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        byte[] content = feeCollectionService.export(new FeeCollectionReportRequest(period));
        String filename = "thu-theo-ky-" + period + ".xlsx";

        return buildExcelResponse(content, filename);
    }

    @GetMapping("/cash-receipts.xlsx")
    public ResponseEntity<byte[]> cashReceipts(HttpServletRequest request) {
        validateExactQueryParams(request, Set.of("from", "to"));
        String fromStr = request.getParameter("from");
        String toStr = request.getParameter("to");

        if (fromStr == null || !fromStr.matches("^[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])$") ||
            toStr == null || !toStr.matches("^[0-9]{4}-(?:0[1-9]|1[0-2])-(?:0[1-9]|[12][0-9]|3[01])$")) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromStr, DateTimeFormatter.ISO_LOCAL_DATE);
            to = LocalDate.parse(toStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        if (from.getYear() < 1000 || from.getYear() > 9999 || to.getYear() < 1000 || to.getYear() > 9999) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        LocalDate today = LocalDate.now(clock.withZone(VIETNAM_ZONE));
        if (to.isAfter(today)) {
            throw new BadRequestException("INVALID_REPORT_FILTER");
        }

        byte[] content = cashReceiptsService.export(new CashReceiptsReportRequest(from, to));
        String filename = "tien-mat-" + fromStr + "-" + toStr + ".xlsx";

        return buildExcelResponse(content, filename);
    }

    @GetMapping("/residency.xlsx")
    public ResponseEntity<byte[]> residency(HttpServletRequest request) {
        validateExactQueryParams(request, Set.of());

        byte[] content = residencyService.export();
        String dateStr = FILENAME_DATE_FORMAT.format(clock.instant());
        String filename = "cu-tru-hien-tai-" + dateStr + ".xlsx";

        return buildExcelResponse(content, filename);
    }

    private void validateExactQueryParams(HttpServletRequest request, Set<String> expectedKeys) {
        Map<String, String[]> params = request.getParameterMap();
        for (String key : params.keySet()) {
            if (!expectedKeys.contains(key)) {
                throw new BadRequestException("INVALID_REPORT_FILTER");
            }
            String[] vals = params.get(key);
            if (vals == null || vals.length != 1) {
                throw new BadRequestException("INVALID_REPORT_FILTER");
            }
        }
        for (String expected : expectedKeys) {
            if (!params.containsKey(expected)) {
                throw new BadRequestException("INVALID_REPORT_FILTER");
            }
            String val = request.getParameter(expected);
            if (val == null || val.isBlank()) {
                throw new BadRequestException("INVALID_REPORT_FILTER");
            }
        }
    }

    private ResponseEntity<byte[]> buildExcelResponse(byte[] content, String filename) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .header(HttpHeaders.CONTENT_TYPE, XLSX_CONTENT_TYPE)
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(content);
    }
}
