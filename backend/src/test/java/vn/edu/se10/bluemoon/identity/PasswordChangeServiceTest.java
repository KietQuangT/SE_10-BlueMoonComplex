package vn.edu.se10.bluemoon.identity;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.mail.MockEmailService;
import vn.edu.se10.bluemoon.identity.application.*;
import vn.edu.se10.bluemoon.identity.persistence.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
class PasswordChangeServiceTest {
 UserRepository users=mock(UserRepository.class); Role role=mock(Role.class); AuditService audit=mock(AuditService.class); MockEmailService mail=mock(MockEmailService.class); JdbcTemplate jdbc=mock(JdbcTemplate.class); Clock clock=Clock.fixed(Instant.parse("2026-11-07T00:00:00Z"),ZoneOffset.UTC); BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(4);
 LoginAttemptService attempts=new LoginAttemptService(users,mock(AuthenticationManager.class),audit,clock); PasswordChangeService service=new PasswordChangeService(users,encoder,attempts,mail,jdbc,audit,clock); User user;
 @BeforeEach void setup(){when(role.getCode()).thenReturn("ACCOUNTANT"); user=User.create("account","User","email@example.test",encoder.encode("OldPass1!"),role,null,true,clock.instant()); ReflectionTestUtils.setField(user,"id",1L); when(users.findByIdForUpdate(1)).thenReturn(Optional.of(user));}
 @Test void wrongCurrentCountsAndAudits(){String hash=user.getPasswordHash(); assertThat(service.change(1,"wrong","x")).isEqualTo(PasswordChangeService.Result.CURRENT_PASSWORD_INCORRECT); assertThat(user.getPasswordHash()).isEqualTo(hash); assertThat(user.isMustChangePassword()).isTrue(); assertThat(user.getFailedLoginCount()).isEqualTo(1); verify(audit).record(1,"LOGIN_FAILED","User","1",null); verifyNoInteractions(jdbc,mail);}
 @Test void failureLocksNonAdminButNotAdmin(){for(int i=0;i<5;i++) service.change(1,"wrong","NewPass2!"); assertThat(user.getLockLevel()).isEqualTo(1); assertThat(user.getLockedUntil()).isAfter(clock.instant()); assertThat(service.change(1,"OldPass1!","NewPass2!")).isEqualTo(PasswordChangeService.Result.LOCKED); user.resetLoginFailures(); when(role.getCode()).thenReturn("ADMIN"); for(int i=0;i<6;i++) service.change(1,"wrong","NewPass2!"); assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockedUntil()).isNull();}
 @Test void validatesInOrderWithoutCountingNewPasswordErrors(){assertThat(service.change(1,"bad","a")).isEqualTo(PasswordChangeService.Result.CURRENT_PASSWORD_INCORRECT); int count=user.getFailedLoginCount(); assertThat(service.change(1,"OldPass1!","a")).isEqualTo(PasswordChangeService.Result.PASSWORD_TOO_SHORT); assertThat(service.change(1,"OldPass1!","ậ".repeat(25))).isEqualTo(PasswordChangeService.Result.PASSWORD_TOO_LONG); for(String value:new String[]{"lower123!","UPPER123!","Uppercase!","Upper1234"}) assertThat(service.change(1,"OldPass1!",value)).isEqualTo(PasswordChangeService.Result.PASSWORD_WEAK); assertThat(service.change(1,"OldPass1!","OldPass1!")).isEqualTo(PasswordChangeService.Result.PASSWORD_SAME_AS_CURRENT); assertThat(user.getFailedLoginCount()).isEqualTo(count); verifyNoInteractions(jdbc,mail);}
 @Test void successUpdatesUserAndRedactsOnlyFlaggedEmail(){user.failedAttempt(); user.lock(clock.instant().minusSeconds(1)); assertThat(service.change(1,"OldPass1!","MậtKhẩu2!")).isEqualTo(PasswordChangeService.Result.SUCCESS); assertThat(encoder.matches("MậtKhẩu2!",user.getPasswordHash())).isTrue(); assertThat(user.isMustChangePassword()).isFalse(); assertThat(user.getFailedLoginCount()).isZero(); assertThat(user.getLockLevel()).isZero(); assertThat(user.getLockedUntil()).isNull(); verify(mail).redactBodies(1); verify(jdbc).update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?","account"); verify(audit).record(1,"PASSWORD_CHANGED","User","1",null); verify(audit).record(1,"SESSIONS_REVOKED","User","1",null); assertThat(service.change(1,"MậtKhẩu2!","Another3!")).isEqualTo(PasswordChangeService.Result.SUCCESS); verify(mail,times(1)).redactBodies(1);}
 @Test void inactiveAccountIsUnauthorized(){user.setStatus("INACTIVE",clock.instant()); assertThatThrownBy(()->service.change(1,"OldPass1!","NewPass2!")).isInstanceOf(org.springframework.security.core.AuthenticationException.class); verifyNoInteractions(jdbc,mail,audit);}
}
