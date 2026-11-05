package vn.edu.se10.bluemoon.identity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Query;
import vn.edu.se10.bluemoon.identity.persistence.UserRepository;

class UserRepositoryQueryTest {
    @Test void lockedLookupLeavesIndexedUsernameColumnUnwrapped() throws Exception {
        String query = UserRepository.class.getMethod("findByUsernameForUpdate", String.class)
                .getAnnotation(Query.class).value();
        assertThat(query).contains("u.username = :username").doesNotContain("lower(u.username)");
    }
}
