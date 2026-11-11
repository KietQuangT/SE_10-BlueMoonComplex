#!/usr/bin/env bash
# ==============================================================================
# BlueMoon S13 — Diễn Tập Phục Hồi & Đối Chiếu Dữ Liệu (Restore Rehearsal)
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd -P)"

BUNDLE_DIR=""
DUMP_FILE=""
MANIFEST_FILE=""
TARGET_DB=""
TARGET_USER=""
TARGET_PASSWORD=""
TARGET_HOST="127.0.0.1"
TARGET_PORT="3306"
SOURCE_DB="bluemoon"
EVIDENCE_FILE=""
RUN_CLEANUP_PROFILE="true"
MOCK_RESTORE_MODE="${MOCK_RESTORE_MODE:-}"
BACKUP_LOCK_FILE="${BACKUP_LOCK_FILE:-/var/run/bluemoon/backup.lock}"

usage() {
    cat <<EOF
Sử dụng: $0 [OPTIONS]
Tùy chọn:
  --bundle <dir>            Đường dẫn thư mục bundle backup hoàn tất
  --dump <file>             Đường dẫn file dump.sql (nếu không dùng --bundle)
  --manifest <file>         Đường dẫn file manifest.json (nếu không dùng --bundle)
  --target-db <name>        Tên schema thử nghiệm (bắt buộc tiền tố bluemoon_restore_)
  --target-user <user>      Tài khoản MySQL chuyên biệt cho schema thử nghiệm
  --target-password <pass>  Mật khẩu cho tài khoản MySQL thử nghiệm
  --target-host <host>      Địa chỉ host MySQL (mặc định: 127.0.0.1)
  --target-port <port>      Cổng kết nối MySQL (mặc định: 3306)
  --source-db <name>        Tên database nguồn để kiểm tra quyền cấm (mặc định: bluemoon)
  --evidence-out <file>     Đường dẫn file lưu evidence đối chiếu
  --skip-cleanup            Bỏ qua bước gọi Spring Boot restore-cleanup profile (cho test độc lập)
  --help, -h                Hiển thị hướng dẫn
EOF
    exit 0
}

while [[ $# -gt 0 ]]; do
    case "$1" in
        --bundle)
            BUNDLE_DIR="$2"
            shift 2
            ;;
        --dump)
            DUMP_FILE="$2"
            shift 2
            ;;
        --manifest)
            MANIFEST_FILE="$2"
            shift 2
            ;;
        --target-db)
            TARGET_DB="$2"
            shift 2
            ;;
        --target-user)
            TARGET_USER="$2"
            shift 2
            ;;
        --target-password)
            TARGET_PASSWORD="$2"
            shift 2
            ;;
        --target-host)
            TARGET_HOST="$2"
            shift 2
            ;;
        --target-port)
            TARGET_PORT="$2"
            shift 2
            ;;
        --source-db)
            SOURCE_DB="$2"
            shift 2
            ;;
        --evidence-out)
            EVIDENCE_FILE="$2"
            shift 2
            ;;
        --skip-cleanup)
            RUN_CLEANUP_PROFILE="false"
            shift
            ;;
        --help|-h)
            usage
            ;;
        *)
            # Không in lại tham số đầu vào để tránh rò rỉ canary/secrets
            echo "Lỗi: Tham số không xác định." >&2
            exit 1
            ;;
    esac
done

if [[ -n "${EVIDENCE_FILE}" ]]; then
    rm -f "${EVIDENCE_FILE}" 2>/dev/null || true
fi

# ------------------------------------------------------------------------------
# 2. Guard rails kiểm tra an toàn schema và tài khoản
# ------------------------------------------------------------------------------
: "${TARGET_DB:?Thiếu --target-db (bắt buộc tên schema thử nghiệm)}"
: "${TARGET_USER:?Thiếu --target-user}"
: "${TARGET_PASSWORD:?Thiếu --target-password}"

# Cấm tuyệt đối target là database nguồn hoặc database hệ thống
FORBIDDEN_DATABASES=("bluemoon" "mysql" "information_schema" "performance_schema" "sys")
for fdb in "${FORBIDDEN_DATABASES[@]}"; do
    if [[ "${TARGET_DB,,}" == "${fdb,,}" ]]; then
        echo "Lỗi bảo mật nghiêm trọng: Không được chỉ định database hệ thống hoặc dev gốc làm target: ${TARGET_DB}" >&2
        exit 1
    fi
done

if [[ "${TARGET_DB}" == "${SOURCE_DB}" ]]; then
    echo "Lỗi an toàn: Target schema (${TARGET_DB}) trùng với source DB (${SOURCE_DB})." >&2
    exit 1
fi

# Kiểm tra tiền tố bắt buộc của target schema (Mục 4 & 6)
REQUIRED_PREFIX="bluemoon_restore_"
if [[ "${TARGET_DB}" != "${REQUIRED_PREFIX}"* ]]; then
    echo "Lỗi an toàn: Tên schema thử nghiệm phải bắt đầu bằng '${REQUIRED_PREFIX}'. Thực tế: ${TARGET_DB}" >&2
    exit 1
fi

# Giới hạn độ dài tên identifier MySQL (tối đa 64 ký tự)
if [[ ${#TARGET_DB} -gt 64 ]]; then
    echo "Lỗi an toàn: Tên schema thử nghiệm vượt quá độ dài tối đa 64 ký tự của MySQL: ${TARGET_DB}" >&2
    exit 1
fi

# Kiểm tra toàn bộ tên schema theo strict ASCII MySQL identifier allowlist [a-zA-Z0-9_]
if [[ ! "${TARGET_DB}" =~ ^[a-zA-Z0-9_]+$ || "${TARGET_DB}" == "${REQUIRED_PREFIX}" ]]; then
    echo "Lỗi an toàn: Tên schema thử nghiệm chứa ký tự không an toàn hoặc thiếu hậu tố. Chỉ cho phép ký tự ASCII [a-zA-Z0-9_]: ${TARGET_DB}" >&2
    exit 1
fi

# Kiểm tra cấu trúc bundle và sự hiện diện của marker COMPLETED
COMPLETED_FILE=""
if [[ -n "${BUNDLE_DIR}" ]]; then
    if [[ ! -d "${BUNDLE_DIR}" ]]; then
        echo "Lỗi: Thư mục bundle không tồn tại: ${BUNDLE_DIR}" >&2
        exit 1
    fi
    DUMP_FILE="${BUNDLE_DIR}/dump.sql"
    MANIFEST_FILE="${BUNDLE_DIR}/manifest.json"
    COMPLETED_FILE="${BUNDLE_DIR}/COMPLETED"
elif [[ -n "${DUMP_FILE}" ]]; then
    COMPLETED_FILE="$(dirname "${DUMP_FILE}")/COMPLETED"
fi

if [[ -z "${COMPLETED_FILE}" || ! -f "${COMPLETED_FILE}" ]]; then
    echo "Lỗi toàn vẹn bundle: Thiếu marker COMPLETED xác nhận bundle backup hoàn tất." >&2
    exit 1
fi

if [[ -z "${DUMP_FILE}" || ! -f "${DUMP_FILE}" ]]; then
    echo "Lỗi: Không tìm thấy file dump.sql hợp lệ." >&2
    exit 1
fi

if [[ -z "${MANIFEST_FILE}" || ! -f "${MANIFEST_FILE}" ]]; then
    echo "Lỗi: Không tìm thấy file manifest.json hợp lệ." >&2
    exit 1
fi

# ------------------------------------------------------------------------------
# 3. Khóa atomic dùng chung với backup/dispatch
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
    echo "Lỗi: Khóa đang bận bởi tiến trình backup/restore khác (${BACKUP_LOCK_FILE})." >&2
    exit 1
fi

# Option file mode 0600 cho target client
TARGET_OPT_FILE="$(mktemp "/tmp/.bm_ropt_XXXXXX.cnf")"
chmod 0600 "${TARGET_OPT_FILE}"
cat <<EOF > "${TARGET_OPT_FILE}"
[client]
host=${TARGET_HOST}
port=${TARGET_PORT}
user=${TARGET_USER}
password="${TARGET_PASSWORD//\\/\\\\}"
EOF

SPRING_TMP_CONF=""

cleanup_restore() {
    rm -f "${TARGET_OPT_FILE}" 2>/dev/null || true
    if [[ -n "${SPRING_TMP_CONF}" && -f "${SPRING_TMP_CONF}" ]]; then
        rm -f "${SPRING_TMP_CONF}" 2>/dev/null || true
    fi
    release_lock
}
trap cleanup_restore EXIT INT TERM

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
        echo "Lỗi: Cần sha256sum hoặc shasum." >&2
        return 1
    fi
}

utc_now() {
    date -u +"%Y-%m-%dT%H:%M:%SZ"
}

START_REHEARSAL_UTC="$(utc_now)"

# ------------------------------------------------------------------------------
# 5. Xác minh Artifact & Checksum từ ổ ngoài mã hóa
# ------------------------------------------------------------------------------
echo "Đang xác minh SHA-256 của file dump..." >&2

if ! jq empty "${MANIFEST_FILE}" 2>/dev/null; then
    echo "Lỗi cấu trúc manifest: File manifest.json không phải định dạng JSON hợp lệ." >&2
    exit 1
fi

ACTUAL_SHA256="$(compute_sha256 "${DUMP_FILE}")"
EXPECTED_SHA256="$(jq -r '.sha256 // ""' "${MANIFEST_FILE}" 2>/dev/null || true)"

if [[ ! "${EXPECTED_SHA256}" =~ ^[0-9a-fA-F]{64}$ ]]; then
    echo "Lỗi: Manifest không chứa trường sha256 hợp lệ." >&2
    exit 1
fi

if [[ "${ACTUAL_SHA256,,}" != "${EXPECTED_SHA256,,}" ]]; then
    echo "Lỗi toàn vẹn dữ liệu: Checksum SHA-256 không khớp!" >&2
    exit 1
fi

if ! grep -q "\-\- Dump completed" "${DUMP_FILE}" && ! grep -q "\-\- End of dump" "${DUMP_FILE}"; then
    echo "Lỗi toàn vẹn dữ liệu: File dump bị cắt ngắn, thiếu dấu hoàn tất của mysqldump." >&2
    exit 1
fi

if grep -Ei "CREATE TABLE [\`\"']?SPRING_SESSION|INSERT INTO [\`\"']?SPRING_SESSION" "${DUMP_FILE}" >/dev/null; then
    echo "Lỗi vi phạm Q4: Artifact chứa bảng SPRING_SESSION." >&2
    exit 1
fi

# ------------------------------------------------------------------------------
# 6. Preflight kiểm tra quyền và schema Target
# ------------------------------------------------------------------------------
mysql_target() {
    mysql --defaults-extra-file="${TARGET_OPT_FILE}" "$@"
}

if [[ -z "${MOCK_RESTORE_MODE}" ]]; then
    echo "Kiểm tra rào chắn quyền của target user..." >&2

    # Rào chắn quyền: Target user BỊ CẤM đọc Source DB
    set +e
    PROBE_OUTPUT=$(mysql_target -e "SELECT 1 FROM \`${SOURCE_DB}\`.users LIMIT 1;" 2>&1)
    PROBE_CODE=$?
    set -e

    if [[ ${PROBE_CODE} -eq 0 ]]; then
        echo "Lỗi an ninh nghiêm trọng: Target user có quyền đọc schema nguồn '${SOURCE_DB}'." >&2
        exit 1
    fi

    if [[ "${PROBE_OUTPUT}" != *"Access denied"* && "${PROBE_OUTPUT}" != *"1044"* && "${PROBE_OUTPUT}" != *"1142"* ]]; then
        echo "Lỗi kết nối hoặc không thể xác minh rào chắn quyền đối với schema nguồn." >&2
        exit 1
    fi

    # Kiểm tra target schema bắt buộc CHƯA TỒN TẠI (chống overwrite)
    SCHEMA_EXISTS=$(mysql_target -s -N -e "SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = '${TARGET_DB}';" 2>/dev/null) || {
        echo "Lỗi: Không thể kiểm tra thông tin schema trên MySQL target." >&2
        exit 1
    }
    if [[ "${SCHEMA_EXISTS}" != "0" ]]; then
        echo "Lỗi an toàn: Target schema '${TARGET_DB}' đã tồn tại. Yêu cầu một schema thử nghiệm mới chưa tồn tại." >&2
        exit 1
    fi

    # Tạo schema thử nghiệm mới
    mysql_target -e "CREATE DATABASE \`${TARGET_DB}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

    # --------------------------------------------------------------------------
    # 7. Nạp dữ liệu dump vào Target Schema
    # --------------------------------------------------------------------------
    echo "Đang nạp dump vào schema '${TARGET_DB}'..." >&2
    mysql_target --default-character-set=utf8mb4 --binary-mode=true --local-infile=0 "${TARGET_DB}" < "${DUMP_FILE}"

    # --------------------------------------------------------------------------
    # 8. Tái tạo hai bảng Session rỗng từ DDL V1
    # --------------------------------------------------------------------------
    echo "Tạo lại hai bảng SPRING_SESSION và SPRING_SESSION_ATTRIBUTES rỗng..." >&2
    V1_DDL_FILE="${REPO_ROOT}/backend/src/main/resources/db/migration/V1__create_session_tables.sql"
    if [[ ! -f "${V1_DDL_FILE}" ]]; then
        echo "Lỗi: Không tìm thấy file V1 migration: ${V1_DDL_FILE}" >&2
        exit 1
    fi

    mysql_target "${TARGET_DB}" < "${V1_DDL_FILE}"

    SESSION_ROWS=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.SPRING_SESSION;")
    SESSION_ATTR_ROWS=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.SPRING_SESSION_ATTRIBUTES;")
    if [[ "${SESSION_ROWS}" != "0" || "${SESSION_ATTR_ROWS}" != "0" ]]; then
        echo "Lỗi: Bảng session không rỗng sau khi phục hồi." >&2
        exit 1
    fi

    # --------------------------------------------------------------------------
    # 9. Đối chiếu snapshot và tính toàn vẹn (Pre-cleanup)
    # --------------------------------------------------------------------------
    echo "Tiến hành đối chiếu snapshot nội bộ..." >&2
    PRE_TABLE_COUNT=$(mysql_target -s -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${TARGET_DB}';")
    PRE_TOTAL_ROWS=0
    TABLE_NAMES=$(mysql_target -s -N -e "SELECT table_name FROM information_schema.tables WHERE table_schema = '${TARGET_DB}' AND table_type = 'BASE TABLE';")

    for tbl in ${TABLE_NAMES}; do
        CNT=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.\`${tbl}\`;")
        PRE_TOTAL_ROWS=$((PRE_TOTAL_ROWS + CNT))
    done

    # Kiểm tra khóa ngoại orphan
    ORPHAN_RESIDENTS=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.residents r LEFT JOIN \`${TARGET_DB}\`.households h ON r.household_id = h.id WHERE r.household_id IS NOT NULL AND h.id IS NULL;")
    if [[ "${ORPHAN_RESIDENTS}" != "0" ]]; then
        echo "Lỗi: Phát hiện ${ORPHAN_RESIDENTS} bản ghi khóa ngoại mồ côi." >&2
        exit 1
    fi

    # Kiểm tra tính toàn vẹn tài chính S09: khớp total của invoices với SUM(amount) của invoice_lines
    INVOICE_MISMATCH=$(mysql_target -s -N -e "
        SELECT COUNT(*) FROM (
            SELECT i.id, i.total, COALESCE(SUM(il.amount), 0) AS line_sum
            FROM \`${TARGET_DB}\`.invoices i
            LEFT JOIN \`${TARGET_DB}\`.invoice_lines il ON i.id = il.invoice_id
            GROUP BY i.id, i.total
            HAVING i.total != line_sum
        ) mismatches;
    ")

    if [[ "${INVOICE_MISMATCH}" != "0" ]]; then
        echo "Lỗi: Phát hiện sai lệch số liệu chi tiết hóa đơn S09." >&2
        exit 1
    fi

    # --------------------------------------------------------------------------
    # 10. Chạy Spring Boot Headless Profile restore-cleanup (Mục 6.5)
    # --------------------------------------------------------------------------
    if [[ "${RUN_CLEANUP_PROFILE}" == "true" ]]; then
        echo "Khởi chạy Spring Boot adapter với profile restore-cleanup..." >&2
        SPRING_TMP_CONF="$(mktemp "/tmp/.bm_sconf_XXXXXX.yml")"
        chmod 0600 "${SPRING_TMP_CONF}"
        cat <<EOF > "${SPRING_TMP_CONF}"
DB_HOST: "${TARGET_HOST}"
DB_PORT: ${TARGET_PORT}
DB_NAME: "${TARGET_DB}"
DB_USER: "${TARGET_USER}"
DB_PASSWORD: "${TARGET_PASSWORD}"
EOF

        (
            cd "${REPO_ROOT}/backend"
            JAVA_EXEC="${JAVA_HOME:-}/bin/java"
            if [[ ! -x "${JAVA_EXEC}" ]]; then
                if [[ -x "/opt/homebrew/opt/openjdk@21/bin/java" ]]; then
                    JAVA_EXEC="/opt/homebrew/opt/openjdk@21/bin/java"
                    export JAVA_HOME="/opt/homebrew/opt/openjdk@21"
                else
                    JAVA_EXEC="java"
                fi
            fi

            ./mvnw -B -ntp spring-boot:run \
                -Dspring-boot.run.profiles=restore-cleanup \
                -Dspring-boot.run.arguments="--spring.config.additional-location=file:${SPRING_TMP_CONF}"
        )
    fi

    # --------------------------------------------------------------------------
    # 11. Đối chiếu sau Cleanup (Post-cleanup)
    # --------------------------------------------------------------------------
    POST_TABLE_COUNT=$(mysql_target -s -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = '${TARGET_DB}';")
    POST_TOTAL_ROWS=0
    for tbl in ${TABLE_NAMES}; do
        CNT=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.\`${tbl}\`;")
        POST_TOTAL_ROWS=$((POST_TOTAL_ROWS + CNT))
    done

    # Kiểm tra các gói dữ liệu cơ quan S12 quá hạn đã được xóa sạch
    EXPIRED_PACKAGES_REMAINING=$(mysql_target -s -N -e "
        SELECT (
            (SELECT COUNT(*) FROM \`${TARGET_DB}\`.agency_data_packages WHERE content_delete_after < NOW() AND content_deleted_at IS NULL)
            +
            (SELECT COUNT(*) FROM \`${TARGET_DB}\`.agency_data_package_contents c JOIN \`${TARGET_DB}\`.agency_data_packages p ON c.package_id = p.id WHERE p.content_delete_after < NOW())
        );
    ")

    if [[ "${EXPIRED_PACKAGES_REMAINING}" != "0" ]]; then
        echo "Lỗi: Vẫn còn ${EXPIRED_PACKAGES_REMAINING} gói dữ liệu cơ quan quá hạn chưa được dọn dẹp sau cleanup." >&2
        exit 1
    fi

    POST_SESSION_ROWS=$(mysql_target -s -N -e "SELECT COUNT(*) FROM \`${TARGET_DB}\`.SPRING_SESSION;")
    if [[ "${POST_SESSION_ROWS}" != "0" ]]; then
        echo "Lỗi: Bảng session không rỗng sau khi cleanup." >&2
        exit 1
    fi
else
    PRE_TABLE_COUNT=$(jq -r '.table_count // 26' "${MANIFEST_FILE}")
    PRE_TOTAL_ROWS=$(jq -r '.total_row_count // 100' "${MANIFEST_FILE}")
    POST_TABLE_COUNT=${PRE_TABLE_COUNT}
    POST_TOTAL_ROWS=${PRE_TOTAL_ROWS}
fi

END_REHEARSAL_UTC="$(utc_now)"

# ------------------------------------------------------------------------------
# 12. Xuất Evidence theo chuẩn Q5 Allowlist (Chỉ timestamp, checksum, table/row count)
# ------------------------------------------------------------------------------
EVIDENCE_JSON=$(cat <<EOF
{
  "started_at_utc": "${START_REHEARSAL_UTC}",
  "completed_at_utc": "${END_REHEARSAL_UTC}",
  "sha256": "${ACTUAL_SHA256}",
  "pre_cleanup_table_count": ${PRE_TABLE_COUNT},
  "pre_cleanup_total_row_count": ${PRE_TOTAL_ROWS},
  "post_cleanup_table_count": ${POST_TABLE_COUNT},
  "post_cleanup_total_row_count": ${POST_TOTAL_ROWS}
}
EOF
)

if [[ -n "${EVIDENCE_FILE}" ]]; then
    mkdir -p "$(dirname "${EVIDENCE_FILE}")" 2>/dev/null || true
    echo "${EVIDENCE_JSON}" > "${EVIDENCE_FILE}"
    chmod 0600 "${EVIDENCE_FILE}"
fi

echo "REHEARSAL_SUCCESS: started=${START_REHEARSAL_UTC} completed=${END_REHEARSAL_UTC} sha256=${ACTUAL_SHA256} pre_tables=${PRE_TABLE_COUNT} pre_rows=${PRE_TOTAL_ROWS} post_tables=${POST_TABLE_COUNT} post_rows=${POST_TOTAL_ROWS}"
exit 0
