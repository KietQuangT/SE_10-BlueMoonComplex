CREATE TABLE fee_types (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    calculation_method VARCHAR(20) NOT NULL,
    CONSTRAINT uq_fee_types_code UNIQUE (code),
    CONSTRAINT uq_fee_types_name UNIQUE (name),
    CONSTRAINT ck_fee_types_method CHECK (calculation_method IN ('AREA', 'PER_HOUSEHOLD'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO fee_types (code, name, calculation_method) VALUES
    ('MANAGEMENT_FEE', 'Phí quản lý', 'AREA'),
    ('WASTE_FEE', 'Phí rác sinh hoạt', 'PER_HOUSEHOLD');

CREATE TABLE fee_rates (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    fee_type_id BIGINT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    effective_from DATE NOT NULL,
    created_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_fee_rates_type FOREIGN KEY (fee_type_id) REFERENCES fee_types (id),
    CONSTRAINT fk_fee_rates_created_by FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT ck_fee_rates_price CHECK (unit_price > 0),
    CONSTRAINT ck_fee_rates_first_day CHECK (DAYOFMONTH(effective_from) = 1),
    CONSTRAINT uq_fee_rates_type_month UNIQUE (fee_type_id, effective_from)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
