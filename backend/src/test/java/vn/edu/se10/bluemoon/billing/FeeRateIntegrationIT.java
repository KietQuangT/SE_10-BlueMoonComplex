package vn.edu.se10.bluemoon.billing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import vn.edu.se10.bluemoon.identity.application.AuthenticatedUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import vn.edu.se10.bluemoon.billing.api.CreateFeeRateRequest;
import vn.edu.se10.bluemoon.billing.application.FeeCalculationService;
import vn.edu.se10.bluemoon.billing.application.FeeRateService;
import vn.edu.se10.bluemoon.common.error.ConflictException;

@SpringBootTest
@AutoConfigureMockMvc
class FeeRateIntegrationIT {
    @Autowired JdbcTemplate jdbc;
    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;
    @Autowired FeeRateService service;
    @Autowired FeeCalculationService calculation;
    @Autowired TransactionTemplate transactions;
    private long actor;
    private long household;
    private long management;
    private long waste;
    private LocalDate month;
    private String marker;

    @BeforeEach
    void setup() {
        marker = "s07-" + UUID.randomUUID().toString().substring(0, 12);
        jdbc.update("INSERT INTO users (role_id, username, password_hash, full_name, status, created_at, updated_at) VALUES ((SELECT id FROM roles WHERE code='ACCOUNTANT'), ?, ?, 'Test', 'ACTIVE', NOW(6), NOW(6))", marker, encoder.encode("test-only-pass"));
        actor = jdbc.queryForObject("SELECT id FROM users WHERE username=?", Long.class, marker);
        jdbc.update("INSERT INTO households (household_code, apartment_code, area_m2, status, created_at, updated_at) VALUES (?, ?, 50, 'INACTIVE', NOW(6), NOW(6))", marker, marker);
        household = jdbc.queryForObject("SELECT id FROM households WHERE household_code=?", Long.class, marker);
        management = jdbc.queryForObject("SELECT id FROM fee_types WHERE code='MANAGEMENT_FEE'", Long.class);
        waste = jdbc.queryForObject("SELECT id FROM fee_types WHERE code='WASTE_FEE'", Long.class);
        month = LocalDate.of(3000 + ThreadLocalRandom.current().nextInt(5000), ThreadLocalRandom.current().nextInt(1, 13), 1);
    }

    @AfterEach
    void cleanup() {
        jdbc.update("DELETE FROM audit_log WHERE actor_user_id=?", actor);
        jdbc.update("DELETE FROM fee_rates WHERE created_by=?", actor);
        jdbc.update("DELETE FROM households WHERE id=?", household);
        jdbc.update("DELETE FROM users WHERE id=?", actor);
    }

    @Test
    void constraintsAndSeedsAreEnforced() {
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM fee_types", Integer.class));
        service.create(new CreateFeeRateRequest(management, new BigDecimal("10000"), month), actor);
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) VALUES (?, 1, ?, ?, NOW(6))", management, month, actor));
        assertThatThrownBy(() -> jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) VALUES (?, 0, ?, ?, NOW(6))", waste, month, actor))
                .isInstanceOf(org.springframework.dao.DataAccessException.class)
                .hasStackTraceContaining("ck_fee_rates_price");
        assertThatThrownBy(() -> jdbc.update("INSERT INTO fee_rates (fee_type_id, unit_price, effective_from, created_by, created_at) VALUES (?, 1, ?, ?, NOW(6))", waste, month.plusDays(1), actor))
                .isInstanceOf(org.springframework.dao.DataAccessException.class)
                .hasStackTraceContaining("ck_fee_rates_first_day");
    }

    @Test
    void previewAndAuditRollback() {
        service.create(new CreateFeeRateRequest(management, new BigDecimal("10000"), month), actor);
        service.create(new CreateFeeRateRequest(waste, new BigDecimal("30000"), month), actor);
        var result = calculation.preview(household, month.toString().substring(0, 7));
        assertEquals(new BigDecimal("500000"), result.lines().get(0).amount());
        assertEquals(new BigDecimal("30000"), result.lines().get(1).amount());
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='FEE_RATE_CREATED'", Integer.class, actor));
        transactions.executeWithoutResult(status -> {
            service.create(new CreateFeeRateRequest(management, new BigDecimal("1"), month.plusMonths(1)), actor);
            status.setRollbackOnly();
        });
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM fee_rates WHERE created_by=?", Integer.class, actor));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=?", Integer.class, actor));
    }

    @Test
    void concurrentCreatesHaveOneWinner() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        Callable<String> create = () -> {
            var principal = UsernamePasswordAuthenticationToken.authenticated(
                    new AuthenticatedUser(actor, marker, "Test", "ACCOUNTANT", null, false), null,
                    java.util.List.of(new SimpleGrantedAuthority("ROLE_ACCOUNTANT")));
            String body = "{\"feeTypeId\":" + management + ",\"unitPrice\":1,\"effectiveFrom\":\"" + month + "\"}";
            start.await();
            var response = mvc.perform(post("/api/v1/fee-rates").with(authentication(principal)).with(csrf())
                    .contentType("application/json").content(body)).andReturn().getResponse();
            if (response.getStatus() == 201) {
                return "created";
            }
            return response.getStatus() + ":" + tools.jackson.databind.json.JsonMapper.builder().build()
                    .readTree(response.getContentAsString()).path("code").asString();
        };
        try {
            Future<String> first = pool.submit(create);
            Future<String> second = pool.submit(create);
            start.countDown();
            assertEquals(java.util.Set.of("created", "409:FEE_RATE_DUPLICATE_MONTH"), java.util.Set.of(first.get(30, TimeUnit.SECONDS), second.get(30, TimeUnit.SECONDS)));
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM fee_rates WHERE created_by=?", Integer.class, actor));
        } finally {
            pool.shutdownNow();
        }
    }
}
