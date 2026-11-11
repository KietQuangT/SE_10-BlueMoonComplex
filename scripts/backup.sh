#!/usr/bin/env bash
# ==============================================================================
# BlueMoon S13 — Script Backup Cơ Sở Dữ Liệu Tự Động (Q1–Q5, NFR-008)
# ==============================================================================
# Bản quyền: Ban Quản Trị Chung Cư BlueMoon
# Quy tắc bảo mật:
# - Chạy với Bash 4+ / POSIX sh tương thích; set -euo pipefail.
# - Nhận cấu hình từ file bảo mật mode 0600 ngoài repository.
# - Tuyệt đối không nhận mật khẩu qua argv hoặc ghi vào stdout/stderr.
# - BACKUP_DIR bắt buộc là đường dẫn tuyệt đối trên ổ ngoài đã mã hóa.
# - Giữ đúng 7 bản backup thành công mới nhất; không tính bản dở/hỏng.
# - Loại bỏ SPRING_SESSION và SPRING_SESSION_ATTRIBUTES khỏi dump (Q4).
# - Manifest chỉ chứa timestamp, checksum SHA-256, số bảng/số dòng (Q5 allowlist).
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"

# ------------------------------------------------------------------------------
# 1. Tải và kiểm tra cấu hình
# ------------------------------------------------------------------------------
CONFIG_FILE="${BACKUP_CONF:-}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --config|-c)
            CONFIG_FILE="$2"
            shift 2
            ;;
        --help|-h)
            echo "Sử dụng: $0 --config /path/to/backup.conf"
            exit 0
            ;;
        *)
            echo "Lỗi: Tham số không hợp lệ: $1" >&2
            exit 1
            ;;
    esac
done

if [[ -z "${CONFIG_FILE}" ]]; then
    echo "Lỗi: Chưa chỉ định file cấu hình. Dùng --config <path> hoặc đặt BACKUP_CONF." >&2
    exit 1
fi

if [[ ! -f "${CONFIG_FILE}" ]]; then
    echo "Lỗi: File cấu hình không tồn tại: ${CONFIG_FILE}" >&2
    exit 1
fi

# Kiểm tra phân quyền file cấu hình (phải không cho nhóm khác đọc/ghi nếu trên hệ thống POSIX)
if [[ "$(uname -s)" != "CYGWIN"* && "$(uname -s)" != "MINGW"* ]]; then
    CONF_PERMS="$(stat -f "%OLp" "${CONFIG_FILE}" 2>/dev/null || stat -c "%a" "${CONFIG_FILE}" 2>/dev/null || true)"
    if [[ -n "${CONF_PERMS}" && "${CONF_PERMS}" =~ (..[1-7]|.[1-7].) ]]; then
        echo "Lỗi: Quyền file cấu hình (${CONF_PERMS}) quá mở. Bắt buộc chmod 0600 hoặc 0400." >&2
        exit 1
    fi
fi

# Tải cấu hình
# shellcheck source=/dev/null
source "${CONFIG_FILE}"

# ------------------------------------------------------------------------------
# 2. Kiểm tra các trường bắt buộc và Guard rails
# ------------------------------------------------------------------------------
: "${COMPOSE_FILE:?Thiếu COMPOSE_FILE trong cấu hình}"
: "${COMPOSE_PROJECT:?Thiếu COMPOSE_PROJECT trong cấu hình}"
: "${DB_SERVICE:=db}"
: "${DB_NAME:?Thiếu DB_NAME trong cấu hình}"
: "${DB_USER:?Thiếu DB_USER trong cấu hình}"
: "${DB_PASSWORD:?Thiếu DB_PASSWORD trong cấu hình}"
: "${BACKUP_DIR:?Thiếu BACKUP_DIR trong cấu hình}"
: "${BACKUP_MOUNT_POINT:?Thiếu BACKUP_MOUNT_POINT trong cấu hình}"
: "${REQUIRE_ENCRYPTION_VERIFIED:=false}"
: "${BACKUP_LOCK_FILE:=/var/run/bluemoon/backup.lock}"
: "${MOCK_MYSQLDUMP_CMD:=}"

# Q2: BACKUP_DIR bắt buộc là đường dẫn tuyệt đối
if [[ "${BACKUP_DIR}" != /* ]]; then
    echo "Lỗi: BACKUP_DIR phải là đường dẫn tuyệt đối trên ổ ngoài đã mã hóa: ${BACKUP_DIR}" >&2
    exit 1
fi

# Q2: Không được lưu trong repository
CANONICAL_REPO="${REPO_ROOT}"
# Tạo thư mục BACKUP_DIR nếu mount point tồn tại
if [[ ! -d "${BACKUP_MOUNT_POINT}" ]]; then
    echo "Lỗi: Ổ ngoài chưa được mount hoặc không tồn tại: ${BACKUP_MOUNT_POINT}" >&2
    exit 1
fi

mkdir -p "${BACKUP_DIR}"
CANONICAL_BACKUP_DIR="$(cd "${BACKUP_DIR}" && pwd -P)"

if [[ "${CANONICAL_BACKUP_DIR}" == "${CANONICAL_REPO}"* ]]; then
    echo "Lỗi: BACKUP_DIR không được nằm bên trong repository: ${BACKUP_DIR}" >&2
    exit 1
fi

# Không được nằm trong thư mục Docker volume/datadir
if [[ "${CANONICAL_BACKUP_DIR}" == *"/var/lib/mysql"* || "${CANONICAL_BACKUP_DIR}" == *"/mysql_data"* ]]; then
    echo "Lỗi: BACKUP_DIR không được nằm trong volume hoặc datadir của MySQL." >&2
    exit 1
fi

# Q2 & Q5: Xác minh mã hóa
if [[ "${REQUIRE_ENCRYPTION_VERIFIED}" != "true" ]]; then
    echo "Lỗi: Chưa xác nhận ổ ngoài đã mã hóa at-rest (REQUIRE_ENCRYPTION_VERIFIED!='true')." >&2
    exit 1
fi

# ------------------------------------------------------------------------------
# 3. Khóa nguyên tử chống chạy chồng nhau
# ------------------------------------------------------------------------------
LOCK_DIR="${BACKUP_LOCK_FILE}.d"
mkdir -p "$(dirname "${BACKUP_LOCK_FILE}")" 2>/dev/null || true

acquire_lock() {
    if mkdir "${LOCK_DIR}" 2>/dev/null; then
        echo "$$" > "${LOCK_DIR}/pid"
        return 0
    fi
    if [[ -f "${LOCK_DIR}/pid" ]]; then
        local old_pid
        old_pid="$(cat "${LOCK_DIR}/pid" 2>/dev/null || true)"
        if [[ "${old_pid}" == "$$" || "${old_pid}" == "${PPID:-}" ]]; then
            return 0
        fi
        if [[ -n "${old_pid}" ]] && ! kill -0 "${old_pid}" 2>/dev/null; then
            rm -rf "${LOCK_DIR}"
            if mkdir "${LOCK_DIR}" 2>/dev/null; then
                echo "$$" > "${LOCK_DIR}/pid"
                return 0
            fi
        fi
    fi
    return 1
}

release_lock() {
    if [[ -f "${LOCK_DIR}/pid" ]]; then
        local lock_pid
        lock_pid="$(cat "${LOCK_DIR}/pid" 2>/dev/null || true)"
        if [[ "${lock_pid}" == "$$" ]]; then
            rm -rf "${LOCK_DIR}" 2>/dev/null || true
        fi
    fi
}

if ! acquire_lock; then
    echo "Lỗi: Tiến trình backup/restore khác đang chạy hoặc giữ lock (${BACKUP_LOCK_FILE})." >&2
    exit 1
fi
trap release_lock EXIT INT TERM

# ------------------------------------------------------------------------------
# 4. Helper hàm tiện ích
# ------------------------------------------------------------------------------
compute_sha256() {
    local file="$1"
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "${file}" | awk '{print $1}'
    elif command -v shasum >/dev/null 2>&1; then
        shasum -a 256 "${file}" | awk '{print $1}'
    else
        echo "Lỗi: Cần công cụ sha256sum hoặc shasum." >&2
        return 1
    fi
}

utc_now() {
    date -u +"%Y-%m-%dT%H:%M:%SZ"
}

# ------------------------------------------------------------------------------
# 5. Preflight kiểm tra MySQL và Database (khi không chạy mock)
# ------------------------------------------------------------------------------
START_UTC="$(utc_now)"
RUN_ID="$(date -u +"%Y%m%d_%H%M%SZ")_${RANDOM}"

if [[ -z "${MOCK_MYSQLDUMP_CMD}" ]]; then
    # Kiểm tra Docker Compose file tồn tại
    if [[ ! -f "${COMPOSE_FILE}" ]]; then
        echo "Lỗi: COMPOSE_FILE không tồn tại: ${COMPOSE_FILE}" >&2
        exit 1
    fi

    # Kiểm tra container MySQL đang chạy
    if ! docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" ps --status running -q "${DB_SERVICE}" | grep -q .; then
        echo "Lỗi: Service MySQL '${DB_SERVICE}' không ở trạng thái running." >&2
        exit 1
    fi

    # Truy vấn kiểm tra phiên bản MySQL 8.4
    MYSQL_EXEC="docker compose -p ${COMPOSE_PROJECT} -f ${COMPOSE_FILE} exec -T ${DB_SERVICE} mysql -u${DB_USER} -p${DB_PASSWORD} -s -N"
    
    MYSQL_VERSION=$(${MYSQL_EXEC} -e "SELECT VERSION();" 2>/dev/null || true)
    if [[ "${MYSQL_VERSION}" != *"8.4"* ]]; then
        echo "Lỗi: Phiên bản MySQL không phải 8.4. Thực tế: ${MYSQL_VERSION}" >&2
        exit 1
    fi

    # Kiểm tra tất cả các bảng đều là InnoDB
    NON_INNODB_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}' AND engine != 'InnoDB';" 2>/dev/null || echo "0")
    if [[ "${NON_INNODB_COUNT}" != "0" ]]; then
        echo "Lỗi: Phát hiện ${NON_INNODB_COUNT} bảng không phải InnoDB trong schema ${DB_NAME}." >&2
        exit 1
    fi

    # Kiểm tra không có routine/trigger/view trỏ database khác ngoài thiết kế
    ROUTINE_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '${DB_NAME}';" 2>/dev/null || echo "0")
    TRIGGER_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = '${DB_NAME}';" 2>/dev/null || echo "0")
    VIEW_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.views WHERE table_schema = '${DB_NAME}';" 2>/dev/null || echo "0")
    if [[ "${ROUTINE_COUNT}" != "0" || "${TRIGGER_COUNT}" != "0" || "${VIEW_COUNT}" != "0" ]]; then
        echo "Lỗi: Phát hiện object ngoài thiết kế chuẩn (routines: ${ROUTINE_COUNT}, triggers: ${TRIGGER_COUNT}, views: ${VIEW_COUNT})." >&2
        exit 1
    fi
fi

# ------------------------------------------------------------------------------
# 6. Tạo Staging trực tiếp trên ổ ngoài mã hóa (mode 0700)
# ------------------------------------------------------------------------------
STAGING_DIR="${CANONICAL_BACKUP_DIR}/.staging_${RUN_ID}"
mkdir -p "${STAGING_DIR}"
chmod 0700 "${STAGING_DIR}"

DUMP_FILE="${STAGING_DIR}/dump.sql"
touch "${DUMP_FILE}"
chmod 0600 "${DUMP_FILE}"

cleanup_staging() {
    if [[ -d "${STAGING_DIR}" ]]; then
        rm -rf "${STAGING_DIR}" 2>/dev/null || true
    fi
}
trap 'cleanup_staging; release_lock' EXIT INT TERM

# ------------------------------------------------------------------------------
# 7. Thực hiện mysqldump stream vào staging
# ------------------------------------------------------------------------------
echo "Đang tiến hành mysqldump cho schema '${DB_NAME}'..." >&2

if [[ -n "${MOCK_MYSQLDUMP_CMD}" ]]; then
    # Hỗ trợ synthetic test runner
    eval "${MOCK_MYSQLDUMP_CMD}" > "${DUMP_FILE}"
else
    # Sử dụng mysqldump trong MySQL service qua docker compose exec
    # Cấu hình option an toàn theo Mục 5:
    # --single-transaction --quick --hex-blob --no-tablespaces --set-gtid-purged=OFF --skip-lock-tables
    # và loại bỏ SPRING_SESSION, SPRING_SESSION_ATTRIBUTES theo Q4
    docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T \
        -e MYSQL_PWD="${DB_PASSWORD}" "${DB_SERVICE}" \
        mysqldump -u"${DB_USER}" \
            --single-transaction \
            --quick \
            --hex-blob \
            --no-tablespaces \
            --set-gtid-purged=OFF \
            --skip-lock-tables \
            --ignore-table="${DB_NAME}.SPRING_SESSION" \
            --ignore-table="${DB_NAME}.SPRING_SESSION_ATTRIBUTES" \
            "${DB_NAME}" > "${DUMP_FILE}"
fi

# ------------------------------------------------------------------------------
# 8. Kiểm tra tính toàn vẹn và phạm vi của Dump
# ------------------------------------------------------------------------------
if [[ ! -s "${DUMP_FILE}" ]]; then
    echo "Lỗi: File dump rỗng hoặc tạo thất bại." >&2
    exit 1
fi

# Kiểm tra dump kết thúc hợp lệ
if ! grep -q "\-\- Dump completed" "${DUMP_FILE}" && ! grep -q "\-\- End of dump" "${DUMP_FILE}"; then
    echo "Lỗi: File dump bị cắt ngắn, thiếu dấu hoàn tất của mysqldump." >&2
    exit 1
fi

# Q4: Kiểm tra tuyệt đối không có bảng SPRING_SESSION trong dump
if grep -Ei "CREATE TABLE [\`\"']?SPRING_SESSION|INSERT INTO [\`\"']?SPRING_SESSION" "${DUMP_FILE}" >/dev/null; then
    echo "Lỗi vi phạm Q4: Phát hiện SPRING_SESSION trong tệp dump." >&2
    exit 1
fi

# Đếm số bảng và số dòng trong dump
TABLE_COUNT=$(grep -c "^CREATE TABLE " "${DUMP_FILE}" || echo "0")
TOTAL_ROW_COUNT=$(grep -c "^INSERT INTO " "${DUMP_FILE}" || echo "0")

if [[ "${TABLE_COUNT}" -eq 0 ]]; then
    echo "Lỗi: Không tìm thấy bảng nào trong dump." >&2
    exit 1
fi

# Tính checksum SHA-256
DUMP_SHA256="$(compute_sha256 "${DUMP_FILE}")"
echo "${DUMP_SHA256}  dump.sql" > "${STAGING_DIR}/dump.sha256"
chmod 0600 "${STAGING_DIR}/dump.sha256"

END_UTC="$(utc_now)"

# ------------------------------------------------------------------------------
# 9. Ghi Manifest theo Q5 Allowlist (Chỉ timestamp, checksum, số bảng, số dòng)
# ------------------------------------------------------------------------------
MANIFEST_FILE="${STAGING_DIR}/manifest.json"
cat <<EOF > "${MANIFEST_FILE}"
{
  "started_at_utc": "${START_UTC}",
  "completed_at_utc": "${END_UTC}",
  "sha256": "${DUMP_SHA256}",
  "table_count": ${TABLE_COUNT},
  "total_row_count": ${TOTAL_ROW_COUNT}
}
EOF
chmod 0600 "${MANIFEST_FILE}"

touch "${STAGING_DIR}/COMPLETED"
chmod 0600 "${STAGING_DIR}/COMPLETED"

# ------------------------------------------------------------------------------
# 10. Atomic Publish bản backup lên ổ ngoài mã hóa
# ------------------------------------------------------------------------------
FINAL_BUNDLE_NAME="bluemoon_backup_${RUN_ID}"
FINAL_BUNDLE_DIR="${CANONICAL_BACKUP_DIR}/${FINAL_BUNDLE_NAME}"

mv "${STAGING_DIR}" "${FINAL_BUNDLE_DIR}"
chmod 0700 "${FINAL_BUNDLE_DIR}"

# Cập nhật state file nếu có
if [[ -n "${DISPATCH_STATE_FILE:-}" ]]; then
    STATE_DIR="$(dirname "${DISPATCH_STATE_FILE}")"
    mkdir -p "${STATE_DIR}" 2>/dev/null || true
    cat <<EOF > "${DISPATCH_STATE_FILE}.tmp"
{
  "last_successful_backup_timestamp": "$(date -u +%s)",
  "last_successful_backup_utc": "${END_UTC}",
  "last_bundle": "${FINAL_BUNDLE_NAME}"
}
EOF
    chmod 0600 "${DISPATCH_STATE_FILE}.tmp"
    mv -f "${DISPATCH_STATE_FILE}.tmp" "${DISPATCH_STATE_FILE}"
fi

# ------------------------------------------------------------------------------
# 11. Xoay vòng giữ đúng 7 bản thành công mới nhất (Q3 Retention)
# ------------------------------------------------------------------------------
echo "Kiểm tra xoay vòng giữ 7 bản backup mới nhất..." >&2
RETENTION_COUNT=7

# Lấy danh sách các bundle hợp lệ đã COMPLETED, sắp xếp theo tên (chứa UTC timestamp)
VALID_BUNDLES=()
while IFS= read -r bundle_path; do
    if [[ -d "${bundle_path}" && -f "${bundle_path}/COMPLETED" ]]; then
        VALID_BUNDLES+=("${bundle_path}")
    fi
done < <(find "${CANONICAL_BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" | sort)

TOTAL_BUNDLES=${#VALID_BUNDLES[@]}
echo "Tổng số bản backup hợp lệ hiện có: ${TOTAL_BUNDLES}" >&2

if [[ ${TOTAL_BUNDLES} -gt ${RETENTION_COUNT} ]]; then
    EXCESS=$((TOTAL_BUNDLES - RETENTION_COUNT))
    echo "Cần xoay vòng xóa ${EXCESS} bản cũ nhất..." >&2
    for ((i=0; i<EXCESS; i++)); do
        OLD_BUNDLE="${VALID_BUNDLES[i]}"
        # Xác minh không xóa ngoài CANONICAL_BACKUP_DIR và không đi theo symlink
        if [[ -d "${OLD_BUNDLE}" && ! -L "${OLD_BUNDLE}" && "${OLD_BUNDLE}" == "${CANONICAL_BACKUP_DIR}/bluemoon_backup_"* ]]; then
            echo "Đang xoay vòng: $(basename "${OLD_BUNDLE}")" >&2
            rm -rf "${OLD_BUNDLE}" || echo "Cảnh báo: Không thể xóa ${OLD_BUNDLE}, giữ lại để bảo toàn dữ liệu." >&2
        fi
    done
fi

# In kết quả allowlist ra stdout (Q5: timestamp, sha256, table/row count)
echo "BACKUP_SUCCESS: started=${START_UTC} completed=${END_UTC} sha256=${DUMP_SHA256} tables=${TABLE_COUNT} rows=${TOTAL_ROW_COUNT}"
exit 0
