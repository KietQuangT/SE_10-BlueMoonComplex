package vn.edu.se10.bluemoon.common.mail;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mock_emails")
public class MockEmail {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "recipient_user_id", nullable = false) private long recipientUserId;
    @Column(name = "recipient_email", nullable = false, length = 255) private String recipientEmail;
    @Column(nullable = false, length = 200) private String subject;
    @Column(nullable = false, columnDefinition = "TEXT") private String body;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    protected MockEmail() {}
    MockEmail(long userId, String email, String subject, String body, Instant createdAt) {
        this.recipientUserId = userId; this.recipientEmail = email; this.subject = subject;
        this.body = body; this.createdAt = createdAt; this.status = "SIMULATED";
    }
}
