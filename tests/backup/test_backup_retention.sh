#!/usr/bin/env bash
# ==============================================================================
# Tests: Backup Retention & Error Handling (V3 - Lỗi và 7 Bản)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd -P)"
BACKUP_SCRIPT="${PROJECT_ROOT}/scripts/backup.sh"

TEST_TMP="$(mktemp -d /tmp/bluemoon_test_retention_XXXXXX)"
trap 'rm -rf "${TEST_TMP}"' EXIT

PASSED=0
FAILED=0

assert_eq() {
    local desc="$1"
    local expected="$2"
    local actual="$3"
    if [[ "${expected}" == "${actual}" ]]; then
        echo "  [PASS] ${desc}"
        PASSED=$((PASSED + 1))
    else
        echo "  [FAIL] ${desc} (Expected: '${expected}', Actual: '${actual}')"
        FAILED=$((FAILED + 1))
    fi
}

echo "=== Chạy kiểm tra Retention và Xử lý Lỗi Backup (V3) ==="

EXT_DRIVE="${TEST_TMP}/ext_drive"
BACKUPS_DIR="${EXT_DRIVE}/backups"
CONF_FILE="${TEST_TMP}/backup.conf"
LOCK_FILE="${TEST_TMP}/backup.lock"
STATE_FILE="${TEST_TMP}/dispatch_state.json"

mkdir -p "${BACKUPS_DIR}"

cat <<EOF > "${CONF_FILE}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="backup_user"
DB_PASSWORD="secure_password"
BACKUP_DIR="${BACKUPS_DIR}"
BACKUP_MOUNT_POINT="${EXT_DRIVE}"
REQUIRE_ENCRYPTION_VERIFIED="true"
BACKUP_LOCK_FILE="${LOCK_FILE}"
DISPATCH_STATE_FILE="${STATE_FILE}"
EOF
chmod 0600 "${CONF_FILE}"

# Helper tạo synthetic valid mysqldump stream
MOCK_DUMP_VALID='cat <<DUMPEOF
-- MySQL dump 8.4
CREATE TABLE \`users\` (id INT);
INSERT INTO \`users\` VALUES (1);
CREATE TABLE \`residents\` (id INT);
INSERT INTO \`residents\` VALUES (1);
-- Dump completed on 2026-11-11 02:00:00
DUMPEOF'

# Helper tạo synthetic dump bị cắt ngắn (thiếu footer)
MOCK_DUMP_CORRUPT='cat <<DUMPEOF
-- MySQL dump 8.4
CREATE TABLE \`users\` (id INT);
INSERT INTO \`users\` VALUES (1);
DUMPEOF'

# Helper tạo synthetic dump chứa SPRING_SESSION (vi phạm Q4)
MOCK_DUMP_SESSION_LEAK='cat <<DUMPEOF
-- MySQL dump 8.4
CREATE TABLE \`users\` (id INT);
CREATE TABLE \`SPRING_SESSION\` (primary_id VARCHAR(36));
-- Dump completed on 2026-11-11 02:00:00
DUMPEOF'

# 1. Test: Dump bị cắt ngắn phải fail và không publish
set +e
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_CORRUPT}" "${BACKUP_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
CORRUPT_CODE=$?
set -e
assert_eq "Dump thiếu footer phải trả về lỗi" "1" "${CORRUPT_CODE}"
CNT_AFTER_FAIL=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Không có bundle nào được publish khi dump lỗi" "0" "${CNT_AFTER_FAIL}"

# 2. Test: Dump chứa bảng SPRING_SESSION phải bị chặn theo Q4
set +e
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_SESSION_LEAK}" "${BACKUP_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
LEAK_CODE=$?
set -e
assert_eq "Dump chứa SPRING_SESSION phải trả về lỗi" "1" "${LEAK_CODE}"

# 3. Test: Tạo 8 bản backup thành công liên tiếp -> Chỉ giữ đúng 7 bản mới nhất (Q3)
echo "Đang tạo 8 bản backup liên tiếp..."
for i in {1..8}; do
    # Sleep 1s để timestamp UTC phân biệt
    sleep 1
    MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" "${BACKUP_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
done

TOTAL_BUNDLES=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Sau 8 lần backup, chỉ còn lại đúng 7 bản mới nhất" "7" "${TOTAL_BUNDLES}"

# 4. Test: Thêm một thư mục hỏng (thiếu COMPLETED) -> không được tính vào 7 bản và không bị xóa bừa
mkdir -p "${BACKUPS_DIR}/bluemoon_backup_broken_dir"
TOTAL_WITH_BROKEN=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Tổng số thư mục gồm cả thư mục dở/hỏng là 8" "8" "${TOTAL_WITH_BROKEN}"

sleep 1
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" "${BACKUP_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1

COMPLETED_BUNDLES=0
for d in "${BACKUPS_DIR}"/bluemoon_backup_*; do
    if [[ -f "${d}/COMPLETED" ]]; then
        COMPLETED_BUNDLES=$((COMPLETED_BUNDLES + 1))
    fi
done
assert_eq "Số bản COMPLETED hợp lệ vẫn duy trì đúng 7 bản" "7" "${COMPLETED_BUNDLES}"
assert_eq "Thư mục hỏng không có COMPLETED vẫn được giữ lại để điều tra" "true" "$([[ -d "${BACKUPS_DIR}/bluemoon_backup_broken_dir" ]] && echo "true" || echo "false")"

# 5. Test: Kiểm tra Manifest tuân thủ Q5 allowlist
LATEST_BUNDLE=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" -exec test -f '{}/COMPLETED' ';' -print | sort | tail -n 1)
MANIFEST_JSON="${LATEST_BUNDLE}/manifest.json"

HAS_TABLE_COUNT=$(jq 'has("table_count")' "${MANIFEST_JSON}")
HAS_ROW_COUNT=$(jq 'has("total_row_count")' "${MANIFEST_JSON}")
HAS_SHA256=$(jq 'has("sha256")' "${MANIFEST_JSON}")
HAS_FORBIDDEN_USER=$(jq 'has("user") or has("password") or has("account")' "${MANIFEST_JSON}")

assert_eq "Manifest có table_count" "true" "${HAS_TABLE_COUNT}"
assert_eq "Manifest có total_row_count" "true" "${HAS_ROW_COUNT}"
assert_eq "Manifest có sha256" "true" "${HAS_SHA256}"
assert_eq "Manifest không chứa user/account/credential" "false" "${HAS_FORBIDDEN_USER}"

echo "Kết quả V3 Retention & Error: ${PASSED} đạt, ${FAILED} lỗi."
[[ ${FAILED} -eq 0 ]] || exit 1
