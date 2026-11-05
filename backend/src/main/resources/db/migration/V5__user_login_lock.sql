-- S01: trạng thái chống dò mật khẩu; thời gian ứng dụng ghi theo UTC.
ALTER TABLE users
    ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0,
    ADD COLUMN lock_level INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until DATETIME(6) NULL,
    ADD CONSTRAINT ck_users_failed_login_count CHECK (failed_login_count >= 0),
    ADD CONSTRAINT ck_users_lock_level CHECK (lock_level >= 0);
