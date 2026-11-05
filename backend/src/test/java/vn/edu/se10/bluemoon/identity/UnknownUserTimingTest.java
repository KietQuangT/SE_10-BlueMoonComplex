package vn.edu.se10.bluemoon.identity;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.application.AccountUserDetailsService;
import vn.edu.se10.bluemoon.identity.application.LoginAttemptService;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

class UnknownUserTimingTest {
    @Test void unknownUserStillPerformsDummyBcryptMatchWithoutAudit() {
        var users = mock(UserRepository.class);
        when(users.findByUsernameForUpdate(anyString())).thenReturn(Optional.empty());
        when(users.findByUsername(anyString())).thenReturn(Optional.empty());
        var encoder = spy(new BCryptPasswordEncoder());
        var provider = new DaoAuthenticationProvider(new AccountUserDetailsService(users));
        provider.setPasswordEncoder(encoder);
        var audit = mock(AuditService.class);
        var service = new LoginAttemptService(users, new ProviderManager(provider), audit, Clock.systemUTC());
        org.assertj.core.api.Assertions.assertThat(service.login("missing", "wrong").authenticated()).isFalse();
        verify(encoder).matches(eq("wrong"), anyString());
        verifyNoInteractions(audit);
    }
}
