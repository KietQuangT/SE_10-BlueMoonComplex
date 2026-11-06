import { useState } from 'react';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';

const AVAILABLE_FIELDS = [
  { key: 'apartmentCode', label: 'Mã căn hộ', safe: true },
  { key: 'fullName', label: 'Họ và tên nhân khẩu', safe: true },
  { key: 'residencyStatus', label: 'Trạng thái cư trú (Đang ở / Tạm vắng)', safe: true },
  { key: 'dateOfBirth', label: 'Năm sinh', safe: true },
  { key: 'relationshipToHead', label: 'Quan hệ với chủ hộ', safe: true },
  { key: 'identityNumber', label: 'Số CCCD (Dữ liệu cá nhân nhạy cảm - Cần giấy phép)', safe: false },
  { key: 'phone', label: 'Số điện thoại (Dữ liệu cá nhân nhạy cảm)', safe: false },
];

export function ReportPackagesPage() {
  const { reportPackages, approveReportPackageDemo, forcedUiState } = useAppState();

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [period, setPeriod] = useState('2026-Q4');
  const [selectedFields, setSelectedFields] = useState<string[]>([
    'apartmentCode',
    'fullName',
    'residencyStatus',
  ]);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const handleToggleField = (key: string) => {
    setSelectedFields((prev) =>
      prev.includes(key) ? prev.filter((k) => k !== key) : [...prev, key],
    );
  };

  const handlePreparePackage = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedFields.length === 0) return;
    setConfirmOpen(true);
  };

  const handleConfirmPackage = () => {
    setConfirmOpen(false);
    approveReportPackageDemo(period, selectedFields);
    setCreateModalOpen(false);
    setSuccessBanner(
      `Đã phê duyệt và đóng gói dữ liệu kỳ ${period}. Gói này hiện đã khả dụng cho tài khoản Cơ quan chức năng tại M-40.`,
    );
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-19"
          contract="assumed"
          tierLabel="Mức C"
          title="Đóng gói báo cáo cho cơ quan"
          description="Tạo gói báo cáo đã phê duyệt với danh mục trường giới hạn (whitelist) cho cơ quan chức năng."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-19"
          contract="assumed"
          tierLabel="Mức C"
          title="Đóng gói báo cáo cho cơ quan"
          description="Tạo gói báo cáo đã phê duyệt với danh mục trường giới hạn (whitelist) cho cơ quan chức năng."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <ScreenHeader
          screenCode="M-19"
          contract="assumed"
          tierLabel="Mức C"
          title="Đóng gói báo cáo cho cơ quan"
          description="PBI-023 / NFR-009: Giới hạn trường chia sẻ, bảo vệ dữ liệu cá nhân cư dân, đóng gói phục vụ kiểm tra."
        />
        <div>
          <button
            onClick={() => setCreateModalOpen(true)}
            className="px-4 py-2 bg-teal-brand text-white font-semibold rounded hover:bg-teal-700 transition flex items-center gap-2"
          >
            <span>+ Tạo và phê duyệt gói mới</span>
          </button>
        </div>
      </div>

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">🟡 Nguyên tắc bảo vệ thông tin cư dân (NFR-009)</p>
        <p className="text-sm mt-1">
          Cơ quan chức năng tuyệt đối không được truy cập trực tiếp bảng dữ liệu thô. Mọi số liệu cung cấp phải được Ban Quản Trị rà soát, áp dụng whitelist trường và ký xác nhận đóng gói trước khi phát hành.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {/* Danh sách các gói đã đóng */}
      <div className="station-panel p-5 bg-white border border-border mb-8">
        <div className="station-strip bg-rail mb-4"></div>
        <h2 className="text-base font-bold text-rail mb-3">Các gói báo cáo đã phê duyệt</h2>

        {reportPackages.length === 0 ? (
          <EmptyStatePanel
            title="Chưa có gói báo cáo nào được đóng gói"
            description="Tạo và phê duyệt gói mới để cung cấp số liệu cho tài khoản cơ quan chức năng."
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm border-collapse">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  <th className="py-2.5 px-3">Loại báo cáo</th>
                  <th className="py-2.5 px-3">Kỳ báo cáo</th>
                  <th className="py-2.5 px-3">Người phê duyệt</th>
                  <th className="py-2.5 px-3">Thời điểm duyệt</th>
                  <th className="py-2.5 px-3">Trường cho phép (Whitelist)</th>
                  <th className="py-2.5 px-3">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {reportPackages.map((pkg) => (
                  <tr key={pkg.id} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-medium text-rail">{pkg.typeLabel}</td>
                    <td className="py-2.5 px-3 font-mono font-bold text-teal-brand">{pkg.period}</td>
                    <td className="py-2.5 px-3 text-slate">{pkg.approvedBy}</td>
                    <td className="py-2.5 px-3 font-mono text-xs text-slate">
                      {pkg.approvedAt ? new Date(pkg.approvedAt).toLocaleDateString('vi-VN') : '-'}
                    </td>
                    <td className="py-2.5 px-3">
                      <div className="flex flex-wrap gap-1">
                        {pkg.allowedFields.map((f) => (
                          <span
                            key={f}
                            className="text-xs px-1.5 py-0.5 rounded bg-slate-100 text-slate font-mono"
                          >
                            {f}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="text-xs px-2 py-0.5 rounded bg-teal-50 text-teal-brand border border-teal-200 font-bold uppercase">
                        {pkg.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <OpenQuestions
        items={[
          {
            code: 'Q24',
            text: 'Gói báo cáo đóng gói lưu trữ dạng snapshot bất biến (file PDF/Excel tĩnh) hay dữ liệu được sinh lại tại thời điểm cơ quan tải xuống?',
            owner: 'Sơn / Tuấn',
          },
          {
            code: 'Q24.1',
            text: 'Có cơ chế thu hồi (revoke) gói báo cáo nếu phát hiện sai lệch hoặc khi giấy phép thanh tra của cơ quan hết hạn không?',
            owner: 'Sơn / Kiệt',
          },
        ]}
      />

      {/* Modal tạo gói */}
      {createModalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full p-6 border border-border">
            <h3 className="text-lg font-bold text-rail mb-3">Tạo gói báo cáo cho cơ quan chức năng</h3>
            <form onSubmit={handlePreparePackage} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Kỳ báo cáo <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={period}
                  onChange={(e) => setPeriod(e.target.value)}
                  placeholder="Ví dụ: 2026-Q4 hoặc 2026-11"
                  className="w-full px-3 py-2 border border-border rounded text-sm font-mono"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-2">
                  Danh mục trường cho phép chia sẻ (Whitelist) <span className="text-danger">*</span>
                </label>
                <div className="space-y-2 border border-border p-3 rounded bg-ground max-h-48 overflow-y-auto">
                  {AVAILABLE_FIELDS.map((field) => (
                    <label key={field.key} className="flex items-start gap-2 text-xs cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedFields.includes(field.key)}
                        onChange={() => handleToggleField(field.key)}
                        className="mt-0.5"
                      />
                      <div>
                        <span className="font-semibold text-rail">{field.label}</span>
                        {!field.safe && (
                          <span className="block text-amber-700 font-medium">⚠️ Chú ý: Dữ liệu nhạy cảm</span>
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setCreateModalOpen(false)}
                  className="px-4 py-2 border border-border text-slate rounded hover:bg-slate-50 text-sm"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={selectedFields.length === 0}
                  className="px-4 py-2 bg-teal-brand text-white font-semibold rounded hover:bg-teal-700 disabled:opacity-50 text-sm"
                >
                  Tiếp tục phê duyệt
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <ConfirmDialog
        open={confirmOpen}
        title="Phê duyệt và phát hành gói báo cáo"
        confirmLabel="Phê duyệt ngay"
        consequence={`Bạn đang phê duyệt gói báo cáo lưu trú kỳ ${period} với ${selectedFields.length} trường dữ liệu. Tài khoản Cơ quan chức năng sẽ có quyền tải xuống gói này.`}
        onConfirm={handleConfirmPackage}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  );
}
