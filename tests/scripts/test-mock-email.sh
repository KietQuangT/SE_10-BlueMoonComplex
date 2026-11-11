#!/usr/bin/env bash
set -euo pipefail
root="$(cd "$(dirname "$0")/../.." && pwd)"
tmp="$(mktemp -d)"
trap 'rm -rf "$tmp"' EXIT
cat > "$tmp/docker" <<'MOCK'
#!/usr/bin/env bash
printf '%s\n' "$*" > "$MOCK_ARGS"
cat > "$MOCK_SQL"
printf 'Thư mô phỏng\nMật khẩu tạm: synthetic-test-only\n'
MOCK
chmod +x "$tmp/docker"
export PATH="$tmp:$PATH" MOCK_ARGS="$tmp/args" MOCK_SQL="$tmp/sql"
if bash "$root/scripts/mock-email.sh" '1 OR 1=1' > /dev/null 2>&1; then
  echo 'FAIL: invalid username accepted' >&2; exit 1
fi
[[ ! -e "$MOCK_SQL" ]] || { echo 'FAIL: invalid username reached database' >&2; exit 1; }
output="$(bash "$root/scripts/mock-email.sh" accountant)"
[[ "$output" == *'synthetic-test-only'* ]]
grep -q "u.username = 'accountant'" "$MOCK_SQL"
grep -q 'must_change_password' "$MOCK_SQL"
grep -q 'compose exec -T db' "$MOCK_ARGS"
output="$(bash "$root/scripts/mock-email.sh" --all)"
[[ "$output" == *'synthetic-test-only'* ]]
grep -q 'u.username' "$MOCK_SQL"
grep -q 'must_change_password' "$MOCK_SQL"
grep -q 'Mật khẩu tạm:' "$MOCK_SQL"
grep -q 'ROW_NUMBER()' "$MOCK_SQL"
echo 'mock-email tests passed'
