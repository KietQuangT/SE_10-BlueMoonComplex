package vn.edu.se10.bluemoon.common.mail;
import org.springframework.data.repository.CrudRepository;
public interface MockEmailRepository extends CrudRepository<MockEmail, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("update MockEmail m set m.body = :body where m.recipientUserId = :userId")
    int redactBodies(@org.springframework.data.repository.query.Param("userId") long userId, @org.springframework.data.repository.query.Param("body") String body);
}
