package vn.edu.se10.bluemoon.billing.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "fee_rates")
public class FeeRate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_type_id", nullable = false)
    private FeeType feeType;
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    protected FeeRate() { }
    public FeeRate(FeeType feeType, BigDecimal unitPrice, LocalDate effectiveFrom, long createdBy, Instant createdAt) {
        this.feeType = feeType;
        this.unitPrice = unitPrice;
        this.effectiveFrom = effectiveFrom;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
    public Long getId() { return id; }
    public FeeType getFeeType() { return feeType; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public Instant getCreatedAt() { return createdAt; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
