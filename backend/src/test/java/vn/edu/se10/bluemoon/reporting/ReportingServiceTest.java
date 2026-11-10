package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import vn.edu.se10.bluemoon.reporting.application.*;
import vn.edu.se10.bluemoon.reporting.persistence.CashReceiptsReportReadStore;
import vn.edu.se10.bluemoon.reporting.persistence.FeeCollectionReportReadStore;
import vn.edu.se10.bluemoon.reporting.persistence.ResidencyReportReadStore;

class ReportingServiceTest {
    private static final Instant NOW = Instant.parse("2026-11-10T10:00:00Z");
    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private Clock clock;
    private XlsxReportWriter writer;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZONE);
        writer = mock(XlsxReportWriter.class);
    }

    @Test
    void feeCollectionService_delegatesToStoreAndWriter() {
        FeeCollectionReportReadStore store = mock(FeeCollectionReportReadStore.class);
        FeeCollectionReportService service = new FeeCollectionReportService(store, writer, clock);

        var data = new FeeCollectionReportData(
            new ReportMetadata("FEE_COLLECTION_V1", "1.0", NOW, "Thu theo kỳ"),
            "2026-10",
            new FeeCollectionReportTotals(1, new BigDecimal("100000"), new BigDecimal("100000"), BigDecimal.ZERO, new BigDecimal("100.00")),
            List.of()
        );
        when(store.load(eq("2026-10"), eq(NOW))).thenReturn(data);
        when(writer.writeFeeCollectionReport(data)).thenReturn(new byte[]{1, 2, 3});

        byte[] result = service.export(new FeeCollectionReportRequest("2026-10"));
        assertArrayEquals(new byte[]{1, 2, 3}, result);
        verify(store).load("2026-10", NOW);
        verify(writer).writeFeeCollectionReport(data);
    }

    @Test
    void cashReceiptsService_delegatesToStoreAndWriter() {
        CashReceiptsReportReadStore store = mock(CashReceiptsReportReadStore.class);
        CashReceiptsReportService service = new CashReceiptsReportService(store, writer, clock);

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);
        var data = new CashReceiptReportData(
            new ReportMetadata("CASH_RECEIPTS_V1", "1.0", NOW, "Tiền mặt"),
            from, to,
            new CashReceiptReportTotals(1, new BigDecimal("500000")),
            List.of()
        );
        when(store.load(eq(from), eq(to), eq(NOW))).thenReturn(data);
        when(writer.writeCashReceiptsReport(data)).thenReturn(new byte[]{4, 5, 6});

        byte[] result = service.export(new CashReceiptsReportRequest(from, to));
        assertArrayEquals(new byte[]{4, 5, 6}, result);
        verify(store).load(from, to, NOW);
        verify(writer).writeCashReceiptsReport(data);
    }

    @Test
    void residencyService_delegatesToStoreAndWriter() {
        ResidencyReportReadStore store = mock(ResidencyReportReadStore.class);
        ResidencyReportService service = new ResidencyReportService(store, writer, clock);

        var data = new ResidencyReportData(
            new ReportMetadata("CURRENT_RESIDENCY_V1", "1.0", NOW, "Cư trú"),
            new ResidencyReportTotals(10, 8, 2),
            List.of()
        );
        when(store.load(eq(NOW))).thenReturn(data);
        when(writer.writeResidencyReport(data)).thenReturn(new byte[]{7, 8, 9});

        byte[] result = service.export();
        assertArrayEquals(new byte[]{7, 8, 9}, result);
        verify(store).load(NOW);
        verify(writer).writeResidencyReport(data);
    }
}
