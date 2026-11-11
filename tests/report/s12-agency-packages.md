# Báo cáo kiểm thử chi tiết: Gói dữ liệu cơ quan chức năng (S12)

- **Tệp đặc tả nguồn:** [`tests/cases/s12-agency-packages.md`](../cases/s12-agency-packages.md)
- **Báo cáo tổng hợp:** [`tests/report/README.md`](README.md)
- **Danh mục lỗi liên quan:** [`tests/report/01-bao-cao-loi.md`](01-bao-cao-loi.md)
- **Phạm vi kiểm thử:** 38 ca từ `AGP-01.1` đến `AGP-09.4` của backend gói dữ liệu cơ quan (Feature 12 / S12, WBS 2.4.1.4, PBI-023, quyết định D13): phân quyền, kiểm tra dữ liệu vào, phạm vi hộ và snapshot, quy trình duyệt/bàn giao/thu hồi, thời hạn, audit, toàn vẹn, retention và bố cục tệp. Giao diện ADMIN của S12 đang làm riêng nên không thuộc báo cáo này.

---

## 1. Thống kê kết quả

| Chỉ tiêu | Giá trị |
| --- | --- |
| Tổng số ca kiểm thử | **38** |
| Số ca Đạt (Pass) | **38** |
| Số ca Không đạt (Fail) | **0** |
| Số ca Bị chặn (Blocked) | **0** |
| Số ca Chưa thực thi (Not run) | **0** |
| **Tỷ lệ đạt** | **100.00%** |

---

## 2. Môi trường và bằng chứng thực thi

**Ý nghĩa của "Đạt".** Mỗi ca Đạt khi có test tự động tương ứng chạy đạt, không phải đã thao tác thủ công trên trình duyệt. Chưa ca nào có walkthrough end-to-end với giao diện, vì giao diện S12 chưa hoàn tất.

- **Môi trường:** Windows 11, JDK 21.0.12.1, Maven wrapper, MySQL 8.4 (container Compose `bluemoon-db-1`, cổng 3306) với schema trống riêng `bluemoon_s12` cho lượt chạy này (đã xóa sau khi chạy). Ngày kiểm thử 11/11/2026.
- **Lệnh:** `cd backend && ./mvnw -B -ntp -Pintegration verify` (giống CI), biến `DB_*` lấy từ `.env`.
- **Kết quả:** **464 unit và 193 integration, 0 lỗi, 0 bỏ qua, BUILD SUCCESS.** Trước khi thêm test là 416 unit và 179 integration (theo `docs/VALIDATION.md`); lượt này thêm 48 unit và 14 integration.
- **Test mới (62):**
  - `AgencyDataPackageCaseGapTest`: 23 unit (Mockito, đồng hồ cố định): mốc hạn, giới hạn kích thước, nội dung audit, bảo vệ truy cập.
  - `AgencyDataPackageWriterGapTest`: 7 unit: bố cục tệp XLSX.
  - `AgencyDataPackageErrorMappingApiTest`: 18 WebMvc: dữ liệu vào, bộ lọc, ánh xạ lỗi HTTP.
  - `AgencyDataPackageCaseIT`: 14 integration trên MySQL thật. Dùng `@SpringBootTest` thuần để dùng chung context đã cache (không thêm connection pool), dữ liệu commit và dọn theo ID.
- **Kiểm thử đột biến** (sửa tạm `src/main`, chạy lại, hoàn nguyên); cả 8 đều làm test thất bại:

| # | Đột biến | Test bắt được |
| --- | --- | --- |
| 1 | Hạn tải duyệt + 8 ngày thay vì 7 | `AgencyDataPackageCaseGapTest` |
| 2 | Giới hạn 50.000 dòng thành 50.001 | `AgencyDataPackageCaseGapTest` |
| 3 | Audit bàn giao bỏ khóa `status` | `AgencyDataPackageCaseGapTest` |
| 4 | Mã băm chấp nhận chữ hoa | `AgencyDataPackageErrorMappingApiTest` |
| 5 | Dọn dẹp bỏ `markContentsDeleted` | `AgencyDataPackageCaseIT` |
| 6 | Snapshot gồm cả cư dân `MOVED_OUT` | `AgencyDataPackageCaseIT` (test báo lỗi thay vì assertion sai; chưa truy nguyên nguyên nhân) |
| 7 | Audit S12 hết hạn sau 1 tháng thay vì 5 năm | `AgencyDataPackageCaseIT` |
| 8 | Bỏ `FOR UPDATE` khi khóa hàng gói | `AgencyDataPackageCaseIT` (2 test đồng thời thất bại) |

---

## 3. Kết quả theo từng ca

| Mã ca | Tên ca | Kết quả | Bằng chứng kiểm thử |
| --- | --- | --- | --- |
| `AGP-01.1` | ANON bị 401 trên 9 endpoint | ✅ Đạt | `AgencyDataPackageApiTest.unauthenticated_returns401` |
| `AGP-01.2` | RESIDENT, ACCOUNTANT bị 403 trên 9 endpoint | ✅ Đạt | `AgencyDataPackageApiTest.nonAdmin_returns403` |
| `AGP-01.3` | Ghi thiếu CSRF bị 403 | ✅ Đạt | `AgencyDataPackageApiTest.writeWithoutCsrf_returns403` |
| `AGP-01.4` | Người thực hiện lấy từ phiên, không từ thân yêu cầu | ✅ Đạt | `ErrorMappingApiTest`: `create_clientCannotSupplyActorOrStatusFields`, `create_actorComesFromSessionAndRequestIsPassedUnchanged`, `actions_passSessionActorAndExpectedVersionToService` |
| `AGP-02.1` | Thân tạo gói sai | ✅ Đạt | `ErrorMappingApiTest`: `create_malformedOrNonObjectBodyReturnsInvalidRequest`, `create_missingAnyRequiredFieldReturnsInvalidRequest`, `create_textFieldsMustBeJsonStrings` |
| `AGP-02.2` | `householdIds` không phải mảng số nguyên | ✅ Đạt | `ErrorMappingApiTest.create_householdIdsMustBeAnArrayOfIntegers`; `ApiTest.create_householdIdOverflowsLong_returns400` |
| `AGP-02.3` | Mã cơ quan, mã tham chiếu sai định dạng | ✅ Đạt | `AgencyDataPackageServiceTest.createPackage_invalidAgencyCodeOrReferences_throwsBadRequest` |
| `AGP-02.4` | Thân thao tác thiếu hoặc sai phiên bản, mã băm | ✅ Đạt | `ErrorMappingApiTest.actions_rejectBadVersionOrHashOnEveryActionRoute` (4 endpoint × 14 thân sai) |
| `AGP-02.5` | Bộ lọc danh sách, xem trước, chi tiết, tải | ✅ Đạt | `ErrorMappingApiTest`: `list_rejectsInvalidStatusPageAndSize`, `list_defaultsAndExplicitFiltersReachTheService`, `preview_rejectsInvalidPagingAndExtraParameters`, `detailAndDownload_rejectAnyQueryParameter` |
| `AGP-02.6` | Mã gói không phải số | ✅ Đạt | `ErrorMappingApiTest.nonNumericPackageIdReturns400WithoutReachingTheService` |
| `AGP-03.1` | Chỉ cư dân hộ trong phạm vi | ✅ Đạt | `AgencyDataPackageIT.ac1_scopeIsolationAndFieldAllowlist` |
| `AGP-03.2` | Hộ không hoạt động, không tồn tại, danh sách sai | ✅ Đạt | `AgencyDataPackageIT.ac1_inactiveOrUnknownHousehold_rejected`; `ServiceTest.createPackage_scopeInvalid_duplicateOrNonPositive` |
| `AGP-03.3` | Loại `MOVED_OUT`, sắp xếp, số đếm khớp tệp | ✅ Đạt | `AgencyDataPackageCaseIT.snapshotIncludesOnlyCurrentResidentsOrderedByApartmentAndCountsMatchWorkbook` |
| `AGP-03.4` | Snapshot không đổi khi dữ liệu gốc đổi | ✅ Đạt | `AgencyDataPackageIT.ac3_snapshotImmutabilityAfterCreation`; `CaseIT.householdDeactivatedAfterCreationDoesNotChangeTheFrozenSnapshot` |
| `AGP-03.5` | Giới hạn 1.000 hộ, 50.000 dòng, 10 MB | ✅ Đạt | `CaseGapTest`: `create_exactlyThousandHouseholdsAccepted`, `create_moreThanFiftyThousandResidentRowsRejectedBeforeWritingFile`, `create_exactlyFiftyThousandResidentRowsAccepted`, `create_fileLargerThanTenMegabytesRejectedAndNothingStored`, `create_snapshotCountsComeFromRowsNotFromRequest` |
| `AGP-03.6` | Xem trước phân trang | ✅ Đạt | `AgencyDataPackageCaseIT.previewPagesThroughAllRowsWithoutOverlap`; `ServiceTest.previewPackage_largePageNumber_returnsEmptyListWithoutException` |
| `AGP-04.1` | Tự duyệt bị 403 | ✅ Đạt | `AgencyDataPackageIT.ac2_separationOfDutiesAndCreatorReject`; `ErrorMappingApiTest.selfApprovalMapsTo403WithDedicatedCode`; `ServiceTest.approvePackage_creatorAttemptsApproval_throwsForbidden` |
| `AGP-04.2` | Người tạo từ chối nháp | ✅ Đạt | `AgencyDataPackageIT.ac2_separationOfDutiesAndCreatorReject`; `ServiceTest.rejectPackage_creatorRejectsOwnDraft_success` |
| `AGP-04.3` | Phiên bản hoặc mã băm cũ bị 409 | ✅ Đạt | `ServiceTest.approvePackage_staleVersionOrHash_throwsConflict`; `CaseGapTest.failedTransitionsWriteNoAuditAndNoUpdate`; `ErrorMappingApiTest.conflictCodesMapTo409OnActionsAndPreview` |
| `AGP-04.4` | Bàn giao lưu DB, bàn giao lần hai bị 409 | ✅ Đạt | `AgencyDataPackageCaseIT.fullLifecycleWithHandoffPersistsStateAndAuditsEveryStepWithoutPersonalData` |
| `AGP-04.5` | Thu hồi chặn tải, kể cả sau bàn giao | ✅ Đạt | `AgencyDataPackageIT.ac4_lifecycleRevokeAndDownloadGuard`; `CaseIT.revokeAfterHandoffIsAllowedAndBlocksFurtherDownload` |
| `AGP-04.6` | Tranh chấp đồng thời, một bên thắng | ✅ Đạt | `AgencyDataPackageCaseIT`: `concurrentApproveAndRejectOfTheSameDraftHaveExactlyOneWinner`, `twoAdminsApprovingConcurrentlyYieldSingleApproval` |
| `AGP-05.1` | Hạn tải = min(duyệt + 7 ngày, tạo + 30 ngày) | ✅ Đạt | `CaseGapTest`: `approve_downloadDeadlineIsSevenDaysAfterApprovalWhenEarlier`, `approve_downloadDeadlineIsCappedAtContentDeleteAfter`, `approve_exactlySevenDaysBeforeCapKeepsSevenDays`; `CaseIT`: `downloadDeadlineIsSevenDaysAfterApprovalNormally`, `downloadDeadlineIsCappedWhenContentDeletesSoonerThanSevenDays` |
| `AGP-05.2` | Mốc chính xác của hạn tải và hạn xóa nội dung | ✅ Đạt | `CaseGapTest`: `download_blockedExactlyAtDeadlineAllowedOneSecondBefore`, `handoff_blockedExactlyAtDeadlineAllowedOneSecondBefore`, `preview_approvedPackageBlockedExactlyAtDownloadDeadline`, `approveAndReject_blockedExactlyAtContentDeleteAfter`, `preview_draftBlockedExactlyAtContentDeleteAfter` |
| `AGP-05.3` | Hết hạn tải trên DB thật | ✅ Đạt | `AgencyDataPackageCaseIT.expiredDownloadWindowBlocksDownloadPreviewAndHandoffOnRealDb` |
| `AGP-06.1` | Audit đúng hành động, người, khóa, không PII | ✅ Đạt | `CaseGapTest`: `auditChanges_createCarriesOnlyAllowedKeysAndNoResidentName`, `auditChanges_approveRejectRevokeHandoffRecordStatusVersionAndHash`, `auditChanges_downloadAuthorizationRecordsContentFingerprintOnly`; `CaseIT.fullLifecycle…` |
| `AGP-06.2` | Thao tác thất bại không ghi audit | ✅ Đạt | `CaseGapTest`: `failedTransitionsWriteNoAuditAndNoUpdate`, `unauthenticatedActorRejectedBeforeAnyStoreAccess` |
| `AGP-07.1` | Nội dung sai mã băm hoặc mất không bị phát hành | ✅ Đạt | `CaseGapTest`: `download_tamperedContentIsNeverReleasedNorAudited`, `preview_tamperedContentIsNeverParsed`, `download_contentRowMissingGivesUnavailableNotServerError` |
| `AGP-07.2` | Ánh xạ lỗi HTTP, không lộ nội bộ | ✅ Đạt | `ErrorMappingApiTest`: `notFoundMapsTo404WithCode`, `conflictCodesMapTo409OnActionsAndPreview`, `selfApprovalMapsTo403WithDedicatedCode`, `serviceValidationErrorsKeepTheirSpecificCode`, `contentIntegrityFailureReturnsGeneric500WithoutLeakingInternals` |
| `AGP-07.3` | Tiêu đề phản hồi khi tải | ✅ Đạt | `ApiTest.download_valid_returnsExcelAttachment`; `ErrorMappingApiTest.conflictCodesMapTo409OnActionsAndPreview` (lỗi không có `Content-Disposition`) |
| `AGP-07.4` | Ràng buộc schema | ✅ Đạt | `AgencyDataPackageCaseIT.schemaRejectsSelfApprovalInconsistentCountsBadStatusAndOversizeContent` (kiểm tra đúng tên ràng buộc bị vi phạm) |
| `AGP-08.1` | Dọn dẹp nội dung, metadata, audit S12; audit dọn dẹp actor NULL | ✅ Đạt | `AgencyDataPackageCaseIT.retentionCleanupRemovesExpiredContentMetadataAndS12AuditsOnly` |
| `AGP-08.2` | Gói quá 5 năm ẩn khỏi danh sách, chi tiết 404 | ✅ Đạt | `AgencyDataPackageCaseIT.packageOlderThanFiveYearsIsHiddenFromListAndDetail`; `ServiceTest.metadataExpiredAfter5Years_notFound` |
| `AGP-08.3` | Dọn nhiều lô, không audit khi không có gì | ✅ Đạt | `ServiceTest`: `executeRetentionCleanup_multipleBatchesWithTransactionManager`, `executeRetentionCleanup_noDataCleaned_doesNotLogAudit` |
| `AGP-09.1` | Khối metadata và 4 cột | ✅ Đạt | `AgencyDataPackageWriterGapTest`: `metadataBlockCarriesTemplateVersionsSnapshotTimesAndCounts`, `headerRowAndDataStartWhereTheContractSaysAndOnlyFourColumnsExist` |
| `AGP-09.2` | Cố định dòng và bộ lọc | ✅ Đạt | `AgencyDataPackageWriterGapTest.freezePaneAndAutoFilterCoverTheHeaderAndAllDataRows` |
| `AGP-09.3` | Thứ tự, nhãn tiếng Việt, bọc chữ, chống công thức | ✅ Đạt | `WriterGapTest`: `rowOrderIsPreservedAndStatusLabelsAreVietnameseForHumans`, `longNamesAreWrappedWithTallerRow`; `WriterTest.writePackage_formulaInjection_renderedAsLiteralString` |
| `AGP-09.4` | Mã băm SHA-256 | ✅ Đạt | `AgencyDataPackageWriterGapTest.sameInputProducesSameMetadataAndRows_hashDependsOnContent` |

---

## 4. Phát hiện và quan sát

Không có ca Fail. Hai điểm dưới đây không có trong đặc tả đã duyệt nên không tính là lỗi; tôi chưa sửa mã nguồn trong lượt kiểm thử này.

1. **Nhãn metadata của tệp gói không được in đậm.** `AgencyDataPackageWriter.createBoldStyle` tạo font đậm nhưng không gán vào style, nên cột nhãn của 8 dòng metadata dùng font thường (đã xác nhận bằng cách đọc font của ô: nhãn `bold=false`, tiêu đề cột `bold=true`). Chỉ ảnh hưởng hình thức. Nếu muốn in đậm, thêm `style.setFont(font)`; khi đó nên thêm test và đổi mã băm mẫu nếu có.
2. **Đọc lại tệp coi mọi nhãn trạng thái lạ là "Tạm vắng".** `parseWorkbookRows` chỉ nhận `Đang cư trú` là `RESIDING`, mọi giá trị khác thành `TEMPORARILY_ABSENT`. Hiện tệp chỉ do `writePackage` sinh ra và có kiểm tra mã băm trước khi đọc nên không gây sai lệch; nhưng nếu sau này có nguồn tệp khác thì nên báo lỗi với nhãn lạ.

---

## 5. Giới hạn đã biết

- Không có giao diện S12 nên chưa có walkthrough trên trình duyệt; cần ca giao diện khi UI hoàn tất.
- Scheduler dọn dẹp thật chạy mỗi 60 giây trong context test, nên ca retention chỉ khẳng định trạng thái cuối của dữ liệu do chính test tạo ra, không khẳng định số đếm toàn cục.
- Ca thời gian trên DB thật dùng đồng hồ hệ thống và dời mốc bằng SQL; ca mốc chính xác theo từng giây dùng đồng hồ cố định ở mức unit.
- Chạy trên một máy, chưa có kết quả CI trên GitHub tại thời điểm viết báo cáo.
- Chưa kiểm thử tải lớn (50.000 dòng thực trên MySQL) và hiệu năng truy vấn; chuyển sang S14.
- Quyết định D13 nêu rõ không có mẫu pháp lý được xác nhận cho gói S12; báo cáo này không khẳng định gói đáp ứng yêu cầu pháp luật.
