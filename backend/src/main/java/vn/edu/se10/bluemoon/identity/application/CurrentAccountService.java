package vn.edu.se10.bluemoon.identity.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

@Service
public class CurrentAccountService {
    private final UserRepository users;
    public CurrentAccountService(UserRepository users) { this.users = users; }
    @Transactional(readOnly = true)
    public AuthenticatedUser refresh(long id) {
        return users.findWithRoleById(id).filter(u -> "ACTIVE".equals(u.getStatus()))
            .map(u -> new AuthenticatedUser(u.getId(), u.getUsername(), u.getFullName(), u.getRole().getCode(), u.getHouseholdId(), u.isMustChangePassword()))
            .orElse(null);
    }
}
