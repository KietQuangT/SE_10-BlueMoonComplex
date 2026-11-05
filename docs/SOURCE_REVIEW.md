# Nguồn, đối chiếu và xung đột

Đọc ngày 04/11/2026. Snapshot text trong sources được trích từ tài liệu; không phải bản nhị phân gốc và không chứa macro. SHA-256 bên dưới áp dụng cho snapshot text, không phải file Drive.

| File | Nguồn | Snapshot | SHA-256 |
|---|---|---|---|
| SE_10-W02-AsIsScenerio.xlsm | [Drive](https://drive.google.com/file/d/1bGYgnkCgeFVEgREQmrDX6bxqorkH1Kn-/view?usp=drivesdk) | [docs/sources/SE_10-W02-AsIsScenerio.txt](../docs/sources/SE_10-W02-AsIsScenerio.txt) | `8b11b2bb7138131a184978293ddd613aaf6dc20293fb0ac3ea5e8843f7642ca1` |
| SE_10_W02-Epics.xlsx | [Drive](https://docs.google.com/spreadsheets/d/1NaULzCDLzgQdvM2gcVYeS4vpQFu4cURM/edit?usp=drivesdk&ouid=111023284014285796764&rtpof=true&sd=true) | [docs/sources/SE_10_W02-Epics.txt](../docs/sources/SE_10_W02-Epics.txt) | `210c91ab6ceebbbeb0f38cd8eb6d86657d9e9177d81d7d7de260724a4a747f45` |
| SE_10_W02-Traceability-Matrix | [Drive](https://docs.google.com/spreadsheets/d/1rcjAyEeUODuNo6usqup1HbudyAsDZQGrXCBNy_POhkM/edit?usp=drivesdk) | [docs/sources/SE_10_W02-Traceability-Matrix.txt](../docs/sources/SE_10_W02-Traceability-Matrix.txt) | `38c918419ed8760cb69420d8b87a6ed1c65dd01a72639683c0767da55da185a5` |
| SE_10_W02-Features.xlsx | [Drive](https://docs.google.com/spreadsheets/d/1N-zMTkUdr2Eg98IysQiJ2Pm0f8CVGVlj/edit?usp=drivesdk&ouid=111023284014285796764&rtpof=true&sd=true) | [docs/sources/SE_10_W02-Features.txt](../docs/sources/SE_10_W02-Features.txt) | `401eee56d81672b2ceac6f6877ccfe2d224688f9a471262ca0555dd6a7594e43` |
| SE_10_W02-ProductBacklog | [Drive](https://docs.google.com/spreadsheets/d/1AFgJncqMjPWieefVWhEwOTiCLlZ2yF6Hh7YrSQpj2cA/edit?usp=drivesdk) | [docs/sources/SE_10_W02-ProductBacklog.txt](../docs/sources/SE_10_W02-ProductBacklog.txt) | `cd566df3045d52c7dcdf3df43b8b5a7a68f32304bf1cc68817512432127bdbbe` |
| SE_10_W02-UserStory.xlsx | [Drive](https://docs.google.com/spreadsheets/d/1M5vuBNSc9QdARsdFYnQ3yYsgjEOZuO_3/edit?usp=drivesdk&ouid=111023284014285796764&rtpof=true&sd=true) | [docs/sources/SE_10_W02-UserStory.txt](../docs/sources/SE_10_W02-UserStory.txt) | `9581ce8d404ea0c5bbded4164567daca197c04fb818b6fa4b890c7a19a91433e` |
| SE_10_W02-VisionScenario.xlsx | [Drive](https://docs.google.com/spreadsheets/d/1ehk4v9fyFldg33KAPF_Xp3oJ54BhGJNe/edit?usp=drivesdk&ouid=111023284014285796764&rtpof=true&sd=true) | [docs/sources/SE_10_W02-VisionScenario.txt](../docs/sources/SE_10_W02-VisionScenario.txt) | `59369404f1295ed1047cbd3d041d20af29771997493536620c09d0d96cd5f769` |
| SE_10_W02-RawLog | [Drive](https://docs.google.com/spreadsheets/d/1HB9mRKvrldNyIVbSBkOO7Ecxq047OPrmTAMjKf5cm_8/edit?usp=drivesdk) | [docs/sources/SE_10_W02-RawLog.txt](../docs/sources/SE_10_W02-RawLog.txt) | `5b40f3cee3bd8b0d6c08c5bd4057d20dcdd30a240fe91b8695bdcda3cde7a905` |
| SE_10_W02-NFR | [Drive](https://docs.google.com/spreadsheets/d/1TBkUhE5S0wYy24BJbGZPImq69j0Y5KknEdrbA0Dq_6Q/edit?usp=drivesdk) | [docs/sources/SE_10_W02-NFR.txt](../docs/sources/SE_10_W02-NFR.txt) | `6458d6eb15cb908cd7404c894b9b5ee33078d8747571b60538350aa83538d108` |
| SE_10_W02-Client-wish-list.xlsm | [Drive](https://drive.google.com/file/d/1KBc7gLdOc6xxwZ6tloypXAMmHKxKDuak/view?usp=drivesdk) | [docs/sources/SE_10_W02-Client-wish-list.txt](../docs/sources/SE_10_W02-Client-wish-list.txt) | `16599ba3cc59ff010c6a2f3692acc18c269f428eca47b4bf5e7114fe04b94880` |

## Bộ kế hoạch đã đối chiếu
WBS/CPM, Charter, Gantt, Agile, Risks, Dashboard revised và BlueMoon_WBS_Constraints_ChiTiet bản đã rà soát. Các workbook này cung cấp nguồn lực, phân công và phạm vi; không coi phần đề xuất trong constraint là AC giảng viên đã phê duyệt.

## Kết quả kiểm kê
- Epics 6; Features 52; User Story 52; Vision Scenario 52; Product Backlog 23; NFR 10; As-Is 10.
- RawLog và Traceability Matrix có 53 dòng, trong khi Wish List/User Story có 52. RAW và US không có quan hệ bằng số thứ tự mặc định.

## Mâu thuẫn và quyết định
| Mục | Bằng chứng | Cách xử lý |
|---|---|---|
| Traceability sai taxonomy | Matrix có EP-07 và FE-07x; Epics hiện chỉ EP-01..06. US-001 trong Matrix nói thêm hộ nhưng trong UserStory là đăng nhập. | Giữ snapshot để kiểm toán; không dùng Matrix làm hợp đồng code. |
| US-004 khác PBI-004 | US-004 là truy vết; PBI-004 là vô hiệu account nhưng trace về US-004. | Giữ audit theo NFR-003 và khóa account theo PBI-004; không đổi nghĩa một mã để che mâu thuẫn. |
| US-013 khác PBI-008 | US-013 là tra cứu định danh; PBI-008 là duyệt tạm vắng. | Dùng AC nghiệp vụ PBI đã chốt cho task; mapping US cần Sơn sửa theo ngữ nghĩa. |
| Phạm vi backlog v1 khác WBS | PBI-009,014,016–020 ghi v1 trong Drive, WBS mới bố trí v2. | User đã chọn WBS mới; lưu các mục này ở v2, không xóa backlog. |
| Hiệu năng đã có ngưỡng | NFR-004: ≤3s với 10.000 hồ sơ; Charter cũng ghi mục tiêu này. | Sửa nhận định cũ “chưa có ngưỡng”; chỉ còn thiếu cấu hình/phương pháp/tải đo. |
| FE-036 không phải công thức phí xe | Features gọi FE-036 là yêu cầu dịch vụ gửi xe; WBS dùng cùng FE-020 cho phí xe. | Không dùng ID thay cho mô tả; v2 cần xác định rõ nghiệp vụ phương tiện/phí. |
| Quỹ bảo trì | Có thu–chi/công khai quỹ trong US/Features nhưng WBS chưa rõ nguồn số dư/sổ chi. | Chặn báo cáo quỹ v2 cho tới khi có nguồn, không suy từ tổng thu phí. |
| Định lượng chưa phải fact | 220h, tiết kiệm 80–85%, chi phí quy đổi và mốc ngày là dự báo. | Không coi là kết quả thực nghiệm hoặc tiền đã chi. |

## Thứ tự dùng nguồn
1. Lựa chọn user xác nhận trong DECISIONS cho stack/cấu trúc/phạm vi.
2. WBS mới quyết định v1/v2; constraint bổ sung chỉ là đề xuất nếu chưa duyệt.
3. PBI AC + NFR + mô tả Features/US được đối chiếu theo nghĩa. Gặp mâu thuẫn bảo mật/tiền/dữ liệu thì hỏi, không âm thầm ưu tiên một ô.
4. Matrix cũ/RawLog là bằng chứng nguồn và vấn đề cần chuẩn hóa, không phải lệnh triển khai.

## Snapshot vận hành từ workbook constraint
`docs/sources/WBS_EXECUTION.csv`: 30 dòng công việc cấp 4; `DEPENDENCIES.csv`: 65 quan hệ đề xuất. Đây là view trích từ BlueMoon_WBS_Constraints_ChiTiet.xlsx đã rà soát, không thay thế cấu trúc WBS gốc. Quan hệ đề xuất và thay đổi ngày chưa tự trở thành baseline được PM duyệt.
Workbook constraint cũ còn ghi ngưỡng latency chưa có; nguồn NFR vừa đọc xác nhận ≤3 giây/10.000 hồ sơ, nên dùng REQUIREMENTS.md làm chuẩn hiệu năng.
