#!/usr/bin/env bash
# ==============================================================================
# Tests: Backup Guard Rails (V1 - Tĩnh và Guard)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd -P)"
BACKUP_SCRIPT="${PROJECT_ROOT}/scripts/backup.sh"

TEST_TMP="$(mktemp -d /tmp/bluemoon_test_guards_XXXXXX)"
trap 'rm -rf "${TEST_TMP}"' EXIT

PASSED=0
FAILED=0

assert_fails_with() {
    local desc="$1"
    local conf="$2"
    local expected_msg="$3"
    
    local output
    set +e
    output=$("${BACKUP_SCRIPT}" --config "${conf}" 2>&1)
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

echo "=== Chạy kiểm tra Backup Guard Rails (V1) ==="

# 1. Test: Thiếu BACKUP_DIR
CONF_1="${TEST_TMP}/conf_no_backup_dir.conf"
cat <<EOF > "${CONF_1}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_MOUNT_POINT="/tmp"
REQUIRE_ENCRYPTION_VERIFIED="true"
EOF
chmod 0600 "${CONF_1}"
assert_fails_with "Thiếu BACKUP_DIR thì phải fail-closed" "${CONF_1}" "Thiếu BACKUP_DIR"

# 2. Test: BACKUP_DIR là đường dẫn tương đối
CONF_2="${TEST_TMP}/conf_rel_dir.conf"
cat <<EOF > "${CONF_2}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_DIR="relative/path/backups"
BACKUP_MOUNT_POINT="/tmp"
REQUIRE_ENCRYPTION_VERIFIED="true"
EOF
chmod 0600 "${CONF_2}"
assert_fails_with "BACKUP_DIR là đường dẫn tương đối thì phải từ chối" "${CONF_2}" "BACKUP_DIR phải là đường dẫn tuyệt đối"

# 3. Test: BACKUP_DIR nằm bên trong repository
CONF_3="${TEST_TMP}/conf_in_repo.conf"
cat <<EOF > "${CONF_3}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_DIR="${PROJECT_ROOT}/backups"
BACKUP_MOUNT_POINT="${PROJECT_ROOT}"
REQUIRE_ENCRYPTION_VERIFIED="true"
EOF
chmod 0600 "${CONF_3}"
assert_fails_with "BACKUP_DIR nằm trong repo thì phải từ chối" "${CONF_3}" "không được nằm bên trong repository"

# 4. Test: Mount point không tồn tại hoặc chưa mount
CONF_4="${TEST_TMP}/conf_unmounted.conf"
cat <<EOF > "${CONF_4}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_DIR="/nonexistent_mount_12345/backups"
BACKUP_MOUNT_POINT="/nonexistent_mount_12345"
REQUIRE_ENCRYPTION_VERIFIED="true"
EOF
chmod 0600 "${CONF_4}"
assert_fails_with "Ổ ngoài chưa mount thì phải fail-closed" "${CONF_4}" "Ổ ngoài chưa được mount hoặc không tồn tại"

# 5. Test: Chưa xác nhận mã hóa ổ ngoài
CONF_5="${TEST_TMP}/conf_unverified_enc.conf"
cat <<EOF > "${CONF_5}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_DIR="${TEST_TMP}/ext/backups"
BACKUP_MOUNT_POINT="${TEST_TMP}/ext"
REQUIRE_ENCRYPTION_VERIFIED="false"
EOF
mkdir -p "${TEST_TMP}/ext"
chmod 0600 "${CONF_5}"
assert_fails_with "Chưa xác nhận mã hóa (REQUIRE_ENCRYPTION_VERIFIED=false) thì phải từ chối" "${CONF_5}" "Chưa xác nhận ổ ngoài đã mã hóa"

# 6. Test: Chạy chồng lặp khi lock đang được giữ
CONF_6="${TEST_TMP}/conf_locked.conf"
LOCK_FILE="${TEST_TMP}/test.lock"
cat <<EOF > "${CONF_6}"
COMPOSE_FILE="/tmp/compose.yaml"
COMPOSE_PROJECT="bluemoon"
DB_NAME="bluemoon"
DB_USER="user"
DB_PASSWORD="pwd"
BACKUP_DIR="${TEST_TMP}/ext/backups"
BACKUP_MOUNT_POINT="${TEST_TMP}/ext"
REQUIRE_ENCRYPTION_VERIFIED="true"
BACKUP_LOCK_FILE="${LOCK_FILE}"
EOF
chmod 0600 "${CONF_6}"
mkdir -p "${LOCK_FILE}.d"
echo "$$" > "${LOCK_FILE}.d/pid"
assert_fails_with "Khóa atomic đang bận thì phải từ chối chạy chồng lặp" "${CONF_6}" "Tiến trình backup/restore khác đang chạy hoặc giữ lock"
rm -rf "${LOCK_FILE}.d"

echo "Kết quả V1 Guards: ${PASSED} đạt, ${FAILED} lỗi."
[[ ${FAILED} -eq 0 ]] || exit 1
