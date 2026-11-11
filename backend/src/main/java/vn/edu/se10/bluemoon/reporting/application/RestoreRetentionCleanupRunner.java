package vn.edu.se10.bluemoon.reporting.application;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runner headless thuc hien retention cleanup S12 tren schema restore thu nghiem.
 * Tu choi moi ket noi toi DB dev goc hoac DB khong dung tien to bluemoon_restore_.
 */
@Component
@Profile("restore-cleanup")
public class RestoreRetentionCleanupRunner implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(RestoreRetentionCleanupRunner.class);
    public static final String REQUIRED_PREFIX = "bluemoon_restore_";
    public static final String FORBIDDEN_DEFAULT_DB = "bluemoon";

    private final AgencyDataPackageService packageService;
    private final DataSource dataSource;
    private final String dbName;

    public RestoreRetentionCleanupRunner(
            AgencyDataPackageService packageService,
            DataSource dataSource,
            @Value("${DB_NAME:}") String dbName) {
        this.packageService = packageService;
        this.dataSource = dataSource;
        this.dbName = dbName;
    }

    @Override
    public void run(String... args) {
        validateTargetDatabase();
        log.info("Bat dau cleanup S12 tren schema restore target: {}", dbName);
        packageService.executeRetentionCleanup();
        log.info("Hoan tat cleanup S12 tren schema restore target: {}", dbName);
    }

    void validateTargetDatabase() {
        if (dbName == null || dbName.isBlank()) {
            throw new IllegalStateException("DB_NAME target restore bat buoc phai duoc chi dinh ro rang.");
        }
        String trimmed = dbName.trim();
        if (FORBIDDEN_DEFAULT_DB.equalsIgnoreCase(trimmed)) {
            throw new IllegalStateException("Cung cap DB dev goc 'bluemoon' la bi cam trong profile restore-cleanup.");
        }
        if (!trimmed.startsWith(REQUIRED_PREFIX)) {
            throw new IllegalStateException(
                    "DB_NAME target phai co tien to hop le bat dau bang '" + REQUIRED_PREFIX + "'. Thuc te: " + dbName);
        }

        if (dataSource == null) {
            throw new IllegalStateException("DataSource bat buoc phai ton tai de xac minh ket noi.");
        }

        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            if (catalog == null || catalog.isBlank()) {
                throw new IllegalStateException("Khong the xac dinh catalog tu ket noi thuc te.");
            }
            String trimmedCatalog = catalog.trim();
            if (FORBIDDEN_DEFAULT_DB.equalsIgnoreCase(trimmedCatalog)) {
                throw new IllegalStateException("Ket noi thuc te toi catalog dev goc 'bluemoon' bi cam.");
            }
            if (!trimmedCatalog.startsWith(REQUIRED_PREFIX)) {
                throw new IllegalStateException(
                        "Catalog ket noi thuc te '" + catalog + "' khong co tien to hop le '" + REQUIRED_PREFIX + "'.");
            }
            if (!trimmedCatalog.equalsIgnoreCase(trimmed)) {
                throw new IllegalStateException(
                        "Catalog ket noi thuc te '" + catalog + "' khong khop voi DB_NAME target '" + trimmed + "'.");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Khong the xac minh catalog tu ket noi target: " + e.getMessage(), e);
        }
    }
}
