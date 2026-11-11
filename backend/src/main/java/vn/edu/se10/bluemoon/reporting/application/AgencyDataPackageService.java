package vn.edu.se10.bluemoon.reporting.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.ForbiddenException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyDataPackageStore;
import vn.edu.se10.bluemoon.reporting.persistence.AgencyPackageRecord;

@Service
public class AgencyDataPackageService {
    public static final Pattern RECIPIENT_AGENCY_CODE_PATTERN = Pattern.compile("^[A-Z][A-Z0-9_-]{1,49}$");
    public static final Pattern REFERENCE_PATTERN = Pattern.compile("^[A-Za-z0-9._/-]{1,100}$");

    public static final List<String> S12_AUDIT_ACTIONS = List.of(
        "AGENCY_PACKAGE_CREATED",
        "AGENCY_PACKAGE_APPROVED",
        "AGENCY_PACKAGE_REJECTED",
        "AGENCY_PACKAGE_DOWNLOAD_AUTHORIZED",
        "AGENCY_PACKAGE_HANDED_OFF",
        "AGENCY_PACKAGE_REVOKED"
    );

    private final AgencyDataPackageStore store;
    private final AgencyDataPackageWriter writer;
    private final AuditService auditService;
    private final Clock clock;
    private final TransactionTemplate transactionTemplate;

    public AgencyDataPackageService(
        AgencyDataPackageStore store,
        AgencyDataPackageWriter writer,
        AuditService auditService,
        Clock clock
    ) {
        this(store, writer, auditService, clock, null);
    }

    @Autowired
    public AgencyDataPackageService(
        AgencyDataPackageStore store,
        AgencyDataPackageWriter writer,
        AuditService auditService,
        Clock clock,
        PlatformTransactionManager transactionManager
    ) {
        this.store = store;
        this.writer = writer;
        this.auditService = auditService;
        this.clock = clock;
        if (transactionManager != null) {
            this.transactionTemplate = new TransactionTemplate(transactionManager);
            this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        } else {
            this.transactionTemplate = null;
        }
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public PackageDetail createPackage(CreatePackageRequest request, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        if (request == null) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        validateRequestStrings(request);

        List<Long> householdIds = request.householdIds();
        validateHouseholdScope(householdIds);

        List<Long> validActiveIds = store.validateActiveHouseholdIds(householdIds);
        if (validActiveIds.size() != householdIds.size()) {
            throw new BadRequestException("PACKAGE_SCOPE_INVALID");
        }

        List<AgencyPackageRow> residents = store.fetchSnapshotResidents(householdIds);
        if (residents.size() > 50000) {
            throw new BadRequestException("PACKAGE_TOO_LARGE");
        }

        long householdCount = householdIds.size();
        long residentCount = residents.size();
        long residingCount = residents.stream().filter(r -> "RESIDING".equals(r.residencyStatus())).count();
        long temporarilyAbsentCount = residents.stream().filter(r -> "TEMPORARILY_ABSENT".equals(r.residencyStatus())).count();

        Instant now = clock.instant();
        byte[] fileBytes = writer.writePackage(now, residentCount, residingCount, temporarilyAbsentCount, residents);
        if (fileBytes.length > 10 * 1024 * 1024) {
            throw new BadRequestException("PACKAGE_TOO_LARGE");
        }

        String sha256 = AgencyDataPackageWriter.computeSha256(fileBytes);
        Instant contentDeleteAfter = now.plus(30, ChronoUnit.DAYS);

        AgencyPackageRecord pkg = new AgencyPackageRecord(
            null,
            "DRAFT",
            1L,
            AgencyDataPackageWriter.TEMPLATE_ID,
            AgencyDataPackageWriter.TEMPLATE_VERSION,
            request.recipientAgencyCode(),
            request.requestReference(),
            request.licenceReference(),
            actorId,
            now,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            contentDeleteAfter,
            null,
            sha256,
            householdCount,
            residentCount,
            residingCount,
            temporarilyAbsentCount
        );

        long packageId = store.insertPackage(pkg);
        store.insertPackageHouseholds(packageId, householdIds);
        store.insertPackageContent(packageId, fileBytes, fileBytes.length, sha256);

        Map<String, String> auditChanges = Map.of(
            "version", "1",
            "templateId", AgencyDataPackageWriter.TEMPLATE_ID,
            "householdCount", String.valueOf(householdCount),
            "residentCount", String.valueOf(residentCount),
            "recipientAgencyCode", request.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_CREATED", "AgencyDataPackage", String.valueOf(packageId), auditChanges);

        AgencyPackageRecord createdRecord = store.findPackageById(packageId).orElseThrow();
        return toDetail(createdRecord, householdIds, actorId, now);
    }

    @Transactional(readOnly = true)
    public PackageDetail getPackageDetail(Long id, Long actorId) {
        Instant now = clock.instant();
        AgencyPackageRecord pkg = findPackageWithExpiryCheck(id, now);
        List<Long> householdIds = store.findHouseholdIdsByPackageId(id);
        return toDetail(pkg, householdIds, actorId, now);
    }

    @Transactional(readOnly = true)
    public PackageSummaryPage listPackages(String status, int page, int size, Long actorId) {
        Instant now = clock.instant();
        long total = store.countPackages(status, now);
        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / size);

        List<PackageSummary> summaries;
        long offset = (long) page * size;
        if (offset < 0 || offset >= total) {
            summaries = Collections.emptyList();
        } else {
            summaries = store.findPackages(status, now, (int) offset, size).stream()
                .map(r -> toSummary(r, actorId, now))
                .toList();
        }
        return new PackageSummaryPage(summaries, page, size, total, totalPages);
    }

    @Transactional(readOnly = true)
    public PackagePreviewResponse previewPackage(Long id, int page, int size, Long actorId) {
        Instant now = clock.instant();
        AgencyPackageRecord pkg = findPackageWithExpiryCheck(id, now);

        if ("REJECTED".equals(pkg.status()) || "REVOKED".equals(pkg.status())) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter()) || pkg.contentDeletedAt() != null) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }
        if ("APPROVED".equals(pkg.status()) && pkg.downloadUntil() != null && (now.isAfter(pkg.downloadUntil()) || now.equals(pkg.downloadUntil()))) {
            throw new ConflictException("PACKAGE_DOWNLOAD_EXPIRED");
        }

        byte[] bytes = store.findPackageContentBytes(id)
            .orElseThrow(() -> new ConflictException("PACKAGE_CONTENT_UNAVAILABLE"));

        String actualSha = AgencyDataPackageWriter.computeSha256(bytes);
        if (!actualSha.equals(pkg.contentSha256())) {
            throw new IllegalStateException("Nội dung gói bị sai lệch mã hash SHA-256");
        }

        List<AgencyPackageRow> rows = writer.parseWorkbookRows(bytes);
        long totalElements = rows.size();
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        long fromIndex = (long) page * size;
        List<AgencyPackageRow> content;
        if (fromIndex < 0 || fromIndex >= totalElements) {
            content = Collections.emptyList();
        } else {
            int start = (int) fromIndex;
            int end = (int) Math.min(fromIndex + size, totalElements);
            content = rows.subList(start, end);
        }

        return new PackagePreviewResponse(
            id,
            pkg.version(),
            pkg.contentSha256(),
            content,
            page,
            size,
            totalElements,
            totalPages
        );
    }

    @Transactional
    public PackageDetail approvePackage(Long id, PackageActionRequest request, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        AgencyPackageRecord pkg = store.findPackageForUpdate(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        Instant now = clock.instant();

        checkMetadataExpiry(pkg, now);

        if (actorId.equals(pkg.createdBy())) {
            throw new ForbiddenException("PACKAGE_SELF_DECISION_NOT_ALLOWED");
        }
        if (!"DRAFT".equals(pkg.status())) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (!pkg.version().equals(request.expectedVersion()) || !pkg.contentSha256().equals(request.contentSha256())) {
            throw new ConflictException("PACKAGE_VERSION_CONFLICT");
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter()) || pkg.contentDeletedAt() != null) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }
        if (store.findPackageContentBytes(id).isEmpty()) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }

        Instant sevenDays = now.plus(7, ChronoUnit.DAYS);
        Instant downloadUntil = sevenDays.isBefore(pkg.contentDeleteAfter()) ? sevenDays : pkg.contentDeleteAfter();
        long newVersion = pkg.version() + 1;

        store.updatePackageApprove(id, actorId, now, downloadUntil, newVersion);

        Map<String, String> auditChanges = Map.of(
            "version", String.valueOf(newVersion),
            "contentSha256", pkg.contentSha256(),
            "status", "APPROVED",
            "recipientAgencyCode", pkg.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_APPROVED", "AgencyDataPackage", String.valueOf(id), auditChanges);

        AgencyPackageRecord updated = store.findPackageById(id).orElseThrow();
        List<Long> householdIds = store.findHouseholdIdsByPackageId(id);
        return toDetail(updated, householdIds, actorId, now);
    }

    @Transactional
    public PackageDetail rejectPackage(Long id, PackageActionRequest request, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        AgencyPackageRecord pkg = store.findPackageForUpdate(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        Instant now = clock.instant();

        checkMetadataExpiry(pkg, now);

        if (!"DRAFT".equals(pkg.status())) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter()) || pkg.contentDeletedAt() != null) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }
        if (!pkg.version().equals(request.expectedVersion()) || !pkg.contentSha256().equals(request.contentSha256())) {
            throw new ConflictException("PACKAGE_VERSION_CONFLICT");
        }

        long newVersion = pkg.version() + 1;
        store.updatePackageReject(id, actorId, now, newVersion);

        Map<String, String> auditChanges = Map.of(
            "version", String.valueOf(newVersion),
            "contentSha256", pkg.contentSha256(),
            "status", "REJECTED",
            "recipientAgencyCode", pkg.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_REJECTED", "AgencyDataPackage", String.valueOf(id), auditChanges);

        AgencyPackageRecord updated = store.findPackageById(id).orElseThrow();
        List<Long> householdIds = store.findHouseholdIdsByPackageId(id);
        return toDetail(updated, householdIds, actorId, now);
    }

    @Transactional
    public PackageDetail revokePackage(Long id, PackageActionRequest request, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        AgencyPackageRecord pkg = store.findPackageForUpdate(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        Instant now = clock.instant();

        checkMetadataExpiry(pkg, now);

        if (!"APPROVED".equals(pkg.status()) || pkg.revokedAt() != null) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (!pkg.version().equals(request.expectedVersion()) || !pkg.contentSha256().equals(request.contentSha256())) {
            throw new ConflictException("PACKAGE_VERSION_CONFLICT");
        }

        long newVersion = pkg.version() + 1;
        store.updatePackageRevoke(id, actorId, now, newVersion);

        Map<String, String> auditChanges = Map.of(
            "status", "REVOKED",
            "version", String.valueOf(newVersion),
            "contentSha256", pkg.contentSha256(),
            "recipientAgencyCode", pkg.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_REVOKED", "AgencyDataPackage", String.valueOf(id), auditChanges);

        AgencyPackageRecord updated = store.findPackageById(id).orElseThrow();
        List<Long> householdIds = store.findHouseholdIdsByPackageId(id);
        return toDetail(updated, householdIds, actorId, now);
    }

    @Transactional
    public byte[] downloadPackage(Long id, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        AgencyPackageRecord pkg = store.findPackageForUpdate(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        Instant now = clock.instant();

        checkMetadataExpiry(pkg, now);

        if (!"APPROVED".equals(pkg.status()) || pkg.revokedAt() != null) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (pkg.downloadUntil() != null && (now.isAfter(pkg.downloadUntil()) || now.equals(pkg.downloadUntil()))) {
            throw new ConflictException("PACKAGE_DOWNLOAD_EXPIRED");
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter()) || pkg.contentDeletedAt() != null) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }

        byte[] bytes = store.findPackageContentBytes(id)
            .orElseThrow(() -> new ConflictException("PACKAGE_CONTENT_UNAVAILABLE"));

        String actualSha = AgencyDataPackageWriter.computeSha256(bytes);
        if (!actualSha.equals(pkg.contentSha256())) {
            throw new IllegalStateException("Nội dung gói bị sai lệch mã hash SHA-256");
        }

        Map<String, String> auditChanges = Map.of(
            "contentVersion", AgencyDataPackageWriter.CONTENT_VERSION,
            "contentSha256", pkg.contentSha256(),
            "templateId", pkg.templateId(),
            "householdCount", String.valueOf(pkg.householdCount()),
            "residentCount", String.valueOf(pkg.residentCount()),
            "recipientAgencyCode", pkg.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_DOWNLOAD_AUTHORIZED", "AgencyDataPackage", String.valueOf(id), auditChanges);

        return bytes;
    }

    @Transactional
    public PackageDetail handoffPackage(Long id, PackageActionRequest request, Long actorId) {
        if (actorId == null) {
            throw new ForbiddenException("Chưa xác thực");
        }
        AgencyPackageRecord pkg = store.findPackageForUpdate(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        Instant now = clock.instant();

        checkMetadataExpiry(pkg, now);

        if (!"APPROVED".equals(pkg.status()) || pkg.revokedAt() != null) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (pkg.handedOffAt() != null) {
            throw new ConflictException("PACKAGE_STATE_CONFLICT");
        }
        if (pkg.downloadUntil() != null && (now.isAfter(pkg.downloadUntil()) || now.equals(pkg.downloadUntil()))) {
            throw new ConflictException("PACKAGE_DOWNLOAD_EXPIRED");
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter()) || pkg.contentDeletedAt() != null) {
            throw new ConflictException("PACKAGE_CONTENT_UNAVAILABLE");
        }
        if (!pkg.version().equals(request.expectedVersion()) || !pkg.contentSha256().equals(request.contentSha256())) {
            throw new ConflictException("PACKAGE_VERSION_CONFLICT");
        }

        long newVersion = pkg.version() + 1;
        store.updatePackageHandoff(id, actorId, now, newVersion);

        Map<String, String> auditChanges = Map.of(
            "status", "APPROVED",
            "version", String.valueOf(newVersion),
            "contentSha256", pkg.contentSha256(),
            "recipientAgencyCode", pkg.recipientAgencyCode()
        );
        auditService.record(actorId, "AGENCY_PACKAGE_HANDED_OFF", "AgencyDataPackage", String.valueOf(id), auditChanges);

        AgencyPackageRecord updated = store.findPackageById(id).orElseThrow();
        List<Long> householdIds = store.findHouseholdIdsByPackageId(id);
        return toDetail(updated, householdIds, actorId, now);
    }

    public void executeRetentionCleanup() {
        while (true) {
            boolean hasMore;
            if (transactionTemplate != null) {
                Boolean res = transactionTemplate.execute(status -> executeCleanupBatch());
                hasMore = res != null && res;
            } else {
                hasMore = executeCleanupBatch();
            }
            if (!hasMore) {
                break;
            }
        }
    }

    public boolean executeCleanupBatch() {
        Instant now = clock.instant();

        List<Long> contentBatch = store.findPackageIdsForContentDeletion(now, 100);
        int contentCount = 0;
        if (!contentBatch.isEmpty()) {
            contentCount = store.deletePackageContents(contentBatch);
            store.markContentsDeleted(contentBatch, now);
        }

        List<Long> metaBatch = store.findPackageIdsForMetadataDeletion(now, 100);
        int metadataCount = 0;
        if (!metaBatch.isEmpty()) {
            metadataCount = store.deletePackagesMetadata(metaBatch);
        }

        int auditCount = store.deleteExpiredAudits(now, S12_AUDIT_ACTIONS, 1000);

        if (contentCount > 0 || metadataCount > 0 || auditCount > 0) {
            auditService.recordPackageRetentionCleanup(UUID.randomUUID(), now, contentCount, metadataCount, auditCount);
        }

        return contentBatch.size() == 100 || metaBatch.size() == 100 || auditCount == 1000;
    }

    private AgencyPackageRecord findPackageWithExpiryCheck(Long id, Instant now) {
        AgencyPackageRecord pkg = store.findPackageById(id)
            .orElseThrow(() -> new NotFoundException("PACKAGE_NOT_FOUND"));
        checkMetadataExpiry(pkg, now);
        return pkg;
    }

    private void checkMetadataExpiry(AgencyPackageRecord pkg, Instant now) {
        Instant metadataExpiry = pkg.createdAt().atZone(ZoneOffset.UTC).plusYears(5).toInstant();
        if (now.isAfter(metadataExpiry) || now.equals(metadataExpiry)) {
            throw new NotFoundException("PACKAGE_NOT_FOUND");
        }
    }

    private void validateRequestStrings(CreatePackageRequest request) {
        if (request.recipientAgencyCode() == null || !RECIPIENT_AGENCY_CODE_PATTERN.matcher(request.recipientAgencyCode()).matches()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        if (request.requestReference() == null || !REFERENCE_PATTERN.matcher(request.requestReference()).matches()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
        if (request.licenceReference() == null || !REFERENCE_PATTERN.matcher(request.licenceReference()).matches()) {
            throw new BadRequestException("INVALID_PACKAGE_REQUEST");
        }
    }

    private void validateHouseholdScope(List<Long> householdIds) {
        if (householdIds == null || householdIds.isEmpty() || householdIds.size() > 1000) {
            throw new BadRequestException("PACKAGE_SCOPE_INVALID");
        }
        Set<Long> set = new HashSet<>();
        for (Long id : householdIds) {
            if (id == null || id <= 0) {
                throw new BadRequestException("PACKAGE_SCOPE_INVALID");
            }
            if (!set.add(id)) {
                throw new BadRequestException("PACKAGE_SCOPE_INVALID");
            }
        }
    }

    public static boolean canApprove(AgencyPackageRecord pkg, Long actorId, Instant now) {
        if (!"DRAFT".equals(pkg.status())) {
            return false;
        }
        if (actorId == null || actorId.equals(pkg.createdBy())) {
            return false;
        }
        if (now.isAfter(pkg.contentDeleteAfter()) || now.equals(pkg.contentDeleteAfter())) {
            return false;
        }
        return pkg.contentDeletedAt() == null;
    }

    public static boolean canReject(AgencyPackageRecord pkg) {
        return "DRAFT".equals(pkg.status()) && pkg.contentDeletedAt() == null;
    }

    public static boolean canReject(AgencyPackageRecord pkg, Instant now) {
        if (!"DRAFT".equals(pkg.status())) {
            return false;
        }
        if (pkg.contentDeletedAt() != null) {
            return false;
        }
        return now.isBefore(pkg.contentDeleteAfter());
    }

    public static boolean canDownload(AgencyPackageRecord pkg, Instant now) {
        if (!"APPROVED".equals(pkg.status())) {
            return false;
        }
        if (pkg.revokedAt() != null || pkg.contentDeletedAt() != null) {
            return false;
        }
        if (pkg.downloadUntil() == null) {
            return false;
        }
        if (now.isAfter(pkg.downloadUntil()) || now.equals(pkg.downloadUntil())) {
            return false;
        }
        return now.isBefore(pkg.contentDeleteAfter());
    }

    public static boolean canRevoke(AgencyPackageRecord pkg) {
        return "APPROVED".equals(pkg.status()) && pkg.revokedAt() == null;
    }

    public static boolean canHandoff(AgencyPackageRecord pkg, Instant now) {
        if (!"APPROVED".equals(pkg.status())) {
            return false;
        }
        if (pkg.revokedAt() != null || pkg.handedOffAt() != null || pkg.contentDeletedAt() != null) {
            return false;
        }
        if (pkg.downloadUntil() == null) {
            return false;
        }
        if (now.isAfter(pkg.downloadUntil()) || now.equals(pkg.downloadUntil())) {
            return false;
        }
        return now.isBefore(pkg.contentDeleteAfter());
    }

    public static PackageSummary toSummary(AgencyPackageRecord pkg, Long actorId, Instant now) {
        return new PackageSummary(
            pkg.id(),
            pkg.status(),
            pkg.version(),
            pkg.templateId(),
            pkg.templateVersion(),
            pkg.createdAt(),
            pkg.createdBy(),
            pkg.approvedAt(),
            pkg.approvedBy(),
            pkg.downloadUntil(),
            pkg.contentDeleteAfter(),
            pkg.contentDeletedAt(),
            pkg.householdCount(),
            pkg.residentCount(),
            pkg.handedOffAt(),
            pkg.revokedAt(),
            canApprove(pkg, actorId, now),
            canReject(pkg, now),
            canDownload(pkg, now),
            canRevoke(pkg),
            canHandoff(pkg, now)
        );
    }

    public static PackageDetail toDetail(AgencyPackageRecord pkg, List<Long> householdIds, Long actorId, Instant now) {
        return new PackageDetail(
            pkg.id(),
            pkg.status(),
            pkg.version(),
            pkg.templateId(),
            pkg.templateVersion(),
            pkg.createdAt(),
            pkg.createdBy(),
            pkg.approvedAt(),
            pkg.approvedBy(),
            pkg.downloadUntil(),
            pkg.contentDeleteAfter(),
            pkg.contentDeletedAt(),
            pkg.householdCount(),
            pkg.residentCount(),
            pkg.handedOffAt(),
            pkg.revokedAt(),
            canApprove(pkg, actorId, now),
            canReject(pkg, now),
            canDownload(pkg, now),
            canRevoke(pkg),
            canHandoff(pkg, now),
            householdIds,
            pkg.recipientAgencyCode(),
            pkg.requestReference(),
            pkg.licenceReference(),
            pkg.contentSha256(),
            pkg.rejectedBy(),
            pkg.rejectedAt(),
            pkg.revokedBy(),
            pkg.handedOffBy()
        );
    }
}
