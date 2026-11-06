package vn.edu.se10.bluemoon.identity.application;

import java.time.Clock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.error.NotFoundException;
import vn.edu.se10.bluemoon.identity.persistence.RoleRepository;
import vn.edu.se10.bluemoon.identity.persistence.User;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class AccountManagementService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final JdbcTemplate jdbc;
    private final AuditService audit;
    private final Clock clock;
    public AccountManagementService(UserRepository users, RoleRepository roles, JdbcTemplate jdbc, AuditService audit, Clock clock) {
        this.users = users; this.roles = roles; this.jdbc = jdbc; this.audit = audit; this.clock = clock;
    }
    private User lock(long id, long actor) {
        // Serialize all management operations before locking a user, including competing ADMIN disables.
        roles.findByCodeForUpdate("ADMIN").orElseThrow();
        if (users.findByIdForUpdate(actor).filter(u -> "ACTIVE".equals(u.getStatus())).isEmpty())
            throw new org.springframework.security.authentication.BadCredentialsException("Unauthorized");
        return users.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("ACCOUNT_NOT_FOUND"));
    }
    @Transactional public void disable(long id, long actor) {
        User user = lock(id, actor);
        if (id == actor || "INACTIVE".equals(user.getStatus()) ||
            ("ADMIN".equals(user.getRole().getCode()) && users.countByRole_CodeAndStatus("ADMIN", "ACTIVE") <= 1))
            throw new ConflictException("ACCOUNT_DISABLE_CONFLICT");
        user.setStatus("INACTIVE", clock.instant());
        jdbc.update("DELETE FROM SPRING_SESSION WHERE PRINCIPAL_NAME = ?", user.getUsername());
        audit.record(actor, "ACCOUNT_DISABLED", "User", Long.toString(id), null);
        audit.record(actor, "SESSIONS_REVOKED", "User", Long.toString(id), null);
    }
    @Transactional public void enable(long id, long actor) {
        User user = lock(id, actor);
        if ("ACTIVE".equals(user.getStatus())) throw new ConflictException("ACCOUNT_ALREADY_ACTIVE");
        user.setStatus("ACTIVE", clock.instant());
        audit.record(actor, "ACCOUNT_ENABLED", "User", Long.toString(id), null);
    }
    @Transactional public void unlock(long id, long actor) {
        User user = lock(id, actor);
        if ("ADMIN".equals(user.getRole().getCode()) || (user.getLockedUntil() == null || !user.getLockedUntil().isAfter(clock.instant())) && user.getLockLevel() == 0 && user.getFailedLoginCount() == 0)
            throw new ConflictException("ACCOUNT_NOT_LOCKED");
        user.resetLoginFailures();
        audit.record(actor, "ACCOUNT_UNLOCKED", "User", Long.toString(id), null);
    }
}
