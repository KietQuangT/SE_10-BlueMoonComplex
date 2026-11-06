ALTER TABLE users ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE mock_emails (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    recipient_user_id BIGINT NOT NULL,
    recipient_email VARCHAR(255) COLLATE utf8mb4_0900_as_ci NOT NULL,
    subject VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SIMULATED',
    created_at DATETIME(6) NOT NULL,
    KEY idx_mock_emails_recipient_created (recipient_user_id, created_at),
    CONSTRAINT fk_mock_emails_user FOREIGN KEY (recipient_user_id) REFERENCES users(id),
    CONSTRAINT ck_mock_emails_status CHECK (status IN ('SIMULATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
