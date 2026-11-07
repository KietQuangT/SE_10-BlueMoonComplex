package vn.edu.se10.bluemoon.identity.application;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.mail.MockEmailService;
import vn.edu.se10.bluemoon.identity.persistence.User;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class PasswordChangeService {
    public enum Result { SUCCESS, LOCKED, CURRENT_PASSWORD_INCORRECT, PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, PASSWORD_WEAK, PASSWORD_SAME_AS_CURRENT }
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final LoginAttemptService attempts;
    private final MockEmailService mail;
    private final JdbcTemplate jdbc;
    private final AuditService audit;
    private final Clock clock;
    public PasswordChangeService(UserRepository users, PasswordEncoder encoder, LoginAttemptService attempts,
                                 MockEmailService mail, JdbcTemplate jdbc, AuditService audit, Clock clock) {
        this.users = users; this.encoder = encoder; this.attempts = attempts; this.mail = mail;
        this.jdbc = jdbc; this.audit = audit; this.clock = clock;
    }
    @Transactional
    public Result change(long id, String current, String replacement) {
        User user = users.findByIdForUpdate(id).filter(u -> "ACTIVE".equals(u.getStatus()))
                .orElseThrow(() -> new BadCredentialsException("Unauthorized"));
        Instant now = clock.instant();
        if (!"ADMIN".equals(user.getRole().getCode()) && user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) return Result.LOCKED;
        if (!encoder.matches(current, user.getPasswordHash())) {
            int failedCount = user.getFailedLoginCount() + 1;
            attempts.recordFailure(user, now);
            audit.record(id, "PASSWORD_CHANGE_FAILED", "User", Long.toString(id), java.util.Map.of("failedCount", Integer.toString(failedCount)));
            return Result.CURRENT_PASSWORD_INCORRECT;
        }
        if (replacement.length() < 8) return Result.PASSWORD_TOO_SHORT;
        if (replacement.getBytes(StandardCharsets.UTF_8).length > 72) return Result.PASSWORD_TOO_LONG;
        if (!replacement.codePoints().anyMatch(Character::isUpperCase) || !replacement.codePoints().anyMatch(Character::isLowerCase)
                || !replacement.codePoints().anyMatch(Character::isDigit) || !replacement.codePoints().anyMatch(c -> !Character.isLetterOrDigit(c))) return Result.PASSWORD_WEAK;
        if (encoder.matches(replacement, user.getPasswordHash())) return Result.PASSWORD_SAME_AS_CURRENT;
        boolean redact = user.isMustChangePassword();
        user.changePassword(encoder.encode(replacement), now);
        if (redact) mail.redactBodies(id);
        jdbc.update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", user.getUsername());
        audit.record(id, "PASSWORD_CHANGED", "User", Long.toString(id), null);
        audit.record(id, "SESSIONS_REVOKED", "User", Long.toString(id), null);
        return Result.SUCCESS;
    }
}
