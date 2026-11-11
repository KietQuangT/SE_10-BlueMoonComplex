package vn.edu.se10.bluemoon.reporting.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RestoreRetentionCleanupRunnerTest {

    @Test
    @DisplayName("Ném ngoại lệ khi DB_NAME rỗng hoặc null")
    void shouldThrowWhenDbNameIsEmptyOrNull() {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);

        RestoreRetentionCleanupRunner runnerNull =
                new RestoreRetentionCleanupRunner(packageService, dataSource, null);
        assertThatThrownBy(runnerNull::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_NAME target restore bat buoc phai duoc chi dinh ro rang.");

        RestoreRetentionCleanupRunner runnerEmpty =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "   ");
        assertThatThrownBy(runnerEmpty::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_NAME target restore bat buoc phai duoc chi dinh ro rang.");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi DB_NAME trỏ tới DB dev bluemoon")
    void shouldThrowWhenDbNameIsBluemoonDev() {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "bluemoon");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cung cap DB dev goc 'bluemoon' la bi cam");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi DB_NAME không có tiền tố hợp lệ bluemoon_restore_")
    void shouldThrowWhenDbNameHasInvalidPrefix() {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "test_restore_db");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DB_NAME target phai co tien to hop le bat dau bang 'bluemoon_restore_'");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi DataSource null")
    void shouldThrowWhenDataSourceIsNull() {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, null, "bluemoon_restore_s13_test");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DataSource bat buoc phai ton tai");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi catalog thực tế từ connection trỏ về bluemoon")
    void shouldThrowWhenActualCatalogIsBluemoon() throws SQLException {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("bluemoon");

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "bluemoon_restore_s13_test");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ket noi thuc te toi catalog dev goc 'bluemoon' bi cam");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi catalog thực tế không có tiền tố bluemoon_restore_")
    void shouldThrowWhenActualCatalogHasInvalidPrefix() throws SQLException {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("production_db");

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "bluemoon_restore_s13_test");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalog ket noi thuc te 'production_db' khong co tien to hop le");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Ném ngoại lệ khi catalog thực tế và DB_NAME không khớp nhau dù cùng có tiền tố hợp lệ")
    void shouldThrowWhenCatalogAndDbNameMismatch() throws SQLException {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("bluemoon_restore_s13_other_target");

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(packageService, dataSource, "bluemoon_restore_s13_test");
        assertThatThrownBy(runner::run)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("khong khop voi DB_NAME target");

        verifyNoInteractions(packageService);
    }

    @Test
    @DisplayName("Chạy cleanup thành công khi target DB hợp lệ và catalog khớp hoàn toàn")
    void shouldExecuteCleanupWhenTargetDbIsValid() throws SQLException {
        AgencyDataPackageService packageService = mock(AgencyDataPackageService.class);
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getCatalog()).thenReturn("bluemoon_restore_s13_20261111_run1");

        RestoreRetentionCleanupRunner runner =
                new RestoreRetentionCleanupRunner(
                        packageService, dataSource, "bluemoon_restore_s13_20261111_run1");

        assertThatCode(runner::run).doesNotThrowAnyException();
        verify(packageService).executeRetentionCleanup();
    }
}
