#!/usr/bin/env bash
# Smoke test ràng buộc V1–V4 trên MySQL 8.4 tạm (cổng 3308, tự xóa khi xong). Chạy ở bất kỳ đâu trong repo:
#   bash <đường dẫn>/verify/smoke.sh
# Đọc migration từ backend/src/main/resources/db/migration. Không đụng volume compose.
# Mật khẩu chỉ dùng cho container tạm này.
set -euo pipefail
HERE="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(git -C "$HERE" rev-parse --show-toplevel)"
M="$ROOT/backend/src/main/resources/db/migration"
C=bluemoon-smoke
trap 'docker rm -f "$C" >/dev/null 2>&1 || true' EXIT
docker rm -f "$C" >/dev/null 2>&1 || true
docker run -d --rm --name "$C" -e MYSQL_ROOT_PASSWORD=verify_only_1 -e MYSQL_DATABASE=bluemoon_smoke \
  -e MYSQL_USER=verify -e MYSQL_PASSWORD=verify_only_1 -p 127.0.0.1:3308:3306 mysql:8.4 >/dev/null
until docker exec "$C" mysqladmin ping -h 127.0.0.1 -uverify -pverify_only_1 --silent 2>/dev/null; do sleep 2; done
run() { docker exec -i "$C" mysql -uverify -pverify_only_1 bluemoon_smoke "$@" 2>&1 | grep -v 'Using a password' || true; }
for f in "$M"/V1__*.sql "$M"/V2__*.sql "$M"/V3__*.sql "$M"/V4__*.sql; do
  echo "== $(basename "$f")"; run < "$f"
done
echo "== smoke_test.sql"
run --force --table < "$HERE/smoke_test.sql"
