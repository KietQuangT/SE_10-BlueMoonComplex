#!/usr/bin/env bash
# ==============================================================================
# BlueMoon S13 — Chạy Toàn Bộ Test Suite Backup & Restore
# ==============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"

echo "========================================================================"
echo "           BlueMoon S13 Test Suite: Backup & Restore Rehearsal          "
echo "========================================================================"

TOTAL_SUITES=3
SUITES_PASSED=0

run_suite() {
    local suite_script="$1"
    local suite_name="$2"
    echo ""
    echo ">>> Running Suite: ${suite_name}"
    if "${suite_script}"; then
        echo ">>> SUITE PASSED: ${suite_name}"
        SUITES_PASSED=$((SUITES_PASSED + 1))
    else
        echo ">>> SUITE FAILED: ${suite_name}"
        exit 1
    fi
}

run_suite "${SCRIPT_DIR}/test_backup_guards.sh" "V1 - Backup Guard Rails"
run_suite "${SCRIPT_DIR}/test_backup_retention.sh" "V3 - Retention & Failure Handling"
run_suite "${SCRIPT_DIR}/test_dispatch_catchup.sh" "V4 - Dispatcher Schedule & Catch-up"
run_suite "${SCRIPT_DIR}/test_restore_rehearsal_guards.sh" "V1/V2 - Restore Rehearsal Guards"

echo ""
echo "========================================================================"
echo "TẤT CẢ TEST SUITES S13 ĐÃ HOÀN THÀNH XUẤT SẮC: ${SUITES_PASSED} / 4 PASSED"
echo "========================================================================"
