-- S09: append-only financial history; reversals retain their source links.
CREATE TABLE payment_commands (
    idempotency_key CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL PRIMARY KEY,
    fingerprint CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    result_json JSON NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_household_locks (
    household_id BIGINT NOT NULL PRIMARY KEY,
    CONSTRAINT fk_payment_lock_household FOREIGN KEY (household_id) REFERENCES households (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_receipts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    household_id BIGINT NOT NULL,
    amount DECIMAL(24,0) NOT NULL,
    applied_amount DECIMAL(24,0) NOT NULL,
    credit_amount DECIMAL(24,0) NOT NULL,
    debt_after DECIMAL(24,0) NOT NULL,
    paid_on DATE NOT NULL,
    late_reason VARCHAR(255) NULL,
    status VARCHAR(16) NOT NULL,
    recorded_by BIGINT NOT NULL,
    recorded_at DATETIME(6) NOT NULL,
    voided_by BIGINT NULL,
    voided_at DATETIME(6) NULL,
    void_reason VARCHAR(255) NULL,
    KEY idx_receipt_invoice (invoice_id, id),
    KEY idx_receipt_household_fifo (household_id, id),
    CONSTRAINT fk_receipt_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT fk_receipt_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_receipt_actor FOREIGN KEY (recorded_by) REFERENCES users (id),
    CONSTRAINT fk_receipt_voider FOREIGN KEY (voided_by) REFERENCES users (id),
    CONSTRAINT ck_receipt_amount CHECK (amount > 0 AND applied_amount >= 0 AND credit_amount >= 0 AND debt_after >= 0 AND amount = applied_amount + credit_amount),
    CONSTRAINT ck_receipt_status CHECK (status IN ('POSTED', 'VOIDED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_credit_allocations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    household_id BIGINT NOT NULL,
    invoice_id BIGINT NOT NULL,
    amount DECIMAL(24,0) NOT NULL,
    debt_after DECIMAL(24,0) NOT NULL,
    status VARCHAR(16) NOT NULL,
    recorded_by BIGINT NOT NULL,
    recorded_at DATETIME(6) NOT NULL,
    reversed_by BIGINT NULL,
    reversed_at DATETIME(6) NULL,
    reverse_reason VARCHAR(255) NULL,
    KEY idx_allocation_invoice (invoice_id, status),
    CONSTRAINT fk_allocation_household FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_allocation_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (id),
    CONSTRAINT fk_allocation_actor FOREIGN KEY (recorded_by) REFERENCES users (id),
    CONSTRAINT fk_allocation_reverser FOREIGN KEY (reversed_by) REFERENCES users (id),
    CONSTRAINT ck_allocation_amount CHECK (amount > 0 AND debt_after >= 0),
    CONSTRAINT ck_allocation_status CHECK (status IN ('ACTIVE', 'REVERSED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment_credit_allocation_sources (
    allocation_id BIGINT NOT NULL,
    receipt_id BIGINT NOT NULL,
    amount DECIMAL(24,0) NOT NULL,
    PRIMARY KEY (allocation_id, receipt_id),
    KEY idx_allocation_source_receipt (receipt_id),
    CONSTRAINT fk_allocation_source_allocation FOREIGN KEY (allocation_id) REFERENCES payment_credit_allocations (id),
    CONSTRAINT fk_allocation_source_receipt FOREIGN KEY (receipt_id) REFERENCES payment_receipts (id),
    CONSTRAINT ck_allocation_source_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
