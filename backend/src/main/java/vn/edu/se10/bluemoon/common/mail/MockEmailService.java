package vn.edu.se10.bluemoon.common.mail;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
@Service
public class MockEmailService {
    private final MockEmailRepository repository;
    private final Clock clock;
    public MockEmailService(MockEmailRepository repository, Clock clock) { this.repository = repository; this.clock = clock; }
    @Transactional(propagation = Propagation.MANDATORY)
    public void send(long recipientUserId, String recipientEmail, String subject, String body) {
        repository.save(new MockEmail(recipientUserId, recipientEmail, subject, body, clock.instant()));
    }
}
