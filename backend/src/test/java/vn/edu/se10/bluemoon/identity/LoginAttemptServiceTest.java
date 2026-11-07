package vn.edu.se10.bluemoon.identity;

import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.application.LoginAttemptService;
import vn.edu.se10.bluemoon.identity.persistence.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginAttemptServiceTest {
    @Test void realLoginPreservesEscalationAdminExemptionAndReset() {
        UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class);
        AuthenticationManager manager=mock(AuthenticationManager.class); Clock clock=mock(Clock.class);
        Instant now=Instant.parse("2026-11-07T00:00:00Z"); when(clock.instant()).thenReturn(now);
        when(role.getCode()).thenReturn("ACCOUNTANT"); User user=User.create("user","User",null,"hash",role,null,false,now);
        ReflectionTestUtils.setField(user,"id",1L); when(users.findByUsernameForUpdate("user")).thenReturn(Optional.of(user));
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        LoginAttemptService service=new LoginAttemptService(users,manager,audit,clock);
        long[] minutes={1,5,10,15,30,60};
        for(int level=0;level<minutes.length;level++) {
            for(int i=0;i<(level==0?5:1);i++) assertThat(service.login("user","bad").authenticated()).isFalse();
            assertThat(user.getLockLevel()).isEqualTo(level+1); assertThat(user.getLockedUntil()).isEqualTo(now.plusSeconds(minutes[level]*60));
            now=user.getLockedUntil().plusSeconds(1); when(clock.instant()).thenReturn(now);
        }
        var ordered=inOrder(audit); ordered.verify(audit,times(5)).record(1L,"LOGIN_FAILED","User","1",null);
        ordered.verify(audit).record(eq(1L),eq("ACCOUNT_LOCKED"),eq("User"),eq("1"),anyMap());
        Authentication authenticated=mock(Authentication.class); when(authenticated.isAuthenticated()).thenReturn(true);
        doReturn(authenticated).when(manager).authenticate(any()); assertThat(service.login("user","good").authenticated()).isTrue();
        assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockLevel()).isZero(); assertThat(user.getLockedUntil()).isNull();
        when(role.getCode()).thenReturn("ADMIN"); when(manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        for(int i=0;i<8;i++) service.login("user","bad"); assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockedUntil()).isNull();
    }
    @Test void passwordFailureAtLockThresholdAuditsActualCount() {
        UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class);
        Instant now=Instant.parse("2026-11-07T00:00:00Z"); when(role.getCode()).thenReturn("ACCOUNTANT");
        User user=User.create("user","User",null,"hash",role,null,false,now); ReflectionTestUtils.setField(user,"id",1L);
        LoginAttemptService service=new LoginAttemptService(users,mock(AuthenticationManager.class),audit,Clock.fixed(now,ZoneOffset.UTC));
        for(int i=0;i<5;i++) service.recordFailure(user,now,"PASSWORD_CHANGE_FAILED");
        verify(audit).record(1L,"PASSWORD_CHANGE_FAILED","User","1",java.util.Map.of("failedCount","5"));
        assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockedUntil()).isEqualTo(now.plusSeconds(60));
    }
    @Test void lockedLoginDoesNotAuthenticateCountOrAudit() {
        UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class);
        AuthenticationManager manager=mock(AuthenticationManager.class); Instant now=Instant.parse("2026-11-07T00:00:00Z");
        when(role.getCode()).thenReturn("ACCOUNTANT"); User user=User.create("user","User",null,"hash",role,null,false,now);
        user.lock(now.plusSeconds(60)); when(users.findByUsernameForUpdate("user")).thenReturn(Optional.of(user));
        assertThat(new LoginAttemptService(users,manager,audit,Clock.fixed(now,ZoneOffset.UTC)).login("user","good").authenticated()).isFalse();
        assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockedUntil()).isEqualTo(now.plusSeconds(60));
        verifyNoInteractions(manager,audit);
    }
    @Test void adminFailuresAreAuditedWithoutLocking() {
        UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class);
        AuthenticationManager manager=mock(AuthenticationManager.class); Instant now=Instant.parse("2026-11-07T00:00:00Z");
        when(role.getCode()).thenReturn("ADMIN"); User user=User.create("user","User",null,"hash",role,null,false,now);
        ReflectionTestUtils.setField(user,"id",1L); when(users.findByUsernameForUpdate("user")).thenReturn(Optional.of(user));
        when(manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        LoginAttemptService service=new LoginAttemptService(users,manager,audit,Clock.fixed(now,ZoneOffset.UTC));
        for(int i=0;i<8;i++) service.login("user","bad");
        verify(audit,times(8)).record(1L,"LOGIN_FAILED","User","1",null);
        assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockedUntil()).isNull(); assertThat(user.getLockLevel()).isZero();
    }
    @Test void lockLevelsBeyondSixtyMinutesIncreaseBySixty() {
        UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class);
        AuthenticationManager manager=mock(AuthenticationManager.class); Clock clock=mock(Clock.class);
        Instant now=Instant.parse("2026-11-07T00:00:00Z"); when(role.getCode()).thenReturn("ACCOUNTANT");
        User user=User.create("user","User",null,"hash",role,null,false,now); ReflectionTestUtils.setField(user,"id",1L);
        when(users.findByUsernameForUpdate("user")).thenReturn(Optional.of(user)); when(manager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        LoginAttemptService service=new LoginAttemptService(users,manager,audit,clock);
        long[] minutes={1,5,10,15,30,60,120,180};
        for(int level=0;level<minutes.length;level++) {
            when(clock.instant()).thenReturn(now);
            for(int i=0;i<(level==0?5:1);i++) service.login("user","bad");
            assertThat(user.getLockLevel()).isEqualTo(level+1); assertThat(user.getLockedUntil()).isEqualTo(now.plusSeconds(minutes[level]*60));
            now=user.getLockedUntil().plusSeconds(1);
        }
    }
}
