package vn.edu.se10.bluemoon.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "voluntary_campaigns")
public class VoluntaryCampaign {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(length = 2000)
    private String description;
    @Column(name = "opens_on", nullable = false)
    private LocalDate opensOn;
    @Column(name = "closes_on", nullable = false)
    private LocalDate closesOn;
    @Column(nullable = false, length = 20)
    private String status;
    @Version @Column(nullable = false)
    private long version;
    @Column(name = "created_by", nullable = false, updatable = false)
    private long createdBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "published_by")
    private Long publishedBy;
    @Column(name = "published_at")
    private Instant publishedAt;
    @Column(name = "closed_by")
    private Long closedBy;
    @Column(name = "closed_at")
    private Instant closedAt;

    protected VoluntaryCampaign() { }

    public VoluntaryCampaign(String name, String description, LocalDate opensOn, LocalDate closesOn, long actor, Instant now) {
        this.name = name;
        this.description = description;
        this.opensOn = opensOn;
        this.closesOn = closesOn;
        this.createdBy = actor;
        this.createdAt = now;
        this.status = "DRAFT";
    }

    public void update(String name, String description, LocalDate opensOn, LocalDate closesOn) {
        this.name = name;
        this.description = description;
        this.opensOn = opensOn;
        this.closesOn = closesOn;
    }
    public void publish(long actor, Instant now) { status = "PUBLISHED"; publishedBy = actor; publishedAt = now; }
    public void close(long actor, Instant now) { status = "CLOSED"; closedBy = actor; closedAt = now; }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public LocalDate getOpensOn() { return opensOn; }
    public LocalDate getClosesOn() { return closesOn; }
    public String getStatus() { return status; }
    public long getVersion() { return version; }
    public long getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public Long getPublishedBy() { return publishedBy; }
    public Instant getPublishedAt() { return publishedAt; }
    public Long getClosedBy() { return closedBy; }
    public Instant getClosedAt() { return closedAt; }
}
