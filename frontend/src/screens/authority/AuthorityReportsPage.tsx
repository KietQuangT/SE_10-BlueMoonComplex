import { formatBusinessDate, formatInstantVn } from '../../lib/format';
import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';
import type { ReportPackage } from '../../types/domain';

export function AuthorityReportsPage() {
  const { reportPackages, residents, households, forcedUiState } = useAppState();

  const [selectedPackage, setSelectedPackage] = useState<ReportPackage | null>(null);

  const approvedPackages = reportPackages.filter((p) => p.status === 'APPROVED');

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-40"
          contract="assumed"
          tierLabel="Mức C"
          title="Báo cáo đã được phê duyệt"
          description="Xem và kết xuất các gói hồ sơ, báo cáo chính thức đã được Ban Quản Trị thẩm định."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-40"
          contract="assumed"
          tierLabel="Mức C"
          title="Báo cáo đã được phê duyệt"
          description="Xem và kết xuất các gói hồ sơ, báo cáo chính thức đã được Ban Quản Trị thẩm định."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <ScreenHeader
        screenCode="M-40"
        contract="assumed"
        tierLabel="Mức C"
        title="Báo cáo đã được phê duyệt"
        description="Xem những gói dữ liệu đã được Ban quản trị kiểm tra, phê duyệt."
      />

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Giới hạn phạm vi quyền Cơ quan chức năng</p>
        <p className="text-sm mt-1">
          Hệ thống chỉ cung cấp các gói báo cáo đã được phê duyệt. Cơ quan không có quyền truy cập trực tiếp danh bạ nhân khẩu thô, không tra cứu căn hộ tùy ý và không xem thông tin tài chính - công nợ nội bộ của chung cư.
        </p>
      </div>

      <p id="authority-download-note" className="field-hint mb-6">Tải tệp chưa khả dụng. Chọn Xem gói để kiểm tra nội dung được phép chia sẻ.</p>

      {/* Danh sách gói báo cáo */}
      <div className="station-panel p-5 bg-white border border-border mb-8">
        <div className="station-strip bg-teal-brand mb-4"></div>
        <h2 className="text-base font-bold text-rail mb-3">Danh sách gói báo cáo khả dụng</h2>

        {approvedPackages.length === 0 ? (
          <EmptyStatePanel
            title="Chưa có gói báo cáo nào khả dụng"
            description="Hiện chưa có gói báo cáo nào được Ban Quản Trị phê duyệt cho tài khoản cơ quan."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="ledger-table">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  <th className="py-2.5 px-3">Loại báo cáo</th>
                  <th className="py-2.5 px-3">Kỳ báo cáo</th>
                  <th className="py-2.5 px-3">Ngày phê duyệt</th>
                  <th className="py-2.5 px-3">Người phê duyệt</th>
                  <th className="py-2.5 px-3">Phạm vi trường</th>
                  <th className="py-2.5 px-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {approvedPackages.map((pkg) => (
                  <tr key={pkg.id} className="hover:bg-slate-50">
                    <td className="py-3 px-3 font-medium text-rail">{pkg.typeLabel}</td>
                    <td className="py-3 px-3 font-mono font-bold text-teal-brand">{pkg.period}</td>
                    <td className="py-3 px-3 font-mono text-xs text-slate">
                      {formatInstantVn(pkg.approvedAt)}
                    </td>
                    <td className="py-3 px-3 text-slate">{pkg.approvedBy}</td>
                    <td className="py-3 px-3 text-xs text-slate">
                      {pkg.allowedFields.length} trường được duyệt
                    </td>
                    <td className="py-3 px-3 text-right"><div className="row-actions">
                      <button
                        onClick={() => setSelectedPackage(pkg)}
                        className="btn btn-secondary btn-sm"
                      >
                        Xem gói
                      </button>
                      <button
                        disabled aria-describedby="authority-download-note"
                        className="btn btn-primary btn-sm"
                      >
                        Tải về
                      </button></div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Xem chi tiết gói đã chọn */}
      {selectedPackage && (
        <div className="station-panel p-5 bg-white border border-border mb-8">
          <div className="filter-row justify-between mb-4">
            <div>
              <h3 className="font-bold text-rail text-base">
                Nội dung gói: {selectedPackage.typeLabel} - Kỳ {selectedPackage.period}
              </h3>
              <span className="text-xs text-slate">
                Phê duyệt bởi: {selectedPackage.approvedBy} · Trạng thái: {selectedPackage.status === 'APPROVED' ? 'Đã phê duyệt' : selectedPackage.status}
              </span>
            </div>
            <button
              onClick={() => setSelectedPackage(null)}
              className="btn btn-ghost btn-sm"
            >
              Đóng xem trước
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="ledger-table">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  {selectedPackage.allowedFields.includes('apartmentCode') && (
                    <th className="py-2 px-3">Căn hộ</th>
                  )}
                  {selectedPackage.allowedFields.includes('fullName') && (
                    <th className="py-2 px-3">Họ và tên</th>
                  )}
                  {selectedPackage.allowedFields.includes('residencyStatus') && (
                    <th className="py-2 px-3">Trạng thái cư trú</th>
                  )}
                  {selectedPackage.allowedFields.includes('dateOfBirth') && (
                    <th className="py-2 px-3">Ngày sinh</th>
                  )}
                  {selectedPackage.allowedFields.includes('relationshipToHead') && (
                    <th className="py-2 px-3">Quan hệ chủ hộ</th>
                  )}
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {residents.slice(0, 5).map((r) => {
                  const hh = households.find((h) => h.id === r.householdId);
                  return (
                    <tr key={r.id}>
                      {selectedPackage.allowedFields.includes('apartmentCode') && (
                        <td className="py-2 px-3 font-mono font-bold text-teal-brand">
                          {hh?.apartmentCode || '-'}
                        </td>
                      )}
                      {selectedPackage.allowedFields.includes('fullName') && (
                        <td className="py-2 px-3 font-medium text-rail">{r.fullName}</td>
                      )}
                      {selectedPackage.allowedFields.includes('residencyStatus') && (
                        <td className="py-2 px-3 text-slate">{r.residencyStatus}</td>
                      )}
                      {selectedPackage.allowedFields.includes('dateOfBirth') && (
                        <td className="py-2 px-3 font-mono text-xs">{formatBusinessDate(r.dateOfBirth)}</td>
                      )}
                      {selectedPackage.allowedFields.includes('relationshipToHead') && (
                        <td className="py-2 px-3 text-slate">{r.relationshipToHead}</td>
                      )}
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}

      <OpenQuestions
        items={[
          {
            code: 'Q19',
            text: 'Chưa có API hoặc màn hình quản trị để BQT cấp tài khoản cho vai trò Cơ quan chức năng (màn M-11 hiện chỉ cho chọn 3 vai trò).',
            owner: 'Sơn / Tuấn',
          },
          {
            code: 'Q22',
            text: 'Cơ chế gửi yêu cầu thanh tra / khiếu nại từ cơ quan chức năng đến BQT chưa được đưa vào phạm vi v1.',
            owner: 'Sơn / PO',
          },
        ]}
      />
    </div>
  );
}
