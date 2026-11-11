#!/usr/bin/env bash
# Read one local account's simulated provisioning email from the Compose database.
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ $# -ne 1 || ! "$1" =~ ^[1-9][0-9]{0,17}$ ]]; then
  echo "Dùng: $0 <user-id> (số nguyên dương, tối đa 18 chữ số)" >&2
  exit 2
fi
user_id="$1"
query="SELECT m.body FROM mock_emails m JOIN users u ON u.id = m.recipient_user_id WHERE m.recipient_user_id = ${user_id} AND u.must_change_password = TRUE AND m.subject = 'Thông tin tài khoản BlueMoon' ORDER BY m.created_at DESC, m.id DESC LIMIT 1;"
result="$(docker compose exec -T db sh -c 'export MYSQL_PWD="$MYSQL_PASSWORD"; exec mysql --default-character-set=utf8mb4 --batch --raw --skip-column-names -u "$MYSQL_USER" "$MYSQL_DATABASE"' <<< "$query")"
if [[ -z "$result" ]]; then
  echo 'Không tìm thấy thư cấp tài khoản còn mật khẩu tạm cho user ID này.' >&2
  exit 1
fi
printf '%s\n' "$result"
