package vn.edu.se10.bluemoon.identity.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.persistence.User;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class LoginAttemptService {
    public record Result(AuthenticatedUser user, boolean authenticated) {}

    private final UserRepository users;
    private final AuthenticationManager authenticationManager;
    private final AuditService audit;
    private final Clock clock;

    public LoginAttemptService(UserRepository users, AuthenticationManager authenticationManager, AuditService audit, Clock clock) {
        this.users = users; this.authenticationManager = authenticationManager; this.audit = audit; this.clock = clock;
    }

    @Transactional
    public Result login(String username, String password) {
        User user = users.findByUsernameForUpdate(username).orElse(null);
        if (user == null) {
            // DaoAuthenticationProvider performs its dummy BCrypt match for unknown users.
            try { authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password)); }
            catch (AuthenticationException | IllegalArgumentException ignored) { }
            return new Result(null, false);
        }

        Instant now = clock.instant();
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(now)) {
            return new Result(null, false);
        }
        if (!"ACTIVE".equals(user.getStatus()) || ("RESIDENT".equals(user.getRole().getCode()) && user.getHouseholdId() == null)) {
            dummyAuthenticate(username, password);
            return new Result(null, false);
        }

        Authentication authentication = null;
        try {
            authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(user.getUsername(), password));
        } catch (AuthenticationException | IllegalArgumentException ignored) {
            // Failure is returned as data so lock counters and audit commit with this transaction.
        }
        if (authentication == null || !authentication.isAuthenticated()) {
            boolean admin = "ADMIN".equals(user.getRole().getCode());
            if (!admin) {
                user.failedAttempt();
                if (user.getFailedLoginCount() >= LoginLockPolicy.failureThreshold(user.getLockLevel())) {
                    int nextLevel = user.getLockLevel() == Integer.MAX_VALUE ? Integer.MAX_VALUE : user.getLockLevel() + 1;
                    var until = LoginLockPolicy.lockedUntil(now, nextLevel);
                    user.lock(until);
                    audit.record(user.getId(), "LOGIN_FAILED", "User", user.getId().toString(), null);
                    audit.record(user.getId(), "ACCOUNT_LOCKED", "User", user.getId().toString(),
                            Map.of("level", Integer.toString(nextLevel), "minutes", Long.toString(LoginLockPolicy.lockDuration(nextLevel).toMinutes())));
                    return new Result(null, false);
                }
            }
            audit.record(user.getId(), "LOGIN_FAILED", "User", user.getId().toString(), null);
            return new Result(null, false);
        }

        user.resetLoginFailures();
        audit.record(user.getId(), "LOGIN_SUCCESS", "User", user.getId().toString(), null);
        return new Result(new AuthenticatedUser(user.getId(), user.getUsername(), user.getFullName(), user.getRole().getCode(), user.getHouseholdId()), true);
    }

    private void dummyAuthenticate(String username, String password) {
        try { authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password)); }
        catch (AuthenticationException | IllegalArgumentException ignored) { }
    }
}
