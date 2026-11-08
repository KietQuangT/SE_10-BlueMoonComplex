package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;

@SpringBootTest
class ActiveHouseholdLockIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired HouseholdLookup households;
    @Autowired org.springframework.transaction.PlatformTransactionManager manager;
    String marker;
    long activeId;
    long inactiveId;

    @BeforeEach
    void setUp() {
        marker = "s08-" + UUID.randomUUID();
        insert(marker + "a", "ACTIVE");
        insert(marker + "i", "INACTIVE");
        activeId = id(marker + "a");
        inactiveId = id(marker + "i");
    }

    @AfterEach
    void cleanUp() {
        jdbc.update("DELETE FROM households WHERE household_code IN (?, ?, ?)", marker + "a", marker + "i", marker + "n");
    }

    @Test
    void activeInsertWaitsForSnapshotTransaction() throws Exception {
        assertWaits(() -> insert(marker + "n", "ACTIVE"));
    }

    @Test
    void activationWaitsForSnapshotTransaction() throws Exception {
        assertWaits(() -> jdbc.update("UPDATE households SET status='ACTIVE' WHERE id=?", inactiveId));
    }

    @Test
    void areaChangeWaitsForSnapshotTransaction() throws Exception {
        assertWaits(() -> jdbc.update("UPDATE households SET area_m2=55 WHERE id=?", activeId));
    }

    @Test
    void deactivationWaitsForSnapshotTransaction() throws Exception {
        assertWaits(() -> jdbc.update("UPDATE households SET status='INACTIVE' WHERE id=?", activeId));
    }

    private void assertWaits(Runnable mutation) throws Exception {
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch locked = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch mutationStarted = new CountDownLatch(1);
        try {
            Future<?> holder = pool.submit(() -> tx.executeWithoutResult(status -> {
                households.lockActiveForBilling();
                locked.countDown();
                try {
                    if (!release.await(10, TimeUnit.SECONDS)) throw new AssertionError("release timed out");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(ex);
                }
            }));
            assertTrue(locked.await(10, TimeUnit.SECONDS));
            Future<?> writer = pool.submit(() -> tx.executeWithoutResult(status -> {
                mutationStarted.countDown();
                mutation.run();
            }));
            assertTrue(mutationStarted.await(10, TimeUnit.SECONDS));
            Thread.sleep(300);
            assertFalse(writer.isDone(), "mutation passed the active range lock");
            release.countDown();
            holder.get(10, TimeUnit.SECONDS);
            writer.get(10, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    private void insert(String code, String status) {
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, ?, NOW(6), NOW(6))", code, code, status);
    }

    private long id(String code) {
        return jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, code);
    }
}
