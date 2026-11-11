#!/usr/bin/env bash
# ==============================================================================
# Tests: Restore Rehearsal Guard Rails (V1 & V2 - Phục hồi & Đối chiếu)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd -P)"
RESTORE_SCRIPT="${PROJECT_ROOT}/scripts/restore-rehearsal.sh"

TEST_TMP="$(mktemp -d /tmp/bluemoon_test_restore_guards_XXXXXX)"
trap 'rm -rf "${TEST_TMP}"' EXIT

export BACKUP_LOCK_FILE="${TEST_TMP}/backup.lock"

PASSED=0
FAILED=0

assert_fails_with() {
    local desc="$1"
    local expected_msg="$2"
    shift 2

    local output
    set +e
    output=$("${RESTORE_SCRIPT}" "$@" 2>&1)
    local code=$?
    set -e

    if [[ ${code} -ne 0 && "${output}" == *"${expected_msg}"* ]]; then
        echo "  [PASS] ${desc}"
        PASSED=$((PASSED + 1))
    else
        echo "  [FAIL] ${desc}"
        echo "    Expected error containing: '${expected_msg}'"
        echo "    Actual exit code: ${code}, output: ${output}"
        FAILED=$((FAILED + 1))
    fi
}

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

echo "=== Chạy kiểm tra Restore Rehearsal Guards (V1 & V2) ==="

BUNDLE_DIR="${TEST_TMP}/bundle_valid"
mkdir -p "${BUNDLE_DIR}"
DUMP_FILE="${BUNDLE_DIR}/dump.sql"
cat <<EOF > "${DUMP_FILE}"
-- MySQL dump 8.4
CREATE TABLE \`users\` (id INT);
INSERT INTO \`users\` VALUES (1);
-- Dump completed on 2026-11-11 02:00:00
EOF

if command -v sha256sum >/dev/null 2>&1; then
    DUMP_SHA256="$(sha256sum "${DUMP_FILE}" | awk '{print $1}')"
else
    DUMP_SHA256="$(shasum -a 256 "${DUMP_FILE}" | awk '{print $1}')"
fi

cat <<EOF > "${BUNDLE_DIR}/manifest.json"
{
  "started_at_utc": "2026-11-11T02:00:00Z",
  "completed_at_utc": "2026-11-11T02:00:05Z",
  "sha256": "${DUMP_SHA256}",
  "table_count": 1,
  "total_row_count": 1
}
EOF

# 1. Test: Tên target schema không có tiền tố bluemoon_restore_ -> Từ chối
assert_fails_with "Target DB không có tiền tố bluemoon_restore_ phải bị từ chối" \
    "Tên schema thử nghiệm phải bắt đầu bằng 'bluemoon_restore_'" \
    --bundle "${BUNDLE_DIR}" \
    --target-db "custom_restore_db" \
    --target-user "test_u" \
    --target-password "test_p"

# 2. Test: Target DB trùng với database dev gốc (bluemoon) -> Từ chối nghiêm ngặt
assert_fails_with "Target DB là bluemoon (dev gốc) phải bị từ chối nghiêm ngặt" \
    "Không được chỉ định database hệ thống hoặc dev gốc" \
    --bundle "${BUNDLE_DIR}" \
    --target-db "bluemoon" \
    --target-user "test_u" \
    --target-password "test_p"

# 3. Test: Target DB là database hệ thống (mysql, sys, ...) -> Từ chối
assert_fails_with "Target DB là database hệ thống mysql phải bị từ chối" \
    "Không được chỉ định database hệ thống hoặc dev gốc" \
    --bundle "${BUNDLE_DIR}" \
    --target-db "mysql" \
    --target-user "test_u" \
    --target-password "test_p"

# 4. Test: Checksum SHA-256 bị sai lệch -> Từ chối
BUNDLE_CORRUPT="${TEST_TMP}/bundle_corrupt"
mkdir -p "${BUNDLE_CORRUPT}"
cp "${BUNDLE_DIR}/dump.sql" "${BUNDLE_CORRUPT}/dump.sql"
cat <<EOF > "${BUNDLE_CORRUPT}/manifest.json"
{
  "sha256": "0000000000000000000000000000000000000000000000000000000000000000"
}
EOF
assert_fails_with "Checksum SHA-256 không khớp phải báo lỗi và dừng ngay" \
    "Checksum SHA-256 không khớp" \
    --bundle "${BUNDLE_CORRUPT}" \
    --target-db "bluemoon_restore_s13_test" \
    --target-user "test_u" \
    --target-password "test_p"

# 5. Test: Artifact chứa SPRING_SESSION -> Từ chối
BUNDLE_LEAK="${TEST_TMP}/bundle_leak"
mkdir -p "${BUNDLE_LEAK}"
cat <<EOF > "${BUNDLE_LEAK}/dump.sql"
-- MySQL dump 8.4
CREATE TABLE \`SPRING_SESSION\` (primary_id VARCHAR(36));
-- Dump completed
EOF
if command -v sha256sum >/dev/null 2>&1; then
    LEAK_SHA256="$(sha256sum "${BUNDLE_LEAK}/dump.sql" | awk '{print $1}')"
else
    LEAK_SHA256="$(shasum -a 256 "${BUNDLE_LEAK}/dump.sql" | awk '{print $1}')"
fi
cat <<EOF > "${BUNDLE_LEAK}/manifest.json"
{
  "sha256": "${LEAK_SHA256}"
}
EOF
assert_fails_with "Artifact chứa SPRING_SESSION phải bị từ chối nạp" \
    "Artifact chứa bảng SPRING_SESSION" \
    --bundle "${BUNDLE_LEAK}" \
    --target-db "bluemoon_restore_s13_test" \
    --target-user "test_u" \
    --target-password "test_p"

# 6. Test: Diễn tập thành công ở chế độ mock restore và xuất evidence Q5
EVIDENCE_OUT="${TEST_TMP}/evidence.json"
MOCK_RESTORE_MODE="true" "${RESTORE_SCRIPT}" \
    --bundle "${BUNDLE_DIR}" \
    --target-db "bluemoon_restore_s13_test" \
    --target-user "test_u" \
    --target-password "test_p" \
    --evidence-out "${EVIDENCE_OUT}" \
    --skip-cleanup >/dev/null 2>&1

assert_eq "File evidence.json được tạo thành công" "true" "$([[ -f "${EVIDENCE_OUT}" ]] && echo "true" || echo "false")"

HAS_SHA256=$(jq 'has("sha256")' "${EVIDENCE_OUT}")
HAS_PRE_TABLES=$(jq 'has("pre_cleanup_table_count")' "${EVIDENCE_OUT}")
HAS_PRE_ROWS=$(jq 'has("pre_cleanup_total_row_count")' "${EVIDENCE_OUT}")
HAS_TARGET_SCHEMA=$(jq 'has("target_schema")' "${EVIDENCE_OUT}")
HAS_VERDICT=$(jq 'has("verdict")' "${EVIDENCE_OUT}")
HAS_PII=$(jq 'has("user") or has("password") or has("sql") or has("raw_data")' "${EVIDENCE_OUT}")

assert_eq "Evidence có sha256" "true" "${HAS_SHA256}"
assert_eq "Evidence có pre_cleanup_table_count" "true" "${HAS_PRE_TABLES}"
assert_eq "Evidence có pre_cleanup_total_row_count" "true" "${HAS_PRE_ROWS}"
assert_eq "Evidence không có target_schema (chuẩn Q5)" "false" "${HAS_TARGET_SCHEMA}"
assert_eq "Evidence không có verdict (chuẩn Q5)" "false" "${HAS_VERDICT}"
assert_eq "Evidence tuyệt đối không chứa PII hoặc dữ liệu thô" "false" "${HAS_PII}"

echo "Kết quả Restore Rehearsal Guards: ${PASSED} đạt, ${FAILED} lỗi."
[[ ${FAILED} -eq 0 ]] || exit 1
