USE bluemoon;

DROP PROCEDURE IF EXISTS run_bluemoon_db_tests;

DELIMITER //

CREATE PROCEDURE run_bluemoon_db_tests()
BEGIN
    DECLARE actual_count INT DEFAULT 0;
    DECLARE actual_amount DECIMAL(12,2) DEFAULT 0;

    SELECT 'T1 - Có đủ bảng lõi' AS test_name;
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = DATABASE()
      AND table_name IN (
          'roles', 'users', 'households', 'residents',
          'residency_changes', 'fee_types', 'fee_rates',
          'billing_periods', 'household_charges',
          'charge_details', 'payments'
      )
    ORDER BY table_name;

    SELECT 'T2 - Tính phí quản lý 50m2 x 7.000' AS test_name;
    SELECT quantity * unit_price AS calculated_amount
    FROM charge_details
    WHERE charge_id = 1
      AND fee_type_id = 2;

    SELECT 'T3 - Khoản tự nguyện không thuộc phí bắt buộc' AS test_name;
    SELECT ft.name, ft.fee_category, hc.total_amount
    FROM household_charges hc
    JOIN charge_details cd ON cd.charge_id = hc.id
    JOIN fee_types ft ON ft.id = cd.fee_type_id
    WHERE hc.id = 3;

    SELECT 'T4 - Lịch sử người đã chuyển đi vẫn tồn tại' AS test_name;
    SELECT full_name, residency_status
    FROM residents
    WHERE identity_number = '001000000004';

    SELECT 'T5 - Tổng thanh toán khớp paid_amount' AS test_name;
    SELECT hc.id, hc.paid_amount, COALESCE(SUM(p.amount), 0) AS payment_sum
    FROM household_charges hc
    LEFT JOIN payments p ON p.charge_id = hc.id
    GROUP BY hc.id, hc.paid_amount
    ORDER BY hc.id;

    SELECT COUNT(*) INTO actual_count
    FROM charge_details
    WHERE charge_id = 1
      AND fee_type_id = 2
      AND quantity = 50
      AND unit_price = 7000
      AND amount = 350000;
    IF actual_count <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'T2 failed: fee calculation data is incorrect';
    END IF;

    SELECT COUNT(*) INTO actual_count
    FROM fee_types
    WHERE code = 'VOLUNTARY_CONTRIBUTION'
      AND fee_category = 'VOLUNTARY';
    IF actual_count <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'T3 failed: voluntary fee category is incorrect';
    END IF;

    SELECT COUNT(*) INTO actual_count
    FROM residents
    WHERE identity_number = '001000000004'
      AND residency_status = 'MOVED_OUT';
    IF actual_count <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'T4 failed: moved-out resident history is missing';
    END IF;

    SELECT COALESCE(SUM(p.amount), 0) INTO actual_amount
    FROM payments p
    WHERE p.charge_id = 1;
    IF actual_amount <> 350000 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'T5 failed: payment total is incorrect';
    END IF;

    SELECT 'PASS - BlueMoon DB tests completed' AS result;
END//

DELIMITER ;

CALL run_bluemoon_db_tests();
DROP PROCEDURE run_bluemoon_db_tests;
