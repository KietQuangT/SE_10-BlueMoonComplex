package vn.edu.se10.bluemoon.reporting.application;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.reporting.persistence.CashReceiptsReportReadStore;

@Service
public class CashReceiptsReportService {
    private final CashReceiptsReportReadStore readStore;
    private final XlsxReportWriter writer;
    private final Clock clock;

    public CashReceiptsReportService(CashReceiptsReportReadStore readStore, XlsxReportWriter writer, Clock clock) {
        this.readStore = readStore;
        this.writer = writer;
        this.clock = clock;
    }

    public byte[] export(CashReceiptsReportRequest request) {
        Instant exportedAt = Instant.now(clock);
        CashReceiptReportData data = readStore.load(request.from(), request.to(), exportedAt);
        return writer.writeCashReceiptsReport(data);
    }
}
