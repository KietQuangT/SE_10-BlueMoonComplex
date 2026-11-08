package vn.edu.se10.bluemoon.billing.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "voluntary_contributions")
public class VoluntaryContribution {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "campaign_id", nullable = false, updatable = false) private long campaignId;
    @Column(name = "resident_id", nullable = false, updatable = false) private long residentId;
    @Column(name = "household_id", nullable = false, updatable = false) private long householdId;
    @Column(name = "donor_name_snapshot", nullable = false, length = 150, updatable = false) private String donorName;
    @Column(name = "household_code_snapshot", nullable = false, length = 50, updatable = false) private String householdCode;
    @Column(name = "apartment_code_snapshot", nullable = false, length = 50, updatable = false) private String apartmentCode;
    @Column(nullable = false, precision = 18, scale = 0, updatable = false) private BigDecimal amount;
    @Column(name = "paid_on", nullable = false, updatable = false) private LocalDate paidOn;
    @Column(name = "recorded_by", nullable = false, updatable = false) private long recordedBy;
    @Column(name = "recorded_at", nullable = false, updatable = false) private Instant recordedAt;
    @Column(nullable = false, length = 20) private String status;
    @Column(name = "late_reason", length = 255, updatable = false) private String lateReason;
    @Column(name = "voided_by") private Long voidedBy;
    @Column(name = "voided_at") private Instant voidedAt;
    @Column(name = "void_reason", length = 255) private String voidReason;
    @Version @Column(nullable = false) private long version;
    @Column(name = "idempotency_key", nullable = false, columnDefinition = "char(36)", updatable = false) private String idempotencyKey;
    @Column(name = "request_fingerprint", nullable = false, columnDefinition = "char(64)", updatable = false) private String requestFingerprint;

    protected VoluntaryContribution() { }

    public VoluntaryContribution(long campaignId, long residentId, long householdId, String donorName,
                                 String householdCode, String apartmentCode, BigDecimal amount, LocalDate paidOn,
                                 String lateReason, String idempotencyKey, String requestFingerprint, long actor, Instant now) {
        this.campaignId = campaignId;
        this.residentId = residentId;
        this.householdId = householdId;
        this.donorName = donorName;
        this.householdCode = householdCode;
        this.apartmentCode = apartmentCode;
        this.amount = amount;
        this.paidOn = paidOn;
        this.recordedBy = actor;
        this.recordedAt = now;
        this.status = "POSTED";
        this.lateReason = lateReason;
        this.idempotencyKey = idempotencyKey;
        this.requestFingerprint = requestFingerprint;
    }

    public void markVoided(long actor, Instant now, String reason) {
        status = "VOIDED"; voidedBy = actor; voidedAt = now; voidReason = reason;
    }
    public Long getId() { return id; }
    public long getCampaignId() { return campaignId; }
    public long getResidentId() { return residentId; }
    public long getHouseholdId() { return householdId; }
    public String getDonorName() { return donorName; }
    public String getHouseholdCode() { return householdCode; }
    public String getApartmentCode() { return apartmentCode; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getPaidOn() { return paidOn; }
    public long getRecordedBy() { return recordedBy; }
    public Instant getRecordedAt() { return recordedAt; }
    public String getStatus() { return status; }
    public String getLateReason() { return lateReason; }
    public Long getVoidedBy() { return voidedBy; }
    public Instant getVoidedAt() { return voidedAt; }
    public String getVoidReason() { return voidReason; }
    public long getVersion() { return version; }
    public String getRequestFingerprint() { return requestFingerprint; }
}
