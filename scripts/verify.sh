#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../backend"
./mvnw -B -ntp verify
if [[ "${1:-}" == '--integration' ]]; then
  : "${DB_PASSWORD:?Export DB_PASSWORD và khởi động MySQL trước khi chạy integration}"
  ./mvnw -B -ntp -Pintegration verify
fi
