package vn.edu.se10.bluemoon.reporting.application;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.reporting.persistence.FeeCollectionReportReadStore;

@Service
public class FeeCollectionReportService {
    private final FeeCollectionReportReadStore readStore;
    private final XlsxReportWriter writer;
    private final Clock clock;

    public FeeCollectionReportService(FeeCollectionReportReadStore readStore, XlsxReportWriter writer, Clock clock) {
        this.readStore = readStore;
        this.writer = writer;
        this.clock = clock;
    }

    public byte[] export(FeeCollectionReportRequest request) {
        Instant exportedAt = Instant.now(clock);
        FeeCollectionReportData data = readStore.load(request.period(), exportedAt);
        return writer.writeFeeCollectionReport(data);
    }
}
