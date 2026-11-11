#!/usr/bin/env bash
# ==============================================================================
# Tests: Dispatcher Schedule & Catch-up (V4 - Lịch và Catch-up)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd -P)"
DISPATCH_SCRIPT="${PROJECT_ROOT}/scripts/backup-dispatch.sh"

TEST_TMP="$(mktemp -d /tmp/bluemoon_test_dispatch_XXXXXX)"
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

echo "=== Chạy kiểm tra Dispatcher Lịch và Catch-up (V4) ==="

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

MOCK_DUMP_VALID='cat <<DUMPEOF
-- MySQL dump 8.4
CREATE TABLE \`users\` (id INT);
INSERT INTO \`users\` VALUES (1);
-- Dump completed on 2026-11-11 02:00:00
DUMPEOF'

# 1. Test: Chưa có backup khởi tạo và không có state -> fail-closed
set +e
MOCK_CONTAINER_READY="true" "${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
NO_INIT_CODE=$?
set -e
assert_eq "Chưa có backup khởi tạo và chưa có state thì phải fail-closed" "1" "${NO_INIT_CODE}"

# Tạo 1 backup khởi tạo để khởi động state
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" "${PROJECT_ROOT}/scripts/backup.sh" --config "${CONF_FILE}" >/dev/null 2>&1
INIT_BUNDLE=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | head -n 1)
assert_eq "Đã có backup khởi tạo" "true" "$([[ -n "${INIT_BUNDLE}" ]] && echo "true" || echo "false")"

# 2. Test: Dispatcher khởi tạo state từ bundle có sẵn khi state file chưa tồn tại
rm -f "${STATE_FILE}"
NOW_EPOCH="$(date +%s)"
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_1" \
MOCK_NOW_EPOCH="${NOW_EPOCH}" \
MOCK_NOW_VN_DATE="2026-11-11" \
MOCK_NOW_VN_TIME="01:59" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1

assert_eq "State file đã được tạo sau lần dispatch đầu tiên" "true" "$([[ -f "${STATE_FILE}" ]] && echo "true" || echo "false")"

# 3. Test: Lúc 01:59 (chưa đến 02:00) -> Không kích hoạt backup
BUNDLES_BEFORE=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_1" \
MOCK_NOW_EPOCH="${NOW_EPOCH}" \
MOCK_NOW_VN_DATE="2026-11-11" \
MOCK_NOW_VN_TIME="01:59" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Không chạy backup khi chưa đến giờ hẹn 02:00" "${BUNDLES_BEFORE}" "${BUNDLES_AFTER}"

# 4. Test: Đúng 02:00 Asia/Ho_Chi_Minh -> Kích hoạt đúng 1 lần scheduled run
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" \
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_1" \
MOCK_NOW_EPOCH=$((NOW_EPOCH + 60)) \
MOCK_NOW_VN_DATE="2026-11-11" \
MOCK_NOW_VN_TIME="02:00" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_0200=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Kích hoạt backup thành công tại mốc 02:00" "$((BUNDLES_BEFORE + 1))" "${BUNDLES_AFTER_0200}"

# 5. Test: Lặp lại cùng mốc 02:00 trong cùng ngày -> Không chạy lần 2
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" \
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_1" \
MOCK_NOW_EPOCH=$((NOW_EPOCH + 90)) \
MOCK_NOW_VN_DATE="2026-11-11" \
MOCK_NOW_VN_TIME="02:00" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_REPEAT=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Không chạy lặp lại trong cùng ngày khi slot đã chạy" "${BUNDLES_AFTER_0200}" "${BUNDLES_AFTER_REPEAT}"

# 6. Test: MySQL khởi động lại khi lần backup thành công cuối đúng 24 giờ (86400s) -> Chưa đủ điều kiện, KHÔNG catch-up
LAST_SUCCESS_TIME=$(jq -r '.last_successful_backup_timestamp' "${STATE_FILE}")
EXACT_24H_EPOCH=$((LAST_SUCCESS_TIME + 86400))
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_2" \
MOCK_NOW_EPOCH="${EXACT_24H_EPOCH}" \
MOCK_NOW_VN_DATE="2026-11-12" \
MOCK_NOW_VN_TIME="10:00" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_EXACT_24H=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Đúng 24 giờ thì chưa đủ điều kiện catch-up (Q1)" "${BUNDLES_AFTER_REPEAT}" "${BUNDLES_AFTER_EXACT_24H}"

# 7. Test: MySQL khởi động lại khi lần thành công cuối ĐÃ > 24 giờ (86401s) -> Kích hoạt đúng 1 lần CATCH-UP
OVER_24H_EPOCH=$((LAST_SUCCESS_TIME + 86401))
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" \
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_3" \
MOCK_NOW_EPOCH="${OVER_24H_EPOCH}" \
MOCK_NOW_VN_DATE="2026-11-12" \
MOCK_NOW_VN_TIME="10:01" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_CATCHUP=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Hơn 24 giờ (>24h) thì kích hoạt catch-up thành công" "$((BUNDLES_AFTER_EXACT_24H + 1))" "${BUNDLES_AFTER_CATCHUP}"

# 8. Test: Poll tiếp theo trong cùng lần khởi động đó -> Không retry catch-up liên tục
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" \
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_3" \
MOCK_NOW_EPOCH=$((OVER_24H_EPOCH + 60)) \
MOCK_NOW_VN_DATE="2026-11-12" \
MOCK_NOW_VN_TIME="10:02" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_BURST_CHECK=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Không retry catch-up mỗi phút trong cùng một startup instance" "${BUNDLES_AFTER_CATCHUP}" "${BUNDLES_AFTER_BURST_CHECK}"

# 9. Test: Gộp mốc scheduled và catch-up nếu cùng xảy ra tại một thời điểm
# Giả lập last_success cách 30h trước, container mới startup lúc đúng 02:00
cat <<EOF > "${STATE_FILE}"
{
  "last_successful_backup_timestamp": $((NOW_EPOCH - 108000)),
  "last_scheduled_slot": "2026-11-10",
  "last_container_started_at": "old_startup",
  "last_catchup_startup_id": "old_startup",
  "last_poll_timestamp": $((NOW_EPOCH - 100))
}
EOF
BUNDLES_BEFORE_MERGE=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
MOCK_MYSQLDUMP_CMD="${MOCK_DUMP_VALID}" \
MOCK_CONTAINER_READY="true" \
MOCK_CONTAINER_STARTED_AT="startup_merged" \
MOCK_NOW_EPOCH="${NOW_EPOCH}" \
MOCK_NOW_VN_DATE="2026-11-11" \
MOCK_NOW_VN_TIME="02:00" \
"${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
BUNDLES_AFTER_MERGE=$(find "${BACKUPS_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | wc -l | tr -d ' ')
assert_eq "Khi cả scheduled và catch-up cùng đến thì gộp chạy đúng 1 lần" "$((BUNDLES_BEFORE_MERGE + 1))" "${BUNDLES_AFTER_MERGE}"

# 10. Test: State hỏng chứa timestamp ở tương lai -> fail-closed
cat <<EOF > "${STATE_FILE}"
{
  "last_successful_backup_timestamp": $((NOW_EPOCH + 1000000)),
  "last_scheduled_slot": "2026-11-11",
  "last_container_started_at": "startup_merged",
  "last_catchup_startup_id": "startup_merged",
  "last_poll_timestamp": ${NOW_EPOCH}
}
EOF
set +e
MOCK_CONTAINER_READY="true" "${DISPATCH_SCRIPT}" --config "${CONF_FILE}" >/dev/null 2>&1
FUTURE_CODE=$?
set -e
assert_eq "Timestamp ở tương lai thì phải từ chối fail-closed" "1" "${FUTURE_CODE}"

echo "Kết quả V4 Dispatcher Schedule & Catch-up: ${PASSED} đạt, ${FAILED} lỗi."
[[ ${FAILED} -eq 0 ]] || exit 1
