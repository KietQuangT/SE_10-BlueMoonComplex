USE bluemoon;

INSERT INTO roles (id, code, name)
VALUES
    (1, 'ADMIN', 'Ban quản trị'),
    (2, 'ACCOUNTANT', 'Kế toán');

INSERT INTO users (id, role_id, username, password_hash, full_name)
VALUES
    (1, 1, 'admin.demo', 'replace-with-application-password-hash', 'Quản trị viên mẫu'),
    (2, 2, 'accountant.demo', 'replace-with-application-password-hash', 'Kế toán mẫu');

INSERT INTO households (
    id, household_code, apartment_code, area_m2, address
)
VALUES
    (1, 'H001', 'A01', 50.00, 'Tòa A - Căn hộ A01'),
    (2, 'H002', 'B02', 70.00, 'Tòa B - Căn hộ B02');

INSERT INTO residents (
    id, household_id, full_name, identity_number, date_of_birth,
    phone, relationship_to_head, residency_status
)
VALUES
    (1, 1, 'Nguyễn Văn An', '001000000001', '1985-04-12',
     '0900000001', 'Chủ hộ', 'PERMANENT'),
    (2, 1, 'Trần Thị Bình', '001000000002', '1987-08-21',
     '0900000002', 'Vợ', 'PERMANENT'),
    (3, 2, 'Lê Văn Cường', '001000000003', '1978-02-14',
     '0900000003', 'Chủ hộ', 'PERMANENT'),
    (4, 2, 'Phạm Thị Dung', '001000000004', '1980-11-03',
     '0900000004', 'Vợ', 'MOVED_OUT');

UPDATE households SET head_resident_id = 1 WHERE id = 1;
UPDATE households SET head_resident_id = 3 WHERE id = 2;

INSERT INTO residency_changes (
    resident_id, change_type, effective_date, reason, status, recorded_by
)
VALUES
    (4, 'MOVE_OUT', '2026-10-01', 'Chuyển sang nơi ở khác', 'APPROVED', 1);

INSERT INTO fee_types (
    id, code, name, fee_category, calculation_method
)
VALUES
    (1, 'SERVICE_FEE', 'Phí dịch vụ', 'MANDATORY', 'AREA'),
    (2, 'MANAGEMENT_FEE', 'Phí quản lý', 'MANDATORY', 'AREA'),
    (3, 'MOTORBIKE_PARKING', 'Phí gửi xe máy', 'MANDATORY', 'VEHICLE'),
    (4, 'VOLUNTARY_CONTRIBUTION', 'Đóng góp tự nguyện', 'VOLUNTARY', 'MANUAL');

INSERT INTO fee_rates (
    id, fee_type_id, unit_price, unit, effective_from
)
VALUES
    (1, 1, 7000.00, 'm2/month', '2026-01-01'),
    (2, 2, 7000.00, 'm2/month', '2026-01-01'),
    (3, 3, 70000.00, 'vehicle/month', '2026-01-01');

INSERT INTO billing_periods (
    id, period_code, name, period_type, start_date, end_date, status
)
VALUES
    (1, '2026-11', 'Kỳ thu tháng 11/2026', 'MONTHLY',
     '2026-11-01', '2026-11-30', 'OPEN'),
    (2, 'FUND-2026-11', 'Quỹ tự nguyện tháng 11/2026',
     'VOLUNTARY_CAMPAIGN', '2026-11-01', '2026-11-30', 'OPEN');

INSERT INTO household_charges (
    id, household_id, billing_period_id, total_amount, paid_amount, status
)
VALUES
    (1, 1, 1, 700000.00, 350000.00, 'PARTIALLY_PAID'),
    (2, 2, 1, 980000.00, 980000.00, 'PAID'),
    (3, 1, 2, 100000.00, 100000.00, 'PAID');

INSERT INTO charge_details (
    id, charge_id, fee_type_id, fee_rate_id, quantity, unit_price, amount, description
)
VALUES
    (1, 1, 2, 2, 50.00, 7000.00, 350000.00, 'Phí quản lý theo diện tích'),
    (2, 1, 1, 1, 50.00, 7000.00, 350000.00, 'Phí dịch vụ theo diện tích'),
    (3, 2, 2, 2, 70.00, 7000.00, 490000.00, 'Phí quản lý theo diện tích'),
    (4, 2, 1, 1, 70.00, 7000.00, 490000.00, 'Phí dịch vụ theo diện tích'),
    (5, 3, 4, NULL, 1.00, 100000.00, 100000.00, 'Đóng góp tự nguyện');

INSERT INTO payments (
    id, charge_id, amount, payment_method, paid_at, received_by, reference_code
)
VALUES
    (1, 1, 350000.00, 'CASH', '2026-11-05 09:00:00', 2, 'PAY-2026-0001'),
    (2, 2, 980000.00, 'BANK_TRANSFER', '2026-11-05 10:00:00', 2, 'PAY-2026-0002'),
    (3, 3, 100000.00, 'CASH', '2026-11-06 11:00:00', 2, 'PAY-2026-0003');
