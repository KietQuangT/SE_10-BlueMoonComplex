package vn.edu.se10.bluemoon.common.audit;

import org.springframework.data.repository.Repository;

/** Chỉ ghi: không kế thừa CrudRepository nên không có delete/update (audit không sửa, không xóa). */
interface AuditLogRepository extends Repository<AuditLog, Long> {
    AuditLog save(AuditLog log);
}
