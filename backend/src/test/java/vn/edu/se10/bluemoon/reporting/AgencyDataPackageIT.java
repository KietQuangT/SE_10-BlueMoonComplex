package vn.edu.se10.bluemoon.reporting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.ForbiddenException;
import vn.edu.se10.bluemoon.reporting.application.AgencyDataPackageService;
import vn.edu.se10.bluemoon.reporting.application.CreatePackageRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageActionRequest;
import vn.edu.se10.bluemoon.reporting.application.PackageDetail;
import vn.edu.se10.bluemoon.reporting.application.PackagePreviewResponse;

@SpringBootTest
@Transactional
class AgencyDataPackageIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired AgencyDataPackageService packageService;

    @Test
    void ac1_scopeIsolationAndFieldAllowlist() throws IOException {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        long adminId = createAdminUser("adm_" + marker);

        long householdA = createHousehold("H_A_" + marker, "APT_A_" + marker, "ACTIVE");
        long householdB = createHousehold("H_B_" + marker, "APT_B_" + marker, "ACTIVE");

        createResident(householdA, "Nguyen Van A " + marker, "RESIDING");
        createResident(householdB, "SECRET_MARKER_B " + marker, "RESIDING");

        CreatePackageRequest req = new CreatePackageRequest(
            List.of(householdA),
            "CA_Q1",
            "REQ_" + marker,
            "LIC_" + marker
        );
        PackageDetail detail = packageService.createPackage(req, adminId);
        assertNotNull(detail);
        assertEquals(1, detail.householdCount());
        assertEquals(1, detail.residentCount());

        byte[] bytes = packageService.downloadPackage(detail.id(), adminId);
        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            XSSFSheet sheet = wb.getSheet("CuTru");
            assertNotNull(sheet);

            boolean foundA = false;
            boolean foundB = false;
            for (int r = 0; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                for (int c = 0; c < row.getLastCellNum(); c++) {
                    String val = row.getCell(c) != null ? row.getCell(c).getStringCellValue() : "";
                    if (val.contains("Nguyen Van A " + marker)) foundA = true;
                    if (val.contains("SECRET_MARKER_B " + marker)) foundB = true;
                }
            }
            assertTrue(foundA, "Phải tìm thấy cư dân hộ A trong file snapshot");
            assertFalse(foundB, "Tuyệt đối không được lộ cư dân hộ B ngoài phạm vi");
        }
    }

    @Test
    void ac1_inactiveOrUnknownHousehold_rejected() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        long adminId = createAdminUser("adm2_" + marker);
        long inactiveH = createHousehold("H_INACT_" + marker, "APT_I_" + marker, "INACTIVE");

        CreatePackageRequest reqInactive = new CreatePackageRequest(
            List.of(inactiveH),
            "CA_Q1",
            "REQ_INACT",
            "LIC_INACT"
        );
        assertThrows(BadRequestException.class, () -> packageService.createPackage(reqInactive, adminId));

        CreatePackageRequest reqUnknown = new CreatePackageRequest(
            List.of(999999999L),
            "CA_Q1",
            "REQ_UNK",
            "LIC_UNK"
        );
        assertThrows(BadRequestException.class, () -> packageService.createPackage(reqUnknown, adminId));
    }

    @Test
    void ac2_separationOfDutiesAndCreatorReject() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        long creatorId = createAdminUser("creator_" + marker);
        long otherAdminId = createAdminUser("other_" + marker);
        long household = createHousehold("H_SEP_" + marker, "APT_S_" + marker, "ACTIVE");
        createResident(household, "Cư Dân S " + marker, "RESIDING");

        CreatePackageRequest req = new CreatePackageRequest(
            List.of(household),
            "CA_Q1",
            "REQ_SEP",
            "LIC_SEP"
        );
        PackageDetail draft = packageService.createPackage(req, creatorId);
        assertEquals("DRAFT", draft.status());

        // Creator attempts approve -> 403
        PackageActionRequest actionReq = new PackageActionRequest(draft.version(), draft.contentSha256());
        assertThrows(ForbiddenException.class, () -> packageService.approvePackage(draft.id(), actionReq, creatorId));

        // Creator rejects own package -> 200 REJECTED
        PackageDetail rejected = packageService.rejectPackage(draft.id(), actionReq, creatorId);
        assertEquals("REJECTED", rejected.status());
        assertEquals(creatorId, rejected.rejectedBy());

        // Another package approved by second admin
        PackageDetail draft2 = packageService.createPackage(req, creatorId);
        PackageActionRequest actionReq2 = new PackageActionRequest(draft2.version(), draft2.contentSha256());
        PackageDetail approved = packageService.approvePackage(draft2.id(), actionReq2, otherAdminId);
        assertEquals("APPROVED", approved.status());
        assertEquals(otherAdminId, approved.approvedBy());
    }

    @Test
    void ac3_snapshotImmutabilityAfterCreation() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        long creatorId = createAdminUser("snap_" + marker);
        long household = createHousehold("H_SNAP_" + marker, "APT_SNAP_" + marker, "ACTIVE");
        long resId = createResident(household, "Tên Ban Đầu " + marker, "RESIDING");

        CreatePackageRequest req = new CreatePackageRequest(
            List.of(household),
            "CA_Q1",
            "REQ_SNAP",
            "LIC_SNAP"
        );
        PackageDetail created = packageService.createPackage(req, creatorId);

        // Mutate live resident data
        jdbc.update("UPDATE residents SET full_name = ?, residency_status = 'MOVED_OUT' WHERE id = ?",
            "Tên Sau Sửa " + marker, resId);

        // Preview reads frozen snapshot
        PackagePreviewResponse preview = packageService.previewPackage(created.id(), 0, 20, creatorId);
        assertEquals(1, preview.content().size());
        assertEquals("Tên Ban Đầu " + marker, preview.content().get(0).fullName());
        assertEquals("RESIDING", preview.content().get(0).residencyStatus());
    }

    @Test
    void ac4_lifecycleRevokeAndDownloadGuard() {
        String marker = UUID.randomUUID().toString().substring(0, 8);
        long creatorId = createAdminUser("life1_" + marker);
        long otherAdminId = createAdminUser("life2_" + marker);
        long household = createHousehold("H_LIFE_" + marker, "APT_L_" + marker, "ACTIVE");
        createResident(household, "Cư Dân L " + marker, "RESIDING");

        CreatePackageRequest req = new CreatePackageRequest(
            List.of(household),
            "CA_Q1",
            "REQ_LIFE",
            "LIC_LIFE"
        );
        PackageDetail draft = packageService.createPackage(req, creatorId);
        PackageActionRequest actionReq = new PackageActionRequest(draft.version(), draft.contentSha256());
        PackageDetail approved = packageService.approvePackage(draft.id(), actionReq, otherAdminId);

        // Download succeeds when approved
        byte[] bytes = packageService.downloadPackage(approved.id(), creatorId);
        assertNotNull(bytes);

        // Revoke package
        PackageActionRequest revokeReq = new PackageActionRequest(approved.version(), approved.contentSha256());
        PackageDetail revoked = packageService.revokePackage(approved.id(), revokeReq, otherAdminId);
        assertEquals("REVOKED", revoked.status());

        // Download blocked after revoke
        assertThrows(ConflictException.class, () -> packageService.downloadPackage(approved.id(), creatorId));
    }

    private long createAdminUser(String username) {
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) " +
            "VALUES ((SELECT id FROM roles WHERE code='ADMIN'), ?, 'pwd_hash', 'Test Admin', 'ACTIVE', NOW(6), NOW(6))", username);
        return jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, username);
    }

    private long createHousehold(String householdCode, String apartmentCode, String status) {
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) " +
            "VALUES (?, ?, 60.5, ?, NOW(6), NOW(6))", householdCode, apartmentCode, status);
        return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, householdCode);
    }

    private long createResident(long householdId, String fullName, String status) {
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) " +
            "VALUES (?, ?, ?, NOW(6), NOW(6))", householdId, fullName, status);
        return jdbc.queryForObject("SELECT id FROM residents WHERE household_id=? AND full_name=?", Long.class, householdId, fullName);
    }
}
