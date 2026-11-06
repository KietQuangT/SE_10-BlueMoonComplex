package vn.edu.se10.bluemoon.identity.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByRole_Code(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u join fetch u.role where u.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);
}
