# db-fix: schema V2–V4

| Đường dẫn | Nội dung |
|---|---|
| `DATABASE.md` | Mô tả database: ERD, quan hệ, từng bảng |
| `TEAM_SUMMARY.md` | Việc mỗi người cần xác nhận |
| `EVIDENCE.md` | Kết quả Flyway, integration, smoke test |
| `migrations/V2__identity_audit.sql` | `roles`, `users`, `audit_log` (S01) |
| `migrations/V3__residency.sql` | `households`, `residents`, `residency_changes` (S05) |
| `migrations/V4__user_email_household.sql` | `users.email`, `users.household_id` (S02) |
| `verify/smoke.sh`, `verify/smoke_test.sql` | 16 ca thử ràng buộc trên MySQL 8.4 tạm |

## Đưa vào backend
Chép 3 file trong `migrations/` vào `backend/src/main/resources/db/migration/` (cạnh `V1__create_session_tables.sql`).

## Kiểm chứng
Cần Docker và JDK 21.
```bash
# Smoke test ràng buộc (container tạm, cổng 3308, tự xóa)
bash <đường dẫn>/verify/smoke.sh

# Flyway + integration trên MySQL tạm (cổng 3307)
docker run -d --rm --name bluemoon-verify -e MYSQL_ROOT_PASSWORD=verify_only_1 -e MYSQL_DATABASE=bluemoon_verify \
  -e MYSQL_USER=verify -e MYSQL_PASSWORD=verify_only_1 -p 127.0.0.1:3307:3306 mysql:8.4
until docker exec bluemoon-verify mysqladmin ping -h 127.0.0.1 -uverify -pverify_only_1 --silent; do sleep 2; done
cd backend
DB_HOST=127.0.0.1 DB_PORT=3307 DB_NAME=bluemoon_verify DB_USER=verify DB_PASSWORD=verify_only_1 \
  ./mvnw -B -ntp -Pintegration verify
docker rm -f bluemoon-verify
```
Dùng `127.0.0.1`, không dùng `localhost`, để không trỏ nhầm tới MySQL khác trên máy. Không chạy trên volume `bluemoon_mysql_data` khi còn có thể sửa V2–V4: Flyway ghi checksum, và `AGENTS.md` cấm tự reset volume.
