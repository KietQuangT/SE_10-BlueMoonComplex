package vn.edu.se10.bluemoon.billing.persistence;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "fee_types")
public class FeeType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String code;
    @Column(nullable = false, length = 150)
    private String name;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "calculation_method", nullable = false, length = 20)
    private CalculationMethod calculationMethod;
    protected FeeType() { }
    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public CalculationMethod getCalculationMethod() { return calculationMethod; }
}
