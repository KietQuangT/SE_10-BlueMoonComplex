import { useState } from 'react';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';

export function BillingPeriodsPage() {
  const { households, invoices, forcedUiState } = useAppState();

  const [period, setPeriod] = useState('2026-11');
  const [dueDate, setDueDate] = useState('2026-11-20');
  const [selectedFeeTypes, setSelectedFeeTypes] = useState<string[]>(['QUAN_LY', 'RAC']);
  const [applyToAll, setApplyToAll] = useState(true);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [successBanner, setSuccessBanner] = useState<string | null>(null);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const activeHouseholds = households.filter((h) => h.status === 'ACTIVE');
  const existingPeriods = Array.from(new Set(invoices.map((i) => i.period))).sort().reverse();

  const handleToggleFee = (code: string) => {
    setSelectedFeeTypes((prev) =>
      prev.includes(code) ? prev.filter((c) => c !== code) : [...prev, code],
    );
  };

  const handleTriggerCreate = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);
    setSuccessBanner(null);

    if (existingPeriods.includes(period)) {
      setErrorBanner(`Kỳ ${period} đã tồn tại trong hệ thống. Ngăn tạo trùng để bảo toàn dữ liệu.`);
      return;
    }

    if (selectedFeeTypes.length === 0) {
      setErrorBanner('Vui lòng chọn ít nhất một loại phí áp dụng.');
      return;
    }

    setConfirmOpen(true);
  };

  const handleConfirmIssue = () => {
    setConfirmOpen(false);
    setSuccessBanner(
      `Đã lập kỳ thu mẫu ${period} cho ${activeHouseholds.length} hộ hoạt động (Chế độ mô phỏng giao diện / Chờ chốt quy tắc).`,
    );
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-24"
          contract="pending-rule"
          tierLabel="Mức C"
          title="Tạo kỳ thu / Phát hành hóa đơn"
          description="Khung phát hành kỳ thu hàng loạt cho các hộ gia đình theo loại phí và hạn nộp."
        />
        <SkeletonRows rows={5} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-24"
          contract="pending-rule"
          tierLabel="Mức C"
          title="Tạo kỳ thu / Phát hành hóa đơn"
          description="Khung phát hành kỳ thu hàng loạt cho các hộ gia đình theo loại phí và hạn nộp."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <ScreenHeader
        screenCode="M-24"
        contract="pending-rule"
        tierLabel="Mức C"
        title="Tạo kỳ thu / Phát hành hóa đơn"
        description="Khung phát hành kỳ thu hàng loạt cho các hộ gia đình theo loại phí và hạn nộp (WBS 2.4 / PBI-011 / task S08)."
      />

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Khung màn hình Chờ chốt quy tắc nghiệp vụ (Tier C - M-24)</p>
        <p className="text-sm mt-1">
          Theo quyết định sprint và ma trận F01, chức năng này thuộc phạm vi v1 để sinh dữ liệu hóa đơn cho Cư dân (M-32, M-33) và Kế toán (M-21, M-22), nhưng đang chờ chốt thẩm quyền (BQT phát hành hay Kế toán) và cơ chế duyệt snapshot đơn giá.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {errorBanner && (
        <div className="notice-box notice-danger mb-6">
          <p>{errorBanner}</p>
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-8">
        <div className="lg:col-span-2 station-panel p-5 bg-white border border-border">
          <div className="station-strip bg-teal-brand mb-4"></div>
          <h2 className="text-lg font-bold text-rail mb-3">Thông số kỳ thu mới</h2>

          <form onSubmit={handleTriggerCreate} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Kỳ thu (Tháng / Năm) <span className="text-danger">*</span>
                </label>
                <input
                  type="month"
                  value={period}
                  onChange={(e) => setPeriod(e.target.value)}
                  className="w-full px-3 py-2 border border-border rounded font-mono text-sm"
                  required
                />
                <span className="text-xs text-slate mt-1 block">Định dạng YYYY-MM (Ví dụ: 2026-11)</span>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Hạn nộp thanh toán <span className="text-danger">*</span>
                </label>
                <input
                  type="date"
                  value={dueDate}
                  onChange={(e) => setDueDate(e.target.value)}
                  className="w-full px-3 py-2 border border-border rounded font-mono text-sm"
                  required
                />
                <span className="text-xs text-slate mt-1 block">Hiển thị cho cư dân tại M-32</span>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold uppercase text-slate mb-1">
                Các loại phí áp dụng <span className="text-danger">*</span>
              </label>
              <div className="flex flex-wrap gap-4 mt-2">
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedFeeTypes.includes('QUAN_LY')}
                    onChange={() => handleToggleFee('QUAN_LY')}
                  />
                  <span>Phí quản lý (10.000 đ/m²)</span>
                </label>
                <label className="flex items-center gap-2 text-sm cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedFeeTypes.includes('RAC')}
                    onChange={() => handleToggleFee('RAC')}
                  />
                  <span>Phí rác sinh hoạt (60.000 đ/hộ)</span>
                </label>
              </div>
            </div>

            <div className="pt-2 border-t border-border">
              <label className="flex items-center gap-2 text-sm cursor-pointer">
                <input
                  type="checkbox"
                  checked={applyToAll}
                  onChange={(e) => setApplyToAll(e.target.checked)}
                />
                <span className="font-medium">
                  Áp dụng cho tất cả hộ đang hoạt động ({activeHouseholds.length} hộ)
                </span>
              </label>
              <p className="text-xs text-slate mt-1 ml-6">
                Chỉ tính cho hộ có trạng thái Hoạt động tại thời điểm lập kỳ thu. Hộ đã chuyển đi không phát sinh hóa đơn mới.
              </p>
            </div>

            <div className="pt-4 flex justify-end">
              <button
                type="submit"
                className="btn btn-primary"
              >
                Tạo kỳ thu & Phát hành hóa đơn
              </button>
            </div>
          </form>
        </div>

        <div className="station-panel p-5 bg-white border border-border">
          <div className="station-strip bg-rail mb-4"></div>
          <h2 className="text-base font-bold text-rail mb-3">Các kỳ đã phát hành</h2>
          <div className="space-y-3">
            {existingPeriods.map((p) => {
              const count = invoices.filter((i) => i.period === p).length;
              const paidCount = invoices.filter((i) => i.period === p && i.status === 'DA_DONG').length;
              return (
                <div key={p} className="p-3 bg-ground rounded border border-border">
                  <div className="flex justify-between items-center mb-1">
                    <span className="font-bold font-mono text-rail">Kỳ {p}</span>
                    <span className="text-xs px-2 py-0.5 rounded bg-teal-50 text-teal-brand border border-teal-200">
                      Đã phát hành
                    </span>
                  </div>
                  <div className="text-xs text-slate space-y-1">
                    <div>Tổng hóa đơn: {count} căn</div>
                    <div>Đã thu: {paidCount} / {count}</div>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <OpenQuestions
        items={[
          {
            code: 'Q11',
            text: 'Ai là người có thẩm quyền tạo kỳ thu hàng loạt? (Ma trận F01 ghi Ban Quản Trị, nhưng đơn giá và thu tiền lại do Kế toán phụ trách).',
            owner: 'Quân / Kiệt',
          },
          {
            code: 'Q11.1',
            text: 'Hóa đơn sau khi lập có cần Kế toán trưởng duyệt trước khi gửi đến cư dân không, hay tự động xuất bản ngay?',
            owner: 'Quân / Kiệt',
          },
          {
            code: 'Q11.2',
            text: 'Kỳ thu đã phát hành có được phép hủy hoặc điều chỉnh nếu phát hiện sai sót số liệu diện tích?',
            owner: 'Quân / Sơn',
          },
        ]}
      />

      <ConfirmDialog
        open={confirmOpen}
        title="Xác nhận phát hành kỳ thu"
        confirmLabel="Phát hành ngay"
        consequence={`Bạn đang tạo hóa đơn kỳ ${period} cho ${activeHouseholds.length} hộ hoạt động. Hệ thống sẽ áp dụng mức đơn giá hiệu lực tại thời điểm này.`}
        onConfirm={handleConfirmIssue}
        onCancel={() => setConfirmOpen(false)}
      />
    </div>
  );
}
