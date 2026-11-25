# Sơ đồ Triển khai UML (Deployment Diagrams) — BlueMoon Complex (Sprint 1)

Tài liệu đặc tả kiến trúc triển khai vật lý/container, luồng đóng gói Docker multi-stage, quy trình CI trên GitHub Actions và hạ tầng vận hành sao lưu – phục hồi của **BlueMoon Complex (SE_10)**, đối chiếu trực tiếp với nhánh `main` (`compose.yaml`, `backend/Dockerfile`, `backend/pom.xml`, `backend/src/main/resources/application.yml`, `frontend/vite.config.ts`, `.github/workflows/backend.yml`, `scripts/`).

---

## DD-01: Kiến trúc Triển khai Runtime (Local / Staging Host & Docker Compose)

Sơ đồ dưới đây mô tả các node chạy thực tế trên máy chủ/máy phát triển: trình duyệt người dùng, Vite dev server / Production SPA bundle, Docker Network chứa container `backend` (JDK 21 JRE + Spring Boot `4.0.9`) và `db` (MySQL `8.4`), cùng các tiến trình vận hành phía Host OS (`systemd` timer `02:00 Asia/Ho_Chi_Minh`, bộ script sao lưu/diễn tập phục hồi S13, và profile khởi tạo CLI `BootstrapAdminRunner`).

```mermaid
flowchart TB
    subgraph ClientNode["Node 1: Client Workstation (Trình duyệt Web)"]
        Browser["Web Browser (React + TypeScript SPA)"]
    end

    subgraph DevFrontendNode["Node 2: Frontend Host (Vite Dev Server :5173 hoặc Static Host)"]
        Vite["Vite Dev Server / Built Assets - Proxy /api/* sang localhost:8080"]
    end

    subgraph DockerHost["Node 3: Docker Compose Host (compose.yaml)"]
        subgraph AppContainer["Container: backend (eclipse-temurin:21-jre) - Port 8080"]
            Spring["Spring Boot 4.0.9 App - BlueMoon REST API"]
            Security["Spring Security Filter Chain - Session + CSRF + CORS"]
            Flyway["Flyway Migrations V1..V11 (26 tables)"]
            Scheduler["AgencyPackageRetentionScheduler - fixedDelay 60000ms"]
        end

        subgraph DbContainer["Container: db (mysql:8.4) - Port 3306"]
            MySQL["MySQL 8.4 Instance - Schema bluemoon"]
            SessionTbl["SPRING_SESSION & SPRING_SESSION_ATTRIBUTES"]
            DomainTbl["Domain, Audit & Agency Package Tables (24 tables)"]
        end

        Volume["Docker Persistent Volume: mysql_data"]
    end

    subgraph HostOps["Node 4: Host OS Operations & Scripts (scripts/)"]
        Timer["systemd Timer: bluemoon-backup.timer - 02:00 Asia/Ho_Chi_Minh"]
        Dispatch["scripts/backup-dispatch.sh + scripts/backup.sh"]
        BackupStore["Backup Directory - .sql.gz + .sha256 + COMPLETED (7 bản gần nhất)"]
        Rehearsal["scripts/restore-rehearsal.sh -> DB tạm bluemoon_restore_s13_*"]
        CleanupCLI["Spring Profile: restore-cleanup - RestoreRetentionCleanupRunner"]
        BootstrapCLI["Spring Profile: bootstrap - BootstrapAdminRunner (CLI TTY)"]
        MockMail["scripts/mock-email.sh - Đọc bảng mock_emails"]
    end

    Browser -- "HTTP/HTTPS (Port 5173 dev hoặc static)" --> Vite
    Vite -- "HTTP Proxy /api/* + Cookie + X-CSRF-TOKEN" --> Security
    Browser -- "CORS trực tiếp (http://localhost:5173 -> :8080)" --> Security
    Security --> Spring
    Spring --> Flyway
    Spring --> Scheduler
    Spring -- "JDBC MySQL Port 3306" --> MySQL
    MySQL --- SessionTbl
    MySQL --- DomainTbl
    MySQL --- Volume

    Timer --> Dispatch
    Dispatch -- "mysqldump (loại trừ SPRING_SESSION*)" --> MySQL
    Dispatch --> BackupStore
    BackupStore --> Rehearsal
    Rehearsal -- "Nạp dump + Flyway V1 session rỗng" --> MySQL
    Rehearsal --> CleanupCLI
    CleanupCLI -- "Dọn gói S12 quá hạn trên DB diễn tập" --> MySQL
    BootstrapCLI -- "Interactive System.console() tạo ADMIN đầu tiên" --> MySQL
    MockMail -- "Truy vấn mật khẩu tạm trong dev/mock" --> MySQL
```

---

## DD-02: Quy trình Build Multi-stage Docker & CI Pipeline (GitHub Actions)

Sơ đồ mô tả quy trình đóng gói image `backend/Dockerfile` (2 stage trên JDK/JRE 21 của nhánh `main`) và luồng kiểm thử tích hợp tự động `.github/workflows/backend.yml`.

> [!NOTE]
> Phiên bản JDK trong sơ đồ bám sát nhánh `origin/main` hiện tại (`maven:3.9.11-eclipse-temurin-21`, `eclipse-temurin:21-jre`, GitHub Actions `java-version: '21'`). Việc nâng cấp lên JDK 25 LTS (nếu áp dụng) được theo dõi ở bản cập nhật cấu hình riêng.

```mermaid
flowchart LR
    subgraph Source["GitHub Repository (IT3180-SE_10)"]
        Repo["Source Code & .github/CODEOWNERS (@KietQuangT, @hhoangsonnw)"]
        Workflow[".github/workflows/backend.yml (Push / PR -> main)"]
    end

    subgraph GitHubRunner["GitHub Actions Runner (ubuntu-latest)"]
        Checkout["actions/checkout + setup-java temurin 21 (cache maven)"]
        TestCmd["mvn verify - Chạy Unit & Integration Tests (Testcontainers / MySQL)"]
    end

    subgraph DockerBuild["Docker Multi-stage Build (backend/Dockerfile)"]
        Stage1["Stage 1 (build): maven:3.9.11-eclipse-temurin-21 -> mvn -B package -DskipTests"]
        Jar["Artifact: target/*.jar (app.jar)"]
        Stage2["Stage 2 (runtime): eclipse-temurin:21-jre -> COPY app.jar -> EXPOSE 8080"]
    end

    subgraph RegistryDeploy["Triển khai Runtime (compose.yaml)"]
        ComposeUp["docker compose up -d (backend + db mysql:8.4)"]
    end

    Repo --> Workflow --> Checkout --> TestCmd
    Repo --> Stage1 --> Jar --> Stage2 --> ComposeUp
```

---

## Đặc tả Hạ tầng, Cổng mạng, Lưu trữ và Ranh giới Bảo mật

| Thành phần / Node | Công nghệ / Image (`main`) | Cổng / Giao thức | Biến môi trường & Lưu trữ chính | Ranh giới bảo mật & Ghi chú vận hành |
| :--- | :--- | :--- | :--- | :--- |
| **Frontend Dev / Build** | React, TypeScript, Vite (`frontend/vite.config.ts`) | `5173` (HTTP dev proxy `/api` -> `8080`) | Nguồn tĩnh SPA, gửi kèm `X-CSRF-TOKEN` và cookie session | Chịu kiểm soát CORS `http://localhost:5173` trên backend |
| **Backend API** | Spring Boot `4.0.9`, `eclipse-temurin:21-jre` | `8080` (HTTP REST `/api/v1/**`) | `SPRING_DATASOURCE_*`, `BLUEMOON_CORS_ALLOWED_ORIGINS` | `CurrentAccountFilter` -> `MustChangePasswordFilter` -> `AuthorizationFilter`; bắt buộc `Idempotency-Key` cho giao dịch thanh toán |
| **Database Server** | `mysql:8.4` (`compose.yaml`) | `3306` (TCP/JDBC) | Volume `mysql_data`, Flyway `V1`–`V11` (26 bảng) | Chỉ mở cho backend trong mạng Docker Compose và các tác vụ Ops có quyền trên host |
| **Scheduled Retention** | `AgencyPackageRetentionScheduler` (in-process) | Trong tiến trình JVM (`60.000 ms`) | Bảng `agency_data_packages`, `agency_data_package_contents` | Xóa nội dung gói S12 sau 30 ngày, đóng link tải sau 7 ngày, giữ metadata & `audit_log` 5 năm |
| **Backup Scheduler** | `bluemoon-backup.timer` + `backup-dispatch.sh` + `backup.sh` | Host `systemd` (02:00 `Asia/Ho_Chi_Minh`) | Tệp `.sql.gz`, `.sha256`, `COMPLETED` (giữ 7 bản) | Loại trừ `SPRING_SESSION*` để tránh rò rỉ phiên đăng nhập vào bản sao lưu |
| **Restore Rehearsal** | `restore-rehearsal.sh` + profile `restore-cleanup` | Host CLI / MySQL | DB tách biệt `bluemoon_restore_s13_*`, xuất JSON Q5 | Chặn ghi đè lên DB nguồn (`source DB guard`), dựng lại bảng session rỗng từ `V1` |
| **Bootstrap Admin** | `BootstrapAdminRunner` (`@Profile("bootstrap")`) | Host CLI (`System.console()`) | Tương tác trực tiếp bàn phím TTY | Không cấu hình mật khẩu Admin mặc định trong file YAML/mã nguồn (`D16`) |
