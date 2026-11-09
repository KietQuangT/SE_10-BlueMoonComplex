import { formatInstantVn } from '../../lib/format';
import { reportFieldLabel } from '../../lib/reportFields';
import { FormDialog } from '../../components/FormDialog';
import { useState } from 'react';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';

const AVAILABLE_FIELDS = [
  { key: 'apartmentCode', label: 'Mã căn hộ', safe: true },
  { key: 'fullName', label: 'Họ và tên nhân khẩu', safe: true },
  { key: 'residencyStatus', label: 'Trạng thái cư trú (Đang ở / Tạm vắng)', safe: true },
  { key: 'dateOfBirth', label: 'Ngày sinh', safe: true },
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
    setCreateModalOpen(false);
    setConfirmOpen(true);
  };

  const handleConfirmPackage = () => {
    setConfirmOpen(false);
    approveReportPackageDemo(period, selectedFields);
    setCreateModalOpen(false);
    setSuccessBanner(
      `Đã phê duyệt và đóng gói dữ liệu kỳ ${period}. Gói này hiện đã khả dụng cho tài khoản Cơ quan chức năng tại Báo cáo đã duyệt.`,
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
          description="Tạo gói báo cáo đã phê duyệt với danh mục trường được phép chia sẻ cho cơ quan chức năng."
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
          description="Tạo gói báo cáo đã phê duyệt với danh mục trường được phép chia sẻ cho cơ quan chức năng."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <ScreenHeader
          screenCode="M-19"
          contract="assumed"
          tierLabel="Mức C"
          title="Đóng gói báo cáo cho cơ quan"
          description="Giới hạn trường chia sẻ, bảo vệ dữ liệu cá nhân cư dân, đóng gói phục vụ kiểm tra."
          actions={<button
            onClick={() => setCreateModalOpen(true)}
            className="btn btn-primary"
          >
            <span>+ Tạo và phê duyệt gói mới</span>
          </button>}
        />

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Nguyên tắc bảo vệ thông tin cư dân</p>
        <p className="text-sm mt-1">
          Cơ quan chức năng tuyệt đối không được truy cập trực tiếp bảng dữ liệu thô. Mọi số liệu cung cấp phải được Ban Quản Trị rà soát, chỉ chọn các trường được phép chia sẻ và ký xác nhận đóng gói trước khi phát hành.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6" role="status">
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
            <table className="ledger-table">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  <th className="py-2.5 px-3">Loại báo cáo</th>
                  <th className="py-2.5 px-3">Kỳ báo cáo</th>
                  <th className="py-2.5 px-3">Người phê duyệt</th>
                  <th className="py-2.5 px-3">Thời điểm duyệt</th>
                  <th className="py-2.5 px-3">Trường được chia sẻ</th>
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
                      {formatInstantVn(pkg.approvedAt)}
                    </td>
                    <td className="py-2.5 px-3">
                      <div className="flex flex-wrap gap-1">
                        {pkg.allowedFields.map((f) => (
                          <span
                            key={reportFieldLabel(f)}
                            className="text-xs px-1.5 py-0.5 rounded bg-slate-100 text-slate font-mono"
                          >
                            {reportFieldLabel(f)}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="text-xs px-2 py-0.5 rounded bg-teal-50 text-teal-brand border border-teal-200 font-bold uppercase">
                        {pkg.status === 'APPROVED' ? 'Đã phê duyệt' : pkg.status}
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

      <FormDialog open={createModalOpen} title="Tạo gói báo cáo cho cơ quan chức năng"
        description="Chọn kỳ báo cáo và những trường được phép chia sẻ. Bạn sẽ kiểm tra lại trước khi phê duyệt."
        submitLabel="Tiếp tục phê duyệt" onSubmit={handlePreparePackage} onCancel={() => setCreateModalOpen(false)}>
        <label className="form-field" htmlFor="package-period">Kỳ báo cáo
          <input id="package-period" className="input tabular" required value={period} onChange={(e) => setPeriod(e.target.value)} placeholder="Ví dụ: 2026-Q4 hoặc 2026-11" />
        </label>
        <fieldset className="field-options"><legend>Trường cho phép chia sẻ</legend>
          {AVAILABLE_FIELDS.map((field) => <label key={field.key} className="field-check">
            <input type="checkbox" checked={selectedFields.includes(field.key)} onChange={() => handleToggleField(field.key)} />
            <span><strong>{field.label}</strong>{!field.safe && <span className="field-hint"> · Dữ liệu nhạy cảm, cần được cho phép chia sẻ</span>}</span>
          </label>)}
        </fieldset>
        {selectedFields.length === 0 && <p className="field-error" role="status">Chọn ít nhất một trường để tiếp tục.</p>}
      </FormDialog>

      <ConfirmDialog
        open={confirmOpen}
        title="Phê duyệt và phát hành gói báo cáo"
        confirmLabel="Phê duyệt ngay"
        consequence={`Bạn đang phê duyệt gói báo cáo lưu trú kỳ ${period} với ${selectedFields.length} trường dữ liệu. Tài khoản Cơ quan chức năng sẽ có quyền tải xuống gói này.`}
        onConfirm={handleConfirmPackage}
        onCancel={() => { setConfirmOpen(false); setCreateModalOpen(true); }}
      />
    </div>
  );
}
