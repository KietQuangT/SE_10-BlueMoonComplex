package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.ForbiddenException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageWriter;
import vn.edu.se10.bluemoon.reporting.application.AgencyPackageRow;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.application.PackagePreviewResponse;
import vn.edu.se10.bluemoon.reporting.application.PackageSummaryPage;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyDataPackageStore;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyPackageRecord;

class AgencyDataPackageServiceTest {
    private AgencyDataPackageStore store;
    private AgencyDataPackageWriter writer;
    private AuditService auditService;
    private Clock clock;
    private AgencyDataPackageService service;

    private final Instant baseTime = Instant.parse("2026-11-10T10:00:00Z");
    private final ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

    @BeforeEach
    void setUp() {
        store = mock(AgencyDataPackageStore.class);
        writer = mock(AgencyDataPackageWriter.class);
        auditService = mock(AuditService.class);
        clock = Clock.fixed(baseTime, zone);
        service = new AgencyDataPackageService(store, writer, auditService, clock);
    }

    @Test
    void createPackage_validRequest_success() {
        CreatePackageRequest req = new CreatePackageRequest(List.of(1L, 2L), "CONG_AN_Q1", "REF-001", "LIC-002");
        when(store.validateActiveHouseholdIds(List.of(1L, 2L))).thenReturn(List.of(1L, 2L));

        List<AgencyPackageRow> rows = List.of(
            new AgencyPackageRow("H01", "A101", "User A", "RESIDING"),
            new AgencyPackageRow("H02", "A102", "User B", "TEMPORARILY_ABSENT")
        );
        when(store.fetchSnapshotResidents(List.of(1L, 2L))).thenReturn(rows);

        byte[] fakeBytes = new byte[]{1, 2, 3};
        when(writer.writePackage(eq(baseTime), eq(2L), eq(1L), eq(1L), eq(rows))).thenReturn(fakeBytes);
        when(store.insertPackage(any(AgencyPackageRecord.class))).thenReturn(100L);

        AgencyPackageRecord saved = new AgencyPackageRecord(
            100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            "CONG_AN_Q1", "REF-001", "LIC-002", 1L, baseTime,
            null, null, null, null, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            AgencyDataPackageWriter.computeSha256(fakeBytes),
            2L, 2L, 1L, 1L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(saved));

        PackageDetail detail = service.createPackage(req, 1L);
        assertNotNull(detail);
        assertEquals(100L, detail.id());
        assertEquals("DRAFT", detail.status());
        assertEquals(1L, detail.version());
        assertEquals(2L, detail.householdCount());
        assertEquals(2L, detail.residentCount());

        verify(auditService).record(eq(1L), eq("AGENCY_PACKAGE_CREATED"), eq("AgencyDataPackage"), eq("100"), anyMap());
        verify(store).insertPackageHouseholds(eq(100L), eq(List.of(1L, 2L)));
        verify(store).insertPackageContent(eq(100L), eq(fakeBytes), eq(3L), anyString());
    }

    @Test
    void createPackage_scopeInvalid_duplicateOrNonPositive() {
        CreatePackageRequest dupReq = new CreatePackageRequest(List.of(1L, 1L), "CONG_AN_Q1", "REF-001", "LIC-002");
        assertThrows(BadRequestException.class, () -> service.createPackage(dupReq, 1L));

        CreatePackageRequest negReq = new CreatePackageRequest(List.of(1L, -5L), "CONG_AN_Q1", "REF-001", "LIC-002");
        assertThrows(BadRequestException.class, () -> service.createPackage(negReq, 1L));

        CreatePackageRequest emptyReq = new CreatePackageRequest(List.of(), "CONG_AN_Q1", "REF-001", "LIC-002");
        assertThrows(BadRequestException.class, () -> service.createPackage(emptyReq, 1L));
    }

    @Test
    void createPackage_scopeExceeds1000_throwsBadRequest() {
        List<Long> many = new ArrayList<>();
        for (long i = 1; i <= 1001; i++) {
            many.add(i);
        }
        CreatePackageRequest req = new CreatePackageRequest(many, "CONG_AN_Q1", "REF-001", "LIC-002");
        assertThrows(BadRequestException.class, () -> service.createPackage(req, 1L));
    }

    @Test
    void createPackage_inactiveOrNonExistentHousehold_throwsBadRequest() {
        CreatePackageRequest req = new CreatePackageRequest(List.of(1L, 2L), "CONG_AN_Q1", "REF-001", "LIC-002");
        when(store.validateActiveHouseholdIds(List.of(1L, 2L))).thenReturn(List.of(1L)); // 2L not found or inactive
        assertThrows(BadRequestException.class, () -> service.createPackage(req, 1L));
    }

    @Test
    void createPackage_invalidAgencyCodeOrReferences_throwsBadRequest() {
        CreatePackageRequest badAgency = new CreatePackageRequest(List.of(1L), "lowercase", "REF", "LIC");
        assertThrows(BadRequestException.class, () -> service.createPackage(badAgency, 1L));

        CreatePackageRequest badRef = new CreatePackageRequest(List.of(1L), "AGENCY", "REF with spaces", "LIC");
        assertThrows(BadRequestException.class, () -> service.createPackage(badRef, 1L));
    }

    @Test
    void approvePackage_creatorAttemptsApproval_throwsForbidden() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime, // createdBy = 1L
            null, null, null, null, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        PackageActionRequest req = new PackageActionRequest(1L, "hash123");
        // actor is 1L (same as creator)
        ForbiddenException ex = assertThrows(ForbiddenException.class, () -> service.approvePackage(100L, req, 1L));
        assertEquals("PACKAGE_SELF_DECISION_NOT_ALLOWED", ex.code());
    }

    @Test
    void approvePackage_otherAdminApproves_success() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            null, null, null, null, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        AgencyPackageRecord approved = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(approved));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(10L));

        PackageActionRequest req = new PackageActionRequest(1L, "hash123");
        PackageDetail res = service.approvePackage(100L, req, 2L); // actor is 2L

        assertEquals("APPROVED", res.status());
        assertEquals(2L, res.version());
        verify(store).updatePackageApprove(eq(100L), eq(2L), eq(baseTime), eq(baseTime.plus(7, ChronoUnit.DAYS)), eq(2L));
        verify(auditService).record(eq(2L), eq("AGENCY_PACKAGE_APPROVED"), eq("AgencyDataPackage"), eq("100"), anyMap());
    }

    @Test
    void approvePackage_staleVersionOrHash_throwsConflict() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "DRAFT", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            null, null, null, null, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        PackageActionRequest wrongVersion = new PackageActionRequest(1L, "hash123");
        assertThrows(ConflictException.class, () -> service.approvePackage(100L, wrongVersion, 2L));

        PackageActionRequest wrongHash = new PackageActionRequest(2L, "wronghash");
        assertThrows(ConflictException.class, () -> service.approvePackage(100L, wrongHash, 2L));
    }

    @Test
    void rejectPackage_creatorRejectsOwnDraft_success() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime, // creator 1L
            null, null, null, null, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        AgencyPackageRecord rejected = new AgencyPackageRecord(
            100L, "REJECTED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            null, null, 1L, baseTime, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(rejected));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(10L));

        PackageActionRequest req = new PackageActionRequest(1L, "hash123");
        PackageDetail res = service.rejectPackage(100L, req, 1L); // creator 1L rejects
        assertEquals("REJECTED", res.status());
        assertEquals(2L, res.version());
        verify(auditService).record(eq(1L), eq("AGENCY_PACKAGE_REJECTED"), eq("AgencyDataPackage"), eq("100"), anyMap());
    }

    @Test
    void revokePackage_approvedPackage_success() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        AgencyPackageRecord revoked = new AgencyPackageRecord(
            100L, "REVOKED", 3L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, 3L, baseTime, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(revoked));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(10L));

        PackageActionRequest req = new PackageActionRequest(2L, "hash123");
        PackageDetail res = service.revokePackage(100L, req, 3L);
        assertEquals("REVOKED", res.status());
        assertEquals(3L, res.version());
        verify(auditService).record(eq(3L), eq("AGENCY_PACKAGE_REVOKED"), eq("AgencyDataPackage"), eq("100"), anyMap());
    }

    @Test
    void handoffPackage_approvedPackage_success() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));

        AgencyPackageRecord handedOff = new AgencyPackageRecord(
            100L, "APPROVED", 3L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, 2L, baseTime,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(handedOff));
        when(store.findHouseholdIdsByPackageId(100L)).thenReturn(List.of(10L));

        PackageActionRequest req = new PackageActionRequest(2L, "hash123");
        PackageDetail res = service.handoffPackage(100L, req, 2L);
        assertEquals(3L, res.version());
        verify(auditService).record(eq(2L), eq("AGENCY_PACKAGE_HANDED_OFF"), eq("AgencyDataPackage"), eq("100"), anyMap());
    }

    @Test
    void handoffPackage_duplicateHandoff_throwsConflict() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 3L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, 2L, baseTime, // already handed off!
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash123", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        PackageActionRequest req = new PackageActionRequest(3L, "hash123");
        assertThrows(ConflictException.class, () -> service.handoffPackage(100L, req, 2L));
    }

    @Test
    void downloadPackage_approvedWithinDeadline_success() {
        byte[] bytes = new byte[]{10, 20, 30};
        String hash = AgencyDataPackageWriter.computeSha256(bytes);

        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            hash, 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(bytes));

        byte[] result = service.downloadPackage(100L, 2L);
        assertArrayEquals(bytes, result);
        verify(auditService).record(eq(2L), eq("AGENCY_PACKAGE_DOWNLOAD_AUTHORIZED"), eq("AgencyDataPackage"), eq("100"), anyMap());
    }

    @Test
    void downloadPackage_expired_throwsConflict() {
        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.minusSeconds(10), baseTime.plus(30, ChronoUnit.DAYS), null, // downloadUntil in the past!
            "hash", 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        ConflictException ex = assertThrows(ConflictException.class, () -> service.downloadPackage(100L, 2L));
        assertEquals("PACKAGE_DOWNLOAD_EXPIRED", ex.code());
    }

    @Test
    void previewPackage_blockedForRejectedOrRevoked() {
        AgencyPackageRecord rejected = new AgencyPackageRecord(
            100L, "REJECTED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            null, null, 1L, baseTime, null, null, null, null,
            null, baseTime.plus(30, ChronoUnit.DAYS), null,
            "hash", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(rejected));
        ConflictException ex = assertThrows(ConflictException.class, () -> service.previewPackage(100L, 0, 20, 1L));
        assertEquals("PACKAGE_STATE_CONFLICT", ex.code());
    }

    @Test
    void metadataExpiredAfter5Years_notFound() {
        Instant oldTime = baseTime.minus(6 * 365, ChronoUnit.DAYS);
        AgencyPackageRecord oldPkg = new AgencyPackageRecord(
            100L, "DRAFT", 1L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, oldTime,
            null, null, null, null, null, null, null, null,
            null, oldTime.plus(30, ChronoUnit.DAYS), null,
            "hash", 1L, 1L, 1L, 0L
        );
        when(store.findPackageById(100L)).thenReturn(Optional.of(oldPkg));
        assertThrows(NotFoundException.class, () -> service.getPackageDetail(100L, 1L));
    }

    @Test
    void snapshotIsolation_sourceChangesDoNotAffectPackageBytesOrPreview() {
        byte[] originalBytes = new byte[]{5, 6, 7};
        String hash = AgencyDataPackageWriter.computeSha256(originalBytes);

        AgencyPackageRecord pkg = new AgencyPackageRecord(
            100L, "APPROVED", 2L, "AGENCY_RESIDENCY_V1", "1.0",
            "AGENCY", "REF", "LIC", 1L, baseTime,
            2L, baseTime, null, null, null, null, null, null,
            baseTime.plus(7, ChronoUnit.DAYS), baseTime.plus(30, ChronoUnit.DAYS), null,
            hash, 1L, 1L, 1L, 0L
        );
        when(store.findPackageForUpdate(100L)).thenReturn(Optional.of(pkg));
        when(store.findPackageById(100L)).thenReturn(Optional.of(pkg));
        when(store.findPackageContentBytes(100L)).thenReturn(Optional.of(originalBytes));
        when(writer.parseWorkbookRows(originalBytes)).thenReturn(List.of(
            new AgencyPackageRow("H01", "A101", "Original Name", "RESIDING")
        ));

        // Download returns stored bytes without calling residents table
        byte[] downloaded = service.downloadPackage(100L, 2L);
        assertArrayEquals(originalBytes, downloaded);
        verify(store, never()).fetchSnapshotResidents(anyList());

        // Preview returns parsed stored rows without calling residents table
        PackagePreviewResponse preview = service.previewPackage(100L, 0, 20, 2L);
        assertEquals("Original Name", preview.content().get(0).fullName());
        verify(store, never()).fetchSnapshotResidents(anyList());
    }

    @Test
    void executeRetentionCleanup_cleansExpiredDataAndLogsAudit() {
        when(store.findPackageIdsForContentDeletion(any(Instant.class), eq(100))).thenReturn(List.of(101L, 102L));
        when(store.deletePackageContents(List.of(101L, 102L))).thenReturn(2);

        when(store.findPackageIdsForMetadataDeletion(any(Instant.class), eq(100))).thenReturn(List.of(50L));
        when(store.deletePackagesMetadata(List.of(50L))).thenReturn(1);

        when(store.deleteExpiredAudits(any(Instant.class), eq(AgencyDataPackageService.S12_AUDIT_ACTIONS), eq(1000)))
            .thenReturn(5);

        service.executeRetentionCleanup();

        verify(store).deletePackageContents(eq(List.of(101L, 102L)));
        verify(store).markContentsDeleted(eq(List.of(101L, 102L)), eq(baseTime));
        verify(store).deletePackagesMetadata(eq(List.of(50L)));
        verify(store).deleteExpiredAudits(any(Instant.class), eq(AgencyDataPackageService.S12_AUDIT_ACTIONS), eq(1000));
        verify(auditService).recordPackageRetentionCleanup(any(), eq(baseTime), eq(2L), eq(1L), eq(5L));
    }

    @Test
    void executeRetentionCleanup_noDataCleaned_doesNotLogAudit() {
        when(store.findPackageIdsForContentDeletion(any(Instant.class), eq(100))).thenReturn(Collections.emptyList());
        when(store.findPackageIdsForMetadataDeletion(any(Instant.class), eq(100))).thenReturn(Collections.emptyList());
        when(store.deleteExpiredAudits(any(Instant.class), anyList(), eq(1000))).thenReturn(0);

        service.executeRetentionCleanup();

        verify(auditService, never()).recordPackageRetentionCleanup(any(), any(), anyLong(), anyLong(), anyLong());
    }
}
