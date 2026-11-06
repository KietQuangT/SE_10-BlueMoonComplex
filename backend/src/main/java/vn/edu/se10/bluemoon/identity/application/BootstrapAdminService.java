package vn.edu.se10.bluemoon.identity.application;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Arrays;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.persistence.*;

@Service
public class BootstrapAdminService {
    private final UserRepository users; private final RoleRepository roles; private final AuditService audit;
    private final PasswordEncoder encoder; private final Clock clock;
    public BootstrapAdminService(UserRepository users, RoleRepository roles, AuditService audit, PasswordEncoder encoder, Clock clock) {
        this.users=users; this.roles=roles; this.audit=audit; this.encoder=encoder; this.clock=clock;
    }
    @Transactional
    public boolean create(String username, String fullName, String email, char[] password, char[] confirmation) {
        try {
            Role role = roles.findByCodeForUpdate("ADMIN").orElseThrow();
            if (users.existsByRole_Code("ADMIN")) return false;
            if (password == null || confirmation == null || password.length < 8 || !Arrays.equals(password, confirmation)
                    || new String(password).getBytes(StandardCharsets.UTF_8).length > 72)
                throw new IllegalArgumentException("Dữ liệu không hợp lệ.");
            if (username == null || !username.matches("[A-Za-z0-9._-]{3,100}") || fullName == null || fullName.isBlank()
                    || fullName.length() > 150 || fullName.chars().anyMatch(Character::isISOControl))
                throw new IllegalArgumentException("Dữ liệu không hợp lệ.");
            String normalized = EmailNormalizer.normalize(email);
            if (users.existsByEmail(normalized) || users.existsByUsername(username)) throw new IllegalArgumentException("Dữ liệu không hợp lệ.");
            User user = users.saveAndFlush(User.create(username, fullName.trim(), normalized, encoder.encode(new String(password)), role, null, false, clock.instant()));
            audit.recordBootstrap(user.getId().toString(), Map.of("role", "ADMIN"));
            return true;
        } finally {
            if (password != null) Arrays.fill(password, '\0');
            if (confirmation != null) Arrays.fill(confirmation, '\0');
        }
    }
}
