package vn.edu.se10.bluemoon.identity.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoleRepository extends Repository<Role, Long> {
    Optional<Role> findByCode(String code);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Role r where r.code = :code")
    Optional<Role> findByCodeForUpdate(@Param("code") String code);
    List<Role> findAll();

    Optional<Role> findById(Long id);
}
