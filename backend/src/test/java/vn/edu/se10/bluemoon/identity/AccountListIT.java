package vn.edu.se10.bluemoon.identity;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountListIT extends AccountIntegrationBase {
    @Test void listsPersistedAccountsAndCurrentLockState() throws Exception {
        create(body(prefix + "-acct", prefix + "@example.test", "ACCOUNTANT", "null")).andExpect(status().isCreated());
        jdbc.update("UPDATE users SET locked_until=DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 10 MINUTE) WHERE username=?", prefix + "-acct");
        var response = mvc.perform(get("/api/v1/accounts?page=0&size=100").cookie(cookie))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        var content = JsonMapper.builder().build().readTree(response).get("content");
        var found = false;
        for (var account : content) {
            if ((prefix + "-acct").equals(account.get("username").asString())) {
                found = true;
                assertThat(account.get("role").asString()).isEqualTo("ACCOUNTANT");
                assertThat(account.get("locked").asBoolean()).isTrue();
                assertThat(account.has("passwordHash")).isFalse();
            }
        }
        assertThat(found).isTrue();
    }
}
