package vn.edu.se10.bluemoon.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.residency.application.ResidentDonorLookup;

@SpringBootTest
@Transactional
class ResidentDonorLookupIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired ResidentDonorLookup donors;

    @Test
    void movedOutResidentInInactiveHouseholdRemainsEligible() {
        String marker = "s08-" + UUID.randomUUID();
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'INACTIVE', NOW(6), NOW(6))", marker, marker);
        long household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        jdbc.update("INSERT INTO residents (household_id, full_name, residency_status, created_at, updated_at) VALUES (?, 'Test donor', 'MOVED_OUT', NOW(6), NOW(6))", household);
        long resident = jdbc.queryForObject("SELECT id FROM residents WHERE household_id=?", Long.class, household);
        var donor = donors.lockById(resident).orElseThrow();
        assertEquals(household, donor.householdId());
        assertEquals(marker, donor.householdCode());
        assertEquals("Test donor", donor.fullName());
    }
}
