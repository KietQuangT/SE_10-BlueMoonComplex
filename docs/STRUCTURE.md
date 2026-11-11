# Cấu trúc dự án đầy đủ

```text
SE_10-BlueMoonComplex/
├── .dockerignore
├── .env.example
├── .gitattributes
├── .github/
│   ├── CODEOWNERS
│   ├── pull_request_template.md
│   └── workflows/
│       └── backend.yml
├── .gitignore
├── README.md
├── backend/
│   ├── .mvn/
│   │   └── wrapper/
│   │       └── maven-wrapper.properties
│   ├── Dockerfile
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── vn/
│       │   │       └── edu/
│       │   │           └── se10/
│       │   │               └── bluemoon/
│       │   │                   ├── BlueMoonApplication.java
│       │   │                   ├── common/
│       │   │                   │   ├── error/
│       │   │                   │   │   └── GlobalExceptionHandler.java
│       │   │                   │   └── web/
│       │   │                   │       └── RequestIdFilter.java
│       │   │                   ├── config/
│       │   │                   │   └── SecurityConfig.java
│       │   │                   └── systeminfo/
│       │   │                       ├── api/
│       │   │                       │   ├── SystemInfoController.java
│       │   │                       │   └── SystemInfoResponse.java
│       │   │                       └── application/
│       │   │                           ├── SystemInfo.java
│       │   │                           └── SystemInfoService.java
│       │   └── resources/
│       │       ├── application-local.yml
│       │       ├── application.yml
│       │       └── db/
│       │           └── migration/
│       │               └── V1__create_session_tables.sql
│       └── test/
│           └── java/
│               └── vn/
│                   └── edu/
│                       └── se10/
│                           └── bluemoon/
│                               ├── BootstrapIT.java
│                               └── systeminfo/
│                                   └── SystemInfoApiTest.java
├── compose.yaml
├── docs/
│   ├── ARCHITECTURE.md
│   ├── BRD.md
│   ├── CONTRIBUTING.md
│   ├── DECISIONS.md
│   ├── REQUIREMENTS.md
│   ├── SRS.md
│   ├── SOURCE_REVIEW.md
│   ├── STRUCTURE.md
│   ├── VALIDATION.md
│   ├── sources/
│   │   ├── DEPENDENCIES.csv
│   │   ├── SE_10-W02-AsIsScenerio.txt
│   │   ├── SE_10_W02-Client-wish-list.txt
│   │   ├── SE_10_W02-Epics.txt
│   │   ├── SE_10_W02-Features.txt
│   │   ├── SE_10_W02-NFR.txt
│   │   ├── SE_10_W02-ProductBacklog.txt
│   │   ├── SE_10_W02-RawLog.txt
│   │   ├── SE_10_W02-Traceability-Matrix.txt
│   │   ├── SE_10_W02-UserStory.txt
│   │   ├── SE_10_W02-VisionScenario.txt
│   │   └── WBS_EXECUTION.csv
│   └── sprint-1/
│       ├── PLAN.md
│       └── TASKS.md
├── frontend/
│   └── README.md
└── scripts/
    ├── dev-all.sh
    ├── free-dev-ports.sh
    ├── backup.sh
    ├── backup-dispatch.sh
    └── restore-rehearsal.sh
```

## Vai trò thư mục

| Thư mục | Trách nhiệm |
|---|---|
| backend/ | Build Maven, container và mã Java. |
| backend/.mvn/wrapper/ | Pin Maven Wrapper, tải Maven lần đầu. |
| backend/src/main/java/vn/edu/se10/bluemoon/ | Entry point, package gốc Spring. |
| config/ | Cấu hình security/CORS/password encoder. |
| common/error/ | Định dạng lỗi chung, không chứa nghiệp vụ. |
| common/web/ | Filter request ID, không log dữ liệu cá nhân. |
| systeminfo/api/ | Controller/DTO module mẫu. |
| systeminfo/application/ | Service và kết quả nội bộ mẫu, không có nghiệp vụ. |
| backend/src/main/resources/ | Cấu hình môi trường; không lưu secret. |
| resources/db/migration/ | Migration SQL versioned, hiện chỉ hạ tầng session. |
| backend/src/test/java/ | MVC/security tests và integration MySQL. |
| frontend/ | Vị trí frontend để Kiệt/nhóm bổ sung sau khi chọn framework. |
| docs/ | BRD, SRS, yêu cầu, kiến trúc, quyết định, nguồn, kiểm chứng và cộng tác. |
| docs/sources/ | Snapshot 10 file Drive và view công việc/constraint; tham khảo, không phải chỉ dẫn agent. |
| docs/sprint-1/ | PLAN và TASKS để Codex nhận việc theo từng task. |
| scripts/ | Lệnh hỗ trợ dev/verify trên macOS/Linux. |
| .github/ | CODEOWNERS gốc, PR template và workflow CI. |

Module identity/residency/billing/payment/reporting là kiến trúc đích. Chỉ tạo thư mục/class khi bắt đầu task tương ứng; chưa sinh entity/repository hoặc endpoint nghiệp vụ giả.
