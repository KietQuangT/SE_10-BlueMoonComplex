#!/usr/bin/env bash
# Show listeners on local development ports and ask before stopping anything.
set -euo pipefail
cd "$(dirname "$0")/.."

ports=(3306 8080 5173)
busy=()
for port in "${ports[@]}"; do
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
    busy+=("$port")
  fi
done

if ((${#busy[@]} == 0)); then
  echo 'Các cổng 3306, 8080, 5173 đều đang trống.'
  exit 0
fi

for port in "${busy[@]}"; do
  echo "=== Cổng $port ==="
  lsof -nP -iTCP:"$port" -sTCP:LISTEN || true
  containers="$(docker ps --filter "publish=$port" --format '{{.ID}} {{.Names}}' 2>/dev/null || true)"
  if [[ -n "$containers" ]]; then
    while read -r id name; do
      [[ -n "$id" ]] || continue
      read -r -p "Dừng container $name ($id) để giải phóng cổng $port? [y/N] " answer </dev/tty
      if [[ "$answer" == [yY] ]]; then docker stop "$id"; fi
    done <<< "$containers"
    continue
  fi

  pids="$(lsof -t -nP -iTCP:"$port" -sTCP:LISTEN 2>/dev/null | sort -u || true)"
  while read -r pid; do
    [[ -n "$pid" ]] || continue
    command="$(ps -p "$pid" -o command= 2>/dev/null || true)"
    if [[ "$command" == *com.docker* ]]; then
      echo "PID $pid thuộc Docker; không dừng Docker daemon. Hãy kiểm tra docker ps --filter publish=$port." >&2
      continue
    fi
    read -r -p "Dừng PID $pid ($command) trên cổng $port? [y/N] " answer </dev/tty
    if [[ "$answer" == [yY] ]]; then
      kill -TERM "$pid"
      echo "Đã gửi SIGTERM tới PID $pid."
    fi
  done <<< "$pids"
done

still_busy=()
for port in "${ports[@]}"; do
  if lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then still_busy+=("$port"); fi
done
if ((${#still_busy[@]})); then
  echo "Còn cổng bị chiếm: ${still_busy[*]}. Kiểm tra tiến trình rồi chạy lại script." >&2
  exit 1
fi
echo 'Đã giải phóng các cổng dev.'
