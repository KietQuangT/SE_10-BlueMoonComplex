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
@Table(name = "billing_periods")
public class BillingPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 7, columnDefinition = "char(7)")
    private String period;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(nullable = false, length = 20)
    private String status;
    @Version
    @Column(nullable = false)
    private long version;
    @Column(nullable = false)
    private long revision;
    @Column(name = "receipt_count", nullable = false)
    private long receiptCount;
    @Column(name = "replaces_period_id")
    private Long replacesPeriodId;
    @Column(name = "created_by", nullable = false, updatable = false)
    private long createdBy;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BillingPeriod() { }

    public BillingPeriod(String period, LocalDate dueDate, Long replacesPeriodId, long actor, Instant createdAt) {
        this.period = period;
        this.dueDate = dueDate;
        this.status = "DRAFT";
        this.replacesPeriodId = replacesPeriodId;
        this.createdBy = actor;
        this.createdAt = createdAt;
    }

    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Long getId() { return id; }
    public String getPeriod() { return period; }
    public LocalDate getDueDate() { return dueDate; }
    public String getStatus() { return status; }
    public long getVersion() { return version; }
    public long getRevision() { return revision; }
    public long getReceiptCount() { return receiptCount; }
    public Long getReplacesPeriodId() { return replacesPeriodId; }
    public Instant getCreatedAt() { return createdAt; }
}
