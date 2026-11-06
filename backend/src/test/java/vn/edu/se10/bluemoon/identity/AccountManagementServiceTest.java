package vn.edu.se10.bluemoon.identity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.identity.application.AccountManagementService;
import vn.edu.se10.bluemoon.identity.persistence.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountManagementServiceTest {
    UserRepository users=mock(UserRepository.class); RoleRepository roles=mock(RoleRepository.class);
    JdbcTemplate jdbc=mock(JdbcTemplate.class); AuditService audit=mock(AuditService.class);
    Clock clock=Clock.fixed(Instant.parse("2026-11-06T00:00:00Z"),ZoneOffset.UTC);
    AccountManagementService service=new AccountManagementService(users,roles,jdbc,audit,clock);
    Role role=mock(Role.class); User user=mock(User.class); User actor=mock(User.class);
    void target(String status, String roleCode) {
        when(roles.findByCodeForUpdate("ADMIN")).thenReturn(Optional.of(role));
        when(users.findByIdForUpdate(1)).thenReturn(Optional.of(actor)); when(actor.getStatus()).thenReturn("ACTIVE");
        when(users.findByIdForUpdate(2)).thenReturn(Optional.of(user));
        when(user.getStatus()).thenReturn(status); when(user.getRole()).thenReturn(role); when(role.getCode()).thenReturn(roleCode);
        when(user.getUsername()).thenReturn("account");
    }
    @Test void lastAdminAndSelfAreProtected() {
        target("ACTIVE","ADMIN"); when(users.countByRole_CodeAndStatus("ADMIN","ACTIVE")).thenReturn(1L);
        assertThatThrownBy(() -> service.disable(2,1)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> service.disable(2,2)).isInstanceOf(ConflictException.class);
        verifyNoInteractions(jdbc,audit);
    }
    @Test void disableDeletesSessionsAndRecordsBothActions() {
        target("ACTIVE","ACCOUNTANT"); service.disable(2,1);
        verify(user).setStatus("INACTIVE",clock.instant());
        verify(jdbc).update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?","account");
        verify(audit).record(1,"ACCOUNT_DISABLED","User","2",null);
        verify(audit).record(1,"SESSIONS_REVOKED","User","2",null);
    }
    @Test void inactiveActorCannotManageAccounts() {
        target("ACTIVE", "ACCOUNTANT"); when(actor.getStatus()).thenReturn("INACTIVE");
        assertThatThrownBy(() -> service.disable(2,1)).isInstanceOf(org.springframework.security.core.AuthenticationException.class);
        verifyNoInteractions(jdbc,audit); verify(user, never()).setStatus(anyString(), any());
    }
    @Test void unlockClearsExpiredLockLevelAndFailedCount() {
        target("ACTIVE", "ACCOUNTANT"); when(user.getLockedUntil()).thenReturn(clock.instant().minusSeconds(1));
        when(user.getLockLevel()).thenReturn(1); service.unlock(2,1);
        when(user.getLockLevel()).thenReturn(0); when(user.getFailedLoginCount()).thenReturn(1); service.unlock(2,1);
        verify(user, times(2)).resetLoginFailures(); verify(audit, times(2)).record(1,"ACCOUNT_UNLOCKED","User","2",null);
    }
    @Test void unlockRejectsAdminAndExpiredLock() {
        target("ACTIVE","ADMIN"); assertThatThrownBy(() -> service.unlock(2,1)).isInstanceOf(ConflictException.class);
        when(role.getCode()).thenReturn("ACCOUNTANT"); when(user.getLockedUntil()).thenReturn(clock.instant());
        assertThatThrownBy(() -> service.unlock(2,1)).isInstanceOf(ConflictException.class);
        verifyNoInteractions(audit);
    }
}
