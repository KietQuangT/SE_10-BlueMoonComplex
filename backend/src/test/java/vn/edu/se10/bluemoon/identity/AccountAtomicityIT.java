package vn.edu.se10.bluemoon.identity;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.mail.MockEmailService;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AccountAtomicityIT extends AccountIntegrationBase {
    @MockitoSpyBean AuditService audit;
    @MockitoSpyBean MockEmailService mail;
    @Test void auditFailureRollsBackAccountAndMail() throws Exception {
        doThrow(new IllegalStateException()).when(audit).record(anyLong(), eq("ACCOUNT_CREATED"), anyString(), anyString(), anyMap());
        create(body(prefix+"-failure",prefix+"@example.test","ADMIN","null")).andExpect(status().isInternalServerError()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(prefix))));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?",Integer.class,prefix+"-failure")).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM mock_emails WHERE recipient_email=?",Integer.class,prefix+"@example.test")).isZero();
    }
    @Test void mailFailureRollsBackAccountAndAudit() throws Exception {
        doThrow(new IllegalStateException()).when(mail).send(anyLong(), anyString(), anyString(), anyString());
        create(body(prefix+"-failure",prefix+"@example.test","ADMIN","null")).andExpect(status().isInternalServerError()).andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(prefix))));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE username=?",Integer.class,prefix+"-failure")).isZero();
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM audit_log WHERE actor_user_id=? AND action='ACCOUNT_CREATED'",Integer.class,adminId)).isZero();
    }
}
