#!/usr/bin/env bash
# Read one local account's simulated provisioning email from the Compose database.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ $# -ne 1 ]]; then
  echo "Dùng: $0 <username>|--all" >&2
  exit 2
fi
if [[ "$1" == "--all" ]]; then
  query="SELECT CONCAT('Username: ', u.username, CHAR(10), m.body, CHAR(10), '---') FROM users u JOIN (SELECT recipient_user_id, body, ROW_NUMBER() OVER (PARTITION BY recipient_user_id ORDER BY created_at DESC, id DESC) AS rn FROM mock_emails WHERE subject = 'Thông tin tài khoản BlueMoon') m ON m.recipient_user_id = u.id AND m.rn = 1 WHERE u.must_change_password = TRUE AND LOCATE('Mật khẩu tạm:', m.body) > 0 ORDER BY u.username;"
elif [[ "$1" =~ ^[A-Za-z0-9._-]{3,100}$ ]]; then
  username="$1"
  query="SELECT m.body FROM mock_emails m JOIN users u ON u.id = m.recipient_user_id WHERE u.username = '${username}' AND u.must_change_password = TRUE AND m.subject = 'Thông tin tài khoản BlueMoon' ORDER BY m.created_at DESC, m.id DESC LIMIT 1;"
else
  echo "Dùng: $0 <username>|--all (username: 3–100 chữ, số, dấu chấm, gạch dưới hoặc gạch ngang)" >&2
  exit 2
fi
result="$(docker compose exec -T db sh -c 'export MYSQL_PWD="$MYSQL_PASSWORD"; exec mysql --default-character-set=utf8mb4 --batch --raw --skip-column-names -u "$MYSQL_USER" "$MYSQL_DATABASE"' <<< "$query")"
if [[ -z "$result" ]]; then
  echo 'Không tìm thấy thư cấp tài khoản còn mật khẩu tạm.' >&2
  exit 1
fi
printf '%s\n' "$result"
