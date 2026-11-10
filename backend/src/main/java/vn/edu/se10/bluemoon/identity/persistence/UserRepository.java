package vn.edu.se10.bluemoon.identity.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    @Override
    @EntityGraph(attributePaths = "role")
    Page<User> findAll(Pageable pageable);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByRole_Code(String code);
    long countByRole_CodeAndStatus(String code, String status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u join fetch u.role where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") long id);
    @Query("select u from User u join fetch u.role where u.id = :id")
    Optional<User> findWithRoleById(@Param("id") long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u join fetch u.role where u.username = :username")
    Optional<User> findByUsernameForUpdate(@Param("username") String username);
}
