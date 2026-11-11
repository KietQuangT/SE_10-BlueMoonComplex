#!/usr/bin/env bash
# ==============================================================================
# BlueMoon S13 — Script Backup Cơ Sở Dữ Liệu Tự Động (Q1–Q5, NFR-008)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"

# ------------------------------------------------------------------------------
# 1. Tải và kiểm tra cấu hình an toàn trước khi source
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
            # Không in lại tham số đầu vào để tránh lộ giá trị nhạy cảm (canary)
            echo "Lỗi: Tham số không hợp lệ." >&2
            exit 1
            ;;
    esac
done

if [[ -z "${CONFIG_FILE}" ]]; then
    echo "Lỗi: Chưa chỉ định file cấu hình. Dùng --config <path> hoặc đặt BACKUP_CONF." >&2
    exit 1
fi

if [[ ! -f "${CONFIG_FILE}" ]]; then
    echo "Lỗi: File cấu hình không tồn tại." >&2
    exit 1
fi

CANONICAL_CONF="$(cd "$(dirname "${CONFIG_FILE}")" && pwd -P)/$(basename "${CONFIG_FILE}")"

# File cấu hình không được nằm trong repo
if [[ "${CANONICAL_CONF}" == "${REPO_ROOT}"* ]]; then
    echo "Lỗi: File cấu hình không được nằm bên trong repository." >&2
    exit 1
fi

# Kiểm tra phân quyền file cấu hình (phải là 0600 hoặc 0400, không cho group/other đọc)
if [[ "$(uname -s)" != "CYGWIN"* && "$(uname -s)" != "MINGW"* ]]; then
    CONF_PERMS="$(stat -f "%OLp" "${CONFIG_FILE}" 2>/dev/null || stat -c "%a" "${CONFIG_FILE}" 2>/dev/null || true)"
    if [[ -n "${CONF_PERMS}" && "${CONF_PERMS}" =~ (..[1-7]|.[1-7].) ]]; then
        echo "Lỗi: Quyền file cấu hình (${CONF_PERMS}) quá mở. Bắt buộc chmod 0600 hoặc 0400." >&2
        exit 1
    fi
fi

# Tải cấu hình an toàn sau khi đã kiểm tra vị trí và quyền
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

# Q2 & Q5: Xác minh mount point tồn tại
if [[ ! -d "${BACKUP_MOUNT_POINT}" ]]; then
    echo "Lỗi: Ổ ngoài chưa được mount hoặc không tồn tại: ${BACKUP_MOUNT_POINT}" >&2
    exit 1
fi

# Q2 & Q5: Bắt buộc đã xác nhận mã hóa ổ ngoài trước khi tạo bất kỳ thư mục nào
if [[ "${REQUIRE_ENCRYPTION_VERIFIED}" != "true" ]]; then
    echo "Lỗi: Chưa xác nhận ổ ngoài đã mã hóa at-rest (REQUIRE_ENCRYPTION_VERIFIED!='true')." >&2
    exit 1
fi

# Kiểm tra đường dẫn BACKUP_DIR không nằm trong repo hoặc volume/datadir
CANONICAL_REPO="${REPO_ROOT}"
# Chỉ tạo thư mục sau khi đã xác nhận mã hóa và mount point
mkdir -p "${BACKUP_DIR}"
CANONICAL_BACKUP_DIR="$(cd "${BACKUP_DIR}" && pwd -P)"

if [[ "${CANONICAL_BACKUP_DIR}" == "${CANONICAL_REPO}"* ]]; then
    echo "Lỗi: BACKUP_DIR không được nằm bên trong repository: ${BACKUP_DIR}" >&2
    exit 1
fi

if [[ "${CANONICAL_BACKUP_DIR}" == *"/var/lib/mysql"* || "${CANONICAL_BACKUP_DIR}" == *"/mysql_data"* ]]; then
    echo "Lỗi: BACKUP_DIR không được nằm trong volume hoặc datadir của MySQL." >&2
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
        # Cho phép re-entry từ chính tiến trình hoặc parent dispatcher
        if [[ "${old_pid}" == "$$" || "${old_pid}" == "${PPID:-}" ]]; then
            return 0
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

# Tạo option file tạm thời mode 0600 để tránh truyền credential trên argv/env
OPT_FILE="$(mktemp "/tmp/.bm_bopt_XXXXXX.cnf")"
chmod 0600 "${OPT_FILE}"
cat <<EOF > "${OPT_FILE}"
[client]
user=${DB_USER}
password="${DB_PASSWORD//\\/\\\\}"
EOF

cleanup_all() {
    rm -f "${OPT_FILE}" 2>/dev/null || true
    if [[ -n "${STAGING_DIR:-}" && -d "${STAGING_DIR}" ]]; then
        rm -rf "${STAGING_DIR}" 2>/dev/null || true
    fi
    release_lock
}
trap cleanup_all EXIT INT TERM

# ------------------------------------------------------------------------------
# 5. Preflight kiểm tra MySQL và Database (khi không chạy mock)
# ------------------------------------------------------------------------------
START_UTC="$(utc_now)"
RUN_ID="$(date -u +"%Y%m%d_%H%M%SZ")_${RANDOM}"

if [[ -z "${MOCK_MYSQLDUMP_CMD}" ]]; then
    if [[ ! -f "${COMPOSE_FILE}" ]]; then
        echo "Lỗi: COMPOSE_FILE không tồn tại: ${COMPOSE_FILE}" >&2
        exit 1
    fi

    if ! docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" ps --status running -q "${DB_SERVICE}" | grep -q .; then
        echo "Lỗi: Service MySQL '${DB_SERVICE}' không ở trạng thái running." >&2
        exit 1
    fi

    # Sao chép option file an toàn vào container để thực hiện truy vấn kiểm tra
    docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" cp "${OPT_FILE}" "${DB_SERVICE}:/tmp/.opt_$$.cnf"
    docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" chmod 0600 "/tmp/.opt_$$.cnf"

    MYSQL_EXEC="docker compose -p ${COMPOSE_PROJECT} -f ${COMPOSE_FILE} exec -T ${DB_SERVICE} mysql --defaults-extra-file=/tmp/.opt_$$.cnf -s -N"

    MYSQL_VERSION=$(${MYSQL_EXEC} -e "SELECT VERSION();" 2>/dev/null) || {
        echo "Lỗi: Không thể kết nối hoặc xác minh phiên bản MySQL." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    }

    if [[ "${MYSQL_VERSION}" != *"8.4"* ]]; then
        echo "Lỗi: Phiên bản MySQL không phải 8.4." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    fi

    NON_INNODB_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${DB_NAME}' AND engine != 'InnoDB';" 2>/dev/null) || {
        echo "Lỗi: Truy vấn kiểm tra engine InnoDB thất bại." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    }
    if [[ "${NON_INNODB_COUNT}" != "0" ]]; then
        echo "Lỗi: Phát hiện bảng không phải InnoDB trong schema." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    fi

    ROUTINE_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.routines WHERE routine_schema = '${DB_NAME}';" 2>/dev/null) || {
        echo "Lỗi: Truy vấn kiểm tra routines thất bại." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    }
    TRIGGER_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.triggers WHERE trigger_schema = '${DB_NAME}';" 2>/dev/null) || {
        echo "Lỗi: Truy vấn kiểm tra triggers thất bại." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    }
    VIEW_COUNT=$(${MYSQL_EXEC} -e "SELECT COUNT(*) FROM information_schema.views WHERE table_schema = '${DB_NAME}';" 2>/dev/null) || {
        echo "Lỗi: Truy vấn kiểm tra views thất bại." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
        exit 1
    }

    if [[ "${ROUTINE_COUNT}" != "0" || "${TRIGGER_COUNT}" != "0" || "${VIEW_COUNT}" != "0" ]]; then
        echo "Lỗi: Phát hiện object ngoài thiết kế chuẩn." >&2
        docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
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

# ------------------------------------------------------------------------------
# 7. Thực hiện mysqldump stream vào staging
# ------------------------------------------------------------------------------
echo "Đang tiến hành mysqldump cho schema '${DB_NAME}'..." >&2

if [[ -n "${MOCK_MYSQLDUMP_CMD}" ]]; then
    eval "${MOCK_MYSQLDUMP_CMD}" > "${DUMP_FILE}"
else
    docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T \
        "${DB_SERVICE}" \
        mysqldump --defaults-extra-file="/tmp/.opt_$$.cnf" \
            --single-transaction \
            --quick \
            --hex-blob \
            --no-tablespaces \
            --set-gtid-purged=OFF \
            --skip-lock-tables \
            --ignore-table="${DB_NAME}.SPRING_SESSION" \
            --ignore-table="${DB_NAME}.SPRING_SESSION_ATTRIBUTES" \
            "${DB_NAME}" > "${DUMP_FILE}"

    # Dọn dẹp option file trong container ngay sau dump
    docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" exec -T "${DB_SERVICE}" rm -f "/tmp/.opt_$$.cnf" 2>/dev/null || true
fi

# ------------------------------------------------------------------------------
# 8. Kiểm tra tính toàn vẹn và phạm vi của Dump
# ------------------------------------------------------------------------------
if [[ ! -s "${DUMP_FILE}" ]]; then
    echo "Lỗi: File dump rỗng hoặc tạo thất bại." >&2
    exit 1
fi

if ! grep -q "\-\- Dump completed" "${DUMP_FILE}" && ! grep -q "\-\- End of dump" "${DUMP_FILE}"; then
    echo "Lỗi: File dump bị cắt ngắn, thiếu dấu hoàn tất của mysqldump." >&2
    exit 1
fi

if grep -Ei "CREATE TABLE [\`\"']?SPRING_SESSION|INSERT INTO [\`\"']?SPRING_SESSION" "${DUMP_FILE}" >/dev/null; then
    echo "Lỗi vi phạm Q4: Phát hiện SPRING_SESSION trong tệp dump." >&2
    exit 1
fi

TABLE_COUNT=$(grep -c "^CREATE TABLE " "${DUMP_FILE}" || echo "0")
TOTAL_ROW_COUNT=$(grep -c "^INSERT INTO " "${DUMP_FILE}" || echo "0")

if [[ "${TABLE_COUNT}" -eq 0 ]]; then
    echo "Lỗi: Không tìm thấy bảng nào trong dump." >&2
    exit 1
fi

DUMP_SHA256="$(compute_sha256 "${DUMP_FILE}")"
echo "${DUMP_SHA256}  dump.sql" > "${STAGING_DIR}/dump.sha256"
chmod 0600 "${STAGING_DIR}/dump.sha256"

END_UTC="$(utc_now)"

# ------------------------------------------------------------------------------
# 9. Ghi Manifest theo chuẩn Q5 Allowlist
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

# Đọc và xác minh tính toàn vẹn của từng bundle trước khi đưa vào danh sách hợp lệ
VALID_BUNDLES=()
while IFS= read -r bundle_path; do
    if [[ -d "${bundle_path}" && -f "${bundle_path}/COMPLETED" && -f "${bundle_path}/dump.sql" && -f "${bundle_path}/manifest.json" ]]; then
        # Kiểm tra checksum sha256 hợp lệ
        B_SHA="$(compute_sha256 "${bundle_path}/dump.sql" 2>/dev/null || true)"
        M_SHA="$(jq -r '.sha256 // ""' "${bundle_path}/manifest.json" 2>/dev/null || true)"
        if [[ -n "${B_SHA}" && "${B_SHA}" == "${M_SHA}" ]]; then
            B_TIME="$(jq -r '.completed_at_utc // ""' "${bundle_path}/manifest.json" 2>/dev/null || true)"
            VALID_BUNDLES+=("${B_TIME}|${bundle_path}")
        fi
    fi
done < <(find "${CANONICAL_BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" 2>/dev/null)

# Sắp xếp theo completed_at_utc tăng dần
IFS=$'\n' SORTED_BUNDLES=($(sort <<<"${VALID_BUNDLES[*]:-}"))
unset IFS

TOTAL_BUNDLES=${#SORTED_BUNDLES[@]}
echo "Tổng số bản backup hợp lệ đã xác minh: ${TOTAL_BUNDLES}" >&2

PRUNE_ERROR=false
if [[ ${TOTAL_BUNDLES} -gt ${RETENTION_COUNT} ]]; then
    EXCESS=$((TOTAL_BUNDLES - RETENTION_COUNT))
    echo "Cần xoay vòng xóa ${EXCESS} bản cũ nhất..." >&2
    for ((i=0; i<EXCESS; i++)); do
        OLD_ENTRY="${SORTED_BUNDLES[i]}"
        OLD_BUNDLE="${OLD_ENTRY#*|}"
        if [[ -d "${OLD_BUNDLE}" && ! -L "${OLD_BUNDLE}" && "${OLD_BUNDLE}" == "${CANONICAL_BACKUP_DIR}/bluemoon_backup_"* ]]; then
            echo "Đang xoay vòng: $(basename "${OLD_BUNDLE}")" >&2
            if ! rm -rf "${OLD_BUNDLE}"; then
                echo "Lỗi: Không thể xóa ${OLD_BUNDLE} trong quá trình prune." >&2
                PRUNE_ERROR=true
                break
            fi
        fi
    done
fi

if [[ "${PRUNE_ERROR}" == "true" ]]; then
    echo "Lỗi: Quá trình xoay vòng bản backup gặp lỗi, bảo toàn các bản còn lại." >&2
    exit 1
fi

# In kết quả allowlist ra stdout (Q5: timestamp, sha256, table/row count)
echo "BACKUP_SUCCESS: started=${START_UTC} completed=${END_UTC} sha256=${DUMP_SHA256} tables=${TABLE_COUNT} rows=${TOTAL_ROW_COUNT}"
exit 0
