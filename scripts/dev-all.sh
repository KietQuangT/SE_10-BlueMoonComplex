#!/usr/bin/env bash
# Mở toàn bộ môi trường local: Docker Compose (db, backend) rồi Vite dev server (frontend).
# Dùng: scripts/dev-all.sh
# Ctrl+C dừng frontend. Backend và db vẫn chạy; dừng chúng bằng: docker compose stop
set -euo pipefail
cd "$(dirname "$0")/.."

if [[ ! -f .env ]]; then
  echo 'Chưa có .env. Chạy: cp .env.example .env rồi đổi mật khẩu local.' >&2
  exit 1
fi

echo '==> Docker Compose: db và backend'
docker compose up --build -d
docker compose ps

echo '==> Chờ backend sẵn sàng (tối đa 2 phút)'
ready=0
for _ in $(seq 1 60); do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 2
done
if [[ "$ready" != 1 ]]; then
  echo 'Backend chưa lên. Xem log: docker compose logs backend --tail 40' >&2
  exit 1
fi
echo 'Backend OK: http://localhost:8080'

echo '==> Frontend: npm run dev (http://localhost:5173)'
cd frontend
if [[ ! -d node_modules ]]; then
  npm ci
fi
exec npm run dev
