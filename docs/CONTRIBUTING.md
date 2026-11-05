# Cách cộng tác và đưa bộ khung lên main

Repo đích: https://github.com/KietQuangT/SE_10-BlueMoonComplex . Baseline đã đọc: `ae142a8`, nhánh main, có README và .github/CODEOWNERS.
Nhánh khởi tạo đề xuất: `codex/backend-foundation`. Chỉ dùng quyền GitHub của thành viên được phép, không gửi token vào chat.

```bash
git clone https://github.com/KietQuangT/SE_10-BlueMoonComplex.git
cd SE_10-BlueMoonComplex
git switch -c codex/backend-foundation
# Chép nội dung gói harness vào repo này, không chép .git hoặc target.
cd backend
./mvnw -B -ntp verify
cd ..
git add AGENTS.md README.md .gitignore .gitattributes .env.example .dockerignore compose.yaml backend docs frontend scripts .github
git diff --cached --check
git commit -m "chore: khởi tạo backend và harness Sprint 1"
git push -u origin codex/backend-foundation
```
Tạo PR vào main. Reviewer kiểm tra CI với MySQL, migration, checklist và quickstart trên máy sạch; merge khi đạt foundation DoD. Đây là quy trình đưa skeleton lên main; không đánh dấu Sprint 1 hoàn thành.
Nếu main đã thay đổi: fetch và merge/rebase trên branch cá nhân sau khi bảo toàn thay đổi. Không dùng `push --force` vào main.
PM nên bật branch protection/ruleset yêu cầu review và Backend CI nếu tài khoản/repo hỗ trợ; cấu hình đó chưa được áp dụng từ gói này.
