package vn.edu.se10.bluemoon.identity.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface RoleRepository extends Repository<Role, Long> {
    List<Role> findAll();

    Optional<Role> findById(Long id);
}
