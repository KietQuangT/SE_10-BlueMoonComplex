CREATE DATABASE IF NOT EXISTS bluemoon
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE bluemoon;

CREATE TABLE roles (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_roles_code (code),
    UNIQUE KEY uq_roles_name (name)
) ENGINE = InnoDB;

CREATE TABLE users (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    role_id BIGINT UNSIGNED NOT NULL,
    username VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_username (username),
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB;

CREATE TABLE households (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    household_code VARCHAR(50) NOT NULL,
    apartment_code VARCHAR(50) NOT NULL,
    area_m2 DECIMAL(10,2) NOT NULL,
    address VARCHAR(255) NULL,
    head_resident_id BIGINT UNSIGNED NULL,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_households_code (household_code),
    UNIQUE KEY uq_households_apartment (apartment_code),
    CONSTRAINT ck_households_area CHECK (area_m2 > 0)
) ENGINE = InnoDB;

CREATE TABLE residents (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    household_id BIGINT UNSIGNED NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    identity_number VARCHAR(50) NULL,
    date_of_birth DATE NULL,
    phone VARCHAR(30) NULL,
    relationship_to_head VARCHAR(100) NULL,
    residency_status ENUM(
        'PERMANENT',
        'TEMPORARY_RESIDENCE',
        'TEMPORARY_ABSENCE',
        'MOVED_OUT'
    ) NOT NULL DEFAULT 'PERMANENT',
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_residents_identity (identity_number),
    KEY idx_residents_household (household_id),
    KEY idx_residents_name (full_name),
    CONSTRAINT fk_residents_household
        FOREIGN KEY (household_id) REFERENCES households (id)
) ENGINE = InnoDB;

ALTER TABLE households
    ADD CONSTRAINT fk_households_head_resident
    FOREIGN KEY (head_resident_id) REFERENCES residents (id);

CREATE TABLE residency_changes (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    resident_id BIGINT UNSIGNED NOT NULL,
    change_type ENUM(
        'MOVE_IN',
        'MOVE_OUT',
        'TEMPORARY_RESIDENCE',
        'TEMPORARY_ABSENCE'
    ) NOT NULL,
    effective_date DATE NOT NULL,
    reason VARCHAR(255) NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'APPROVED',
    recorded_by BIGINT UNSIGNED NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_residency_changes_resident_date (resident_id, effective_date),
    CONSTRAINT fk_residency_changes_resident
        FOREIGN KEY (resident_id) REFERENCES residents (id),
    CONSTRAINT fk_residency_changes_user
        FOREIGN KEY (recorded_by) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE fee_types (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    fee_category ENUM('MANDATORY', 'VOLUNTARY') NOT NULL,
    calculation_method ENUM(
        'AREA',
        'VEHICLE',
        'FIXED',
        'METER',
        'MANUAL'
    ) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uq_fee_types_code (code),
    UNIQUE KEY uq_fee_types_name (name)
) ENGINE = InnoDB;

CREATE TABLE fee_rates (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    fee_type_id BIGINT UNSIGNED NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    unit VARCHAR(30) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_fee_rates_type_dates (fee_type_id, effective_from, effective_to),
    CONSTRAINT fk_fee_rates_type
        FOREIGN KEY (fee_type_id) REFERENCES fee_types (id),
    CONSTRAINT ck_fee_rates_price CHECK (unit_price >= 0),
    CONSTRAINT ck_fee_rates_dates CHECK (
        effective_to IS NULL OR effective_to >= effective_from
    )
) ENGINE = InnoDB;

CREATE TABLE billing_periods (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    period_code VARCHAR(50) NOT NULL,
    name VARCHAR(150) NOT NULL,
    period_type ENUM('MONTHLY', 'VOLUNTARY_CAMPAIGN') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('DRAFT', 'OPEN', 'CLOSED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_billing_periods_code (period_code),
    CONSTRAINT ck_billing_periods_dates CHECK (end_date >= start_date)
) ENGINE = InnoDB;

CREATE TABLE household_charges (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    household_id BIGINT UNSIGNED NOT NULL,
    billing_period_id BIGINT UNSIGNED NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status ENUM('UNPAID', 'PARTIALLY_PAID', 'PAID', 'CANCELLED')
        NOT NULL DEFAULT 'UNPAID',
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_household_charges_period (household_id, billing_period_id),
    KEY idx_household_charges_status (status),
    CONSTRAINT fk_household_charges_household
        FOREIGN KEY (household_id) REFERENCES households (id),
    CONSTRAINT fk_household_charges_period
        FOREIGN KEY (billing_period_id) REFERENCES billing_periods (id),
    CONSTRAINT ck_household_charges_amounts CHECK (
        total_amount >= 0 AND paid_amount >= 0 AND paid_amount <= total_amount
    )
) ENGINE = InnoDB;

CREATE TABLE charge_details (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    charge_id BIGINT UNSIGNED NOT NULL,
    fee_type_id BIGINT UNSIGNED NOT NULL,
    fee_rate_id BIGINT UNSIGNED NULL,
    quantity DECIMAL(12,2) NOT NULL DEFAULT 1,
    unit_price DECIMAL(12,2) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    description VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_charge_details_charge (charge_id),
    CONSTRAINT fk_charge_details_charge
        FOREIGN KEY (charge_id) REFERENCES household_charges (id),
    CONSTRAINT fk_charge_details_fee_type
        FOREIGN KEY (fee_type_id) REFERENCES fee_types (id),
    CONSTRAINT fk_charge_details_fee_rate
        FOREIGN KEY (fee_rate_id) REFERENCES fee_rates (id),
    CONSTRAINT ck_charge_details_values CHECK (
        quantity > 0 AND unit_price >= 0 AND amount >= 0
    )
) ENGINE = InnoDB;

CREATE TABLE payments (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    charge_id BIGINT UNSIGNED NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_method ENUM('CASH', 'BANK_TRANSFER', 'OTHER') NOT NULL,
    paid_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_by BIGINT UNSIGNED NOT NULL,
    reference_code VARCHAR(100) NULL,
    note VARCHAR(255) NULL,
    PRIMARY KEY (id),
    KEY idx_payments_charge_date (charge_id, paid_at),
    UNIQUE KEY uq_payments_reference (reference_code),
    CONSTRAINT fk_payments_charge
        FOREIGN KEY (charge_id) REFERENCES household_charges (id),
    CONSTRAINT fk_payments_user
        FOREIGN KEY (received_by) REFERENCES users (id),
    CONSTRAINT ck_payments_amount CHECK (amount > 0)
) ENGINE = InnoDB;
