package vn.edu.se10.bluemoon.identity.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/** Identity data used for authentication and account provisioning. */
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String username;
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;
    @Column(nullable = false, length = 20)
    private String status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(name = "household_id")
    private Long householdId;
    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount;
    @Column(name = "lock_level", nullable = false)
    private int lockLevel;
    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(length = 255) private String email;
    @Column(name = "must_change_password", nullable = false) private boolean mustChangePassword;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    protected User() {}

    public static User create(String username, String fullName, String email, String passwordHash,
                              Role role, Long householdId, boolean mustChangePassword, Instant now) {
        User user = new User();
        user.username = username; user.fullName = fullName; user.email = email; user.passwordHash = passwordHash;
        user.role = role; user.householdId = householdId; user.mustChangePassword = mustChangePassword;
        user.status = "ACTIVE"; user.createdAt = now; user.updatedAt = now;
        return user;
    }
    public String getEmail() { return email; }
    public boolean isMustChangePassword() { return mustChangePassword; }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getFullName() { return fullName; }
    public String getStatus() { return status; }
    public Role getRole() { return role; }
    public Long getHouseholdId() { return householdId; }
    public int getFailedLoginCount() { return failedLoginCount; }
    public int getLockLevel() { return lockLevel; }
    public Instant getLockedUntil() { return lockedUntil; }

    public void changePassword(String hash, Instant now) { passwordHash = hash; mustChangePassword = false; updatedAt = now; resetLoginFailures(); }
    public void setStatus(String status, Instant now) { this.status = status; this.updatedAt = now; }
    public void failedAttempt() { failedLoginCount++; }
    public void lock(Instant until) { lockLevel++; failedLoginCount = 0; lockedUntil = until; }
    public void resetLoginFailures() { failedLoginCount = 0; lockLevel = 0; lockedUntil = null; }
}
