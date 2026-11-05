package vn.edu.se10.bluemoon.identity.application;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.se10.bluemoon.common.audit.AuditService;
import vn.edu.se10.bluemoon.identity.persistence.Role;
import vn.edu.se10.bluemoon.identity.persistence.RoleRepository;

/**
 * MẪU luồng entity/service/audit của F02. CHƯA có kiểm tra quyền: AGENTS.md đòi kiểm tra quyền
 * ở server nhưng ma trận quyền thuộc F01/S01. KHÔNG gắn vào controller trước khi S01 có quyền.
 * S01 thay hoặc xóa lớp này.
 */
@Service
public class RoleService {
    public static class RoleNotFoundException extends RuntimeException {
        public RoleNotFoundException(long roleId) {
            super("Không tìm thấy vai trò id=" + roleId);
        }
    }

    private final RoleRepository roles;
    private final AuditService audit;

    RoleService(RoleRepository roles, AuditService audit) {
        this.roles = roles;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<RoleView> list() {
        return roles.findAll().stream().map(RoleService::view).toList();
    }

    @Transactional
    public RoleView rename(long actorUserId, long roleId, String newName) {
        Role role = roles.findById(roleId).orElseThrow(() -> new RoleNotFoundException(roleId));
        String oldName = role.getName();
        role.rename(newName);
        audit.record(actorUserId, "ROLE_RENAMED", "Role", String.valueOf(roleId),
                Map.of("oldName", oldName, "newName", newName));
        return view(role);
    }

    private static RoleView view(Role role) {
        return new RoleView(role.getId(), role.getCode(), role.getName());
    }
}
