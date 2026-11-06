package vn.edu.se10.bluemoon.identity.application;

import java.time.Clock;
import vn.edu.se10.bluemoon.residency.application.HouseholdLookup;
import java.util.Map;
import java.util.Set;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.common.error.BadRequestException;
import vn.edu.se10.bluemoon.common.error.ConflictException;
import vn.edu.se10.bluemoon.common.mail.MockEmailService;
import vn.edu.se10.bluemoon.identity.persistence.*;

@Service
public class AccountProvisioningService {
    private static final Set<String> ROLES = Set.of("ADMIN", "ACCOUNTANT", "RESIDENT");
    private final UserRepository users; private final RoleRepository roles; private final AuditService audit;
    private final MockEmailService mail; private final PasswordEncoder encoder;
    private final TemporaryPasswordGenerator passwords; private final Clock clock; private final HouseholdLookup households;
    public AccountProvisioningService(UserRepository users, RoleRepository roles, AuditService audit, MockEmailService mail,
                                      PasswordEncoder encoder, TemporaryPasswordGenerator passwords, Clock clock, HouseholdLookup households) {
        this.users=users; this.roles=roles; this.audit=audit; this.mail=mail; this.encoder=encoder; this.passwords=passwords; this.clock=clock; this.households=households;
    }
    @Transactional
    public CreatedAccount create(CreateAccountCommand request, long actorId) {
        if (!ROLES.contains(request.role())) throw new BadRequestException("INVALID_ROLE");
        String email;
        try { email = EmailNormalizer.normalize(request.email()); }
        catch (IllegalArgumentException ex) { throw new BadRequestException("INVALID_EMAIL"); }
        Long householdId = request.householdId();
        if (request.role().equals("RESIDENT")) {
            if (householdId == null) throw new BadRequestException("HOUSEHOLD_REQUIRED");
            if (!households.findStatus(householdId).filter("ACTIVE"::equals).isPresent()) throw new BadRequestException("HOUSEHOLD_INVALID");
        } else if (householdId != null) throw new BadRequestException("HOUSEHOLD_NOT_ALLOWED");
        if (users.existsByEmail(email)) throw new ConflictException("EMAIL_ALREADY_USED");
        if (users.existsByUsername(request.username())) throw new ConflictException("USERNAME_ALREADY_USED");
        Role role = roles.findByCode(request.role()).orElseThrow();
        String temporaryPassword = passwords.generate();
        User user = User.create(request.username(), request.fullName().trim(), email, encoder.encode(temporaryPassword), role, householdId, true, clock.instant());
        try { user = users.saveAndFlush(user); }
        catch (DataIntegrityViolationException ex) {
            String code = "ACCOUNT_CONFLICT";
            for (Throwable t = ex; t != null; t = t.getCause()) {
                String message = t.getMessage();
                if (message != null && message.contains("uq_users_email")) { code = "EMAIL_ALREADY_USED"; break; }
                if (message != null && message.contains("uq_users_username")) { code = "USERNAME_ALREADY_USED"; break; }
            }
            throw new ConflictException(code);
        }
        Map<String,String> changes = householdId == null ? Map.of("role", request.role()) : Map.of("role", request.role(), "householdId", householdId.toString());
        audit.record(actorId, "ACCOUNT_CREATED", "User", user.getId().toString(), changes);
        mail.send(user.getId(), email, "Thông tin tài khoản BlueMoon", "Thư mô phỏng\nHọ tên: " + user.getFullName() + "\nTên đăng nhập: " + user.getUsername() + "\nMật khẩu tạm: " + temporaryPassword + "\nVui lòng đổi mật khẩu khi đăng nhập lần đầu.");
        return new CreatedAccount(user.getId(), user.getUsername(), user.getFullName(), email, request.role(), householdId);
    }
}
