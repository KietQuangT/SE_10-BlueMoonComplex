package vn.edu.se10.bluemoon.reporting.application;

import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import vn.edu.se10.bluemoon.reporting.persistence.ResidencyReportReadStore;

@Service
public class ResidencyReportService {
    private final ResidencyReportReadStore readStore;
    private final XlsxReportWriter writer;
    private final Clock clock;

    public ResidencyReportService(ResidencyReportReadStore readStore, XlsxReportWriter writer, Clock clock) {
        this.readStore = readStore;
        this.writer = writer;
        this.clock = clock;
    }

    public byte[] export() {
        Instant exportedAt = Instant.now(clock);
        ResidencyReportData data = readStore.load(exportedAt);
        return writer.writeResidencyReport(data);
    }
}
