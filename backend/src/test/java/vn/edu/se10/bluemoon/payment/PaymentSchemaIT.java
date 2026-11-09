package vn.edu.se10.bluemoon.payment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PaymentSchemaIT {
    @Autowired JdbcTemplate jdbc;

    @Test void commandKeysAreUniqueAcrossAllPaymentOperations() {
        String key = "00000000-0000-0000-0000-000000000001";
        jdbc.update("INSERT INTO payment_commands (idempotency_key, fingerprint, result_json) VALUES (?, ?, ?)", key, "a".repeat(64), "{}");
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO payment_commands (idempotency_key, fingerprint, result_json) VALUES (?, ?, ?)", key, "b".repeat(64), "{}"));
    }
}
