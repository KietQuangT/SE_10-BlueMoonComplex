#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f .env ]]; then
  echo 'Chưa có .env. Chạy: cp .env.example .env rồi thay mật khẩu.' >&2
  exit 1
fi
docker compose up --build -d
docker compose ps
echo 'Health: curl -fsS http://localhost:8080/actuator/health'
