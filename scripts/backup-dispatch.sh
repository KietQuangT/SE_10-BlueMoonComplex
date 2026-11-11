#!/usr/bin/env bash
# ==============================================================================
# BlueMoon S13 — Script Điều Phối Backup Định Kỳ & Catch-up (Q1, Section 4)
# ==============================================================================
# Lịch chạy: Gọi mỗi 60 giây bởi native host scheduler (systemd timer / launchd / cron)
# Quy tắc:
# - Tính lịch theo múi giờ Asia/Ho_Chi_Minh (02:00 hằng ngày).
# - Nếu MySQL không sẵn sàng lúc 02:00, thực hiện catch-up ĐÚNG MỘT LẦN ở lần
#   stack khởi động/sẵn sàng tiếp theo nếu lần backup thành công cuối > 24 giờ.
#   (Đúng 24 giờ thì chưa đủ điều kiện).
# - Gộp lịch trùng (scheduled và catch-up cùng đến chỉ chạy 1 lần).
# - Không retry liên tục mỗi phút trong cùng một lần khởi động nếu backup lỗi.
# - Không bịa đặt timestamp; giữ nguyên mốc thành công cũ khi lỗi.
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

# shellcheck source=/dev/null
source "${CONFIG_FILE}"

: "${DISPATCH_STATE_FILE:?Thiếu DISPATCH_STATE_FILE trong cấu hình}"
: "${BACKUP_LOCK_FILE:=/var/run/bluemoon/backup.lock}"
: "${COMPOSE_FILE:?Thiếu COMPOSE_FILE trong cấu hình}"
: "${COMPOSE_PROJECT:?Thiếu COMPOSE_PROJECT trong cấu hình}"
: "${DB_SERVICE:=db}"

# Mock variables cho testing
MOCK_NOW_EPOCH="${MOCK_NOW_EPOCH:-}"
MOCK_NOW_VN_DATE="${MOCK_NOW_VN_DATE:-}"
MOCK_NOW_VN_TIME="${MOCK_NOW_VN_TIME:-}"
MOCK_CONTAINER_READY="${MOCK_CONTAINER_READY:-}"
MOCK_CONTAINER_STARTED_AT="${MOCK_CONTAINER_STARTED_AT:-}"
export MOCK_MYSQLDUMP_CMD="${MOCK_MYSQLDUMP_CMD:-}"

# ------------------------------------------------------------------------------
# 2. Khóa chống chạy chồng lặp (dùng chung với backup.sh)
# ------------------------------------------------------------------------------
LOCK_DIR="${BACKUP_LOCK_FILE}.d"

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
    # Đang có backup chạy, thoát êm (exit 0) để không xung đột
    exit 0
fi
trap release_lock EXIT INT TERM

# ------------------------------------------------------------------------------
# 3. Tính toán thời gian và múi giờ Asia/Ho_Chi_Minh
# ------------------------------------------------------------------------------
if [[ -n "${MOCK_NOW_EPOCH}" ]]; then
    CURRENT_EPOCH="${MOCK_NOW_EPOCH}"
else
    CURRENT_EPOCH="$(date +%s)"
fi

if [[ -n "${MOCK_NOW_VN_DATE}" ]]; then
    CURRENT_DATE_VN="${MOCK_NOW_VN_DATE}"
else
    CURRENT_DATE_VN="$(TZ="Asia/Ho_Chi_Minh" date +"%Y-%m-%d")"
fi

if [[ -n "${MOCK_NOW_VN_TIME}" ]]; then
    CURRENT_TIME_VN="${MOCK_NOW_VN_TIME}"
else
    CURRENT_TIME_VN="$(TZ="Asia/Ho_Chi_Minh" date +"%H:%M")"
fi

# ------------------------------------------------------------------------------
# 4. Đọc và kiểm tra State
# ------------------------------------------------------------------------------
mkdir -p "$(dirname "${DISPATCH_STATE_FILE}")" 2>/dev/null || true

LAST_SUCCESS_EPOCH=0
LAST_SCHEDULED_SLOT=""
LAST_CONTAINER_STARTED_AT=""
LAST_CATCHUP_STARTUP_ID=""
LAST_POLL_EPOCH=0

if [[ -f "${DISPATCH_STATE_FILE}" ]]; then
    # Đọc state từ file JSON
    LAST_SUCCESS_EPOCH="$(jq -r '.last_successful_backup_timestamp // 0' "${DISPATCH_STATE_FILE}" 2>/dev/null || echo "0")"
    LAST_SCHEDULED_SLOT="$(jq -r '.last_scheduled_slot // ""' "${DISPATCH_STATE_FILE}" 2>/dev/null || echo "")"
    LAST_CONTAINER_STARTED_AT="$(jq -r '.last_container_started_at // ""' "${DISPATCH_STATE_FILE}" 2>/dev/null || echo "")"
    LAST_CATCHUP_STARTUP_ID="$(jq -r '.last_catchup_startup_id // ""' "${DISPATCH_STATE_FILE}" 2>/dev/null || echo "")"
    LAST_POLL_EPOCH="$(jq -r '.last_poll_timestamp // 0' "${DISPATCH_STATE_FILE}" 2>/dev/null || echo "0")"

    # Kiểm tra tính hợp lệ của timestamp: không được ở tương lai quá 300 giây
    if [[ "${LAST_SUCCESS_EPOCH}" -gt $((CURRENT_EPOCH + 300)) ]]; then
        echo "Lỗi: State file bị hỏng hoặc chứa timestamp ở tương lai (> current_time + 300s)." >&2
        exit 1
    fi
else
    # Kiểm tra xem đã có backup thành công nào trong BACKUP_DIR chưa
    INITIAL_BUNDLE=""
    if [[ -d "${BACKUP_DIR:-}" ]]; then
        INITIAL_BUNDLE="$(find "${BACKUP_DIR}" -mindepth 1 -maxdepth 1 -type d -name "bluemoon_backup_*" 2>/dev/null | sort | tail -n 1 || true)"
    fi

    if [[ -n "${INITIAL_BUNDLE}" && -f "${INITIAL_BUNDLE}/COMPLETED" ]]; then
        # Khởi tạo state từ manifest của bản gần nhất
        MANIFEST="${INITIAL_BUNDLE}/manifest.json"
        if [[ -f "${MANIFEST}" ]]; then
            COMPLETED_UTC="$(jq -r '.completed_at_utc // ""' "${MANIFEST}" 2>/dev/null || true)"
            if [[ -n "${COMPLETED_UTC}" ]]; then
                # Chuyển UTC sang epoch
                if date -u -d "${COMPLETED_UTC}" +%s >/dev/null 2>&1; then
                    LAST_SUCCESS_EPOCH="$(date -u -d "${COMPLETED_UTC}" +%s)"
                elif date -j -u -f "%Y-%m-%dT%H:%M:%SZ" "${COMPLETED_UTC}" +%s >/dev/null 2>&1; then
                    LAST_SUCCESS_EPOCH="$(date -j -u -f "%Y-%m-%dT%H:%M:%SZ" "${COMPLETED_UTC}" +%s)"
                else
                    LAST_SUCCESS_EPOCH="${CURRENT_EPOCH}"
                fi
            fi
        fi
    else
        echo "Lỗi: Chưa có lịch sử backup thành công. Yêu cầu tạo/kiểm tra backup khởi tạo trước khi bật lịch (không tự bịa timestamp)." >&2
        exit 1
    fi
fi

# ------------------------------------------------------------------------------
# 5. Kiểm tra trạng thái sẵn sàng của MySQL Container và Identity
# ------------------------------------------------------------------------------
CONTAINER_READY="false"
CURRENT_CONTAINER_STARTED_AT=""

if [[ -n "${MOCK_CONTAINER_READY}" ]]; then
    CONTAINER_READY="${MOCK_CONTAINER_READY}"
    CURRENT_CONTAINER_STARTED_AT="${MOCK_CONTAINER_STARTED_AT:-mock_startup_1}"
else
    CONTAINER_JSON="$(docker compose -p "${COMPOSE_PROJECT}" -f "${COMPOSE_FILE}" ps --format json "${DB_SERVICE}" 2>/dev/null || true)"
    if [[ -n "${CONTAINER_JSON}" ]]; then
        # Trích xuất trạng thái và StartedAt
        C_STATE="$(echo "${CONTAINER_JSON}" | jq -r 'if type=="array" then .[0].State else .State end' 2>/dev/null || echo "")"
        C_HEALTH="$(echo "${CONTAINER_JSON}" | jq -r 'if type=="array" then .[0].Health else .Health end' 2>/dev/null || echo "")"
        
        if [[ "${C_STATE}" == "running" && ( "${C_HEALTH}" == "healthy" || "${C_HEALTH}" == "" ) ]]; then
            CONTAINER_READY="true"
            CONTAINER_ID="$(echo "${CONTAINER_JSON}" | jq -r 'if type=="array" then .[0].ID else .ID end' 2>/dev/null || echo "")"
            CURRENT_CONTAINER_STARTED_AT="$(docker inspect --format '{{.State.StartedAt}}' "${CONTAINER_ID}" 2>/dev/null || echo "${CONTAINER_ID}")"
        fi
    fi
fi

save_state() {
    local tmp_file="${DISPATCH_STATE_FILE}.tmp.$$"
    cat <<EOF > "${tmp_file}"
{
  "last_successful_backup_timestamp": ${LAST_SUCCESS_EPOCH},
  "last_scheduled_slot": "${LAST_SCHEDULED_SLOT}",
  "last_container_started_at": "${LAST_CONTAINER_STARTED_AT}",
  "last_catchup_startup_id": "${LAST_CATCHUP_STARTUP_ID}",
  "last_poll_timestamp": ${CURRENT_EPOCH}
}
EOF
    chmod 0600 "${tmp_file}"
    mv -f "${tmp_file}" "${DISPATCH_STATE_FILE}"
}

if [[ "${CONTAINER_READY}" != "true" ]]; then
    # MySQL chưa sẵn sàng: chỉ cập nhật poll timestamp rồi thoát êm
    save_state
    exit 0
fi

# ------------------------------------------------------------------------------
# 6. Xác định điều kiện chạy Scheduled vs Catch-up
# ------------------------------------------------------------------------------
TRIGGER_SCHEDULED=false
TRIGGER_CATCHUP=false

# Điều kiện A: Đúng 02:00 Asia/Ho_Chi_Minh hằng ngày
if [[ "${CURRENT_TIME_VN}" == "02:00" ]]; then
    if [[ "${LAST_SCHEDULED_SLOT}" != "${CURRENT_DATE_VN}" ]]; then
        TRIGGER_SCHEDULED=true
    fi
fi

# Điều kiện B: Catch-up khi phát hiện startup mới / phục hồi sau downtime
STARTUP_DETECTED=false
if [[ -n "${CURRENT_CONTAINER_STARTED_AT}" && "${CURRENT_CONTAINER_STARTED_AT}" != "${LAST_CONTAINER_STARTED_AT}" ]]; then
    STARTUP_DETECTED=true
fi
# Nhận diện host sleep/resume hoặc khoảng gián đoạn poll > 300s
if [[ ${LAST_POLL_EPOCH} -gt 0 && $((CURRENT_EPOCH - LAST_POLL_EPOCH)) -gt 300 ]]; then
    STARTUP_DETECTED=true
fi

if [[ "${STARTUP_DETECTED}" == "true" ]]; then
    # Chỉ catch-up đúng một lần trong cùng một lần khởi động
    if [[ "${LAST_CATCHUP_STARTUP_ID}" != "${CURRENT_CONTAINER_STARTED_AT}" ]]; then
        ELAPSED=$((CURRENT_EPOCH - LAST_SUCCESS_EPOCH))
        # Quy tắc Q1: Lần thành công cuối đã > 24 giờ (86400s). Đúng 24 giờ chưa đủ điều kiện!
        if [[ ${ELAPSED} -gt 86400 ]]; then
            TRIGGER_CATCHUP=true
        fi
    fi
fi

# ------------------------------------------------------------------------------
# 7. Thực hiện điều phối
# ------------------------------------------------------------------------------
if [[ "${TRIGGER_SCHEDULED}" != "true" && "${TRIGGER_CATCHUP}" != "true" ]]; then
    LAST_CONTAINER_STARTED_AT="${CURRENT_CONTAINER_STARTED_AT}"
    save_state
    exit 0
fi

echo "Điều phối phát hiện điều kiện kích hoạt: scheduled=${TRIGGER_SCHEDULED}, catchup=${TRIGGER_CATCHUP}" >&2

# Ghi nhận slot / startup id trước khi chạy để tránh retry mỗi phút nếu lỗi
ATTEMPTED_SLOT="${LAST_SCHEDULED_SLOT}"
if [[ "${TRIGGER_SCHEDULED}" == "true" ]]; then
    ATTEMPTED_SLOT="${CURRENT_DATE_VN}"
fi

ATTEMPTED_CATCHUP_ID="${LAST_CATCHUP_STARTUP_ID}"
if [[ "${TRIGGER_CATCHUP}" == "true" ]]; then
    ATTEMPTED_CATCHUP_ID="${CURRENT_CONTAINER_STARTED_AT}"
fi

LAST_CONTAINER_STARTED_AT="${CURRENT_CONTAINER_STARTED_AT}"
LAST_SCHEDULED_SLOT="${ATTEMPTED_SLOT}"
LAST_CATCHUP_STARTUP_ID="${ATTEMPTED_CATCHUP_ID}"

# Gọi script backup
BACKUP_EXIT_CODE=0
"${SCRIPT_DIR}/backup.sh" --config "${CONFIG_FILE}" || BACKUP_EXIT_CODE=$?

if [[ ${BACKUP_EXIT_CODE} -eq 0 ]]; then
    LAST_SUCCESS_EPOCH="${CURRENT_EPOCH}"
    echo "Backup hoàn tất thành công thông qua dispatcher." >&2
else
    echo "Cảnh báo: Backup thất bại (exit code: ${BACKUP_EXIT_CODE}). Giữ nguyên mốc thành công cũ." >&2
fi

save_state
exit "${BACKUP_EXIT_CODE}"
