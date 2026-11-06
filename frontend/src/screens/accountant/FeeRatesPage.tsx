import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatVnd } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

export function FeeRatesPage() {
  const { feeRates, addFeeRateDemo, forcedUiState } = useAppState();

  const [modalOpen, setModalOpen] = useState(false);
  const [feeType, setFeeType] = useState('QUAN_LY');
  const [unit, setUnit] = useState<'m2' | 'hộ'>('m2');
  const [unitPrice, setUnitPrice] = useState('10000');
  const [effectiveFrom, setEffectiveFrom] = useState('2026-12-01');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const handleOpenAdd = () => {
    setFeeType('QUAN_LY');
    setUnit('m2');
    setUnitPrice('10000');
    setEffectiveFrom('2026-12-01');
    setErrorMsg(null);
    setModalOpen(true);
  };

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);

    const feeName = feeType === 'QUAN_LY' ? 'Phí quản lý' : 'Phí rác sinh hoạt';
    const res = addFeeRateDemo({
      code: feeType,
      name: feeName,
      unit,
      unitPrice,
      effectiveFrom,
    });

    if (!res.ok) {
      setErrorMsg(res.error || 'Có lỗi khi lưu đơn giá.');
      return;
    }

    setModalOpen(false);
    setSuccessBanner(`Đã thêm mức đơn giá mới cho ${feeName}: ${formatVnd(unitPrice)} / ${unit} từ ngày ${effectiveFrom}.`);
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-20"
          contract="assumed"
          tierLabel="Mức C"
          title="Đơn giá các loại phí"
          description="Thiết lập biểu mức đơn giá áp dụng cho các khoản phí bắt buộc tại chung cư."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-20"
          contract="assumed"
          tierLabel="Mức C"
          title="Đơn giá các loại phí"
          description="Thiết lập biểu mức đơn giá áp dụng cho các khoản phí bắt buộc tại chung cư."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <ScreenHeader
          screenCode="M-20"
          contract="assumed"
          tierLabel="Mức C"
          title="Đơn giá các loại phí"
          description="PBI-011: Quản lý mức giá các khoản thu dịch vụ, quản lý và vệ sinh định kỳ."
        />
        <div>
          <button
            onClick={handleOpenAdd}
            className="btn btn-primary"
          >
            <span>+ Thêm mức đơn giá mới</span>
          </button>
        </div>
      </div>

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Bất biến lịch sử hóa đơn (PBI-011 / Q7)</p>
        <p className="text-sm mt-1">
          Hệ thống không ghi đè lên mức đơn giá cũ. Khi thay đổi đơn giá, một bản ghi mới với ngày hiệu lực mới được tạo. Các hóa đơn đã phát hành trong quá khứ luôn giữ nguyên snapshot diện tích và mức giá tại thời điểm phát hành.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {/* Bảng đơn giá */}
      <div className="station-panel p-5 bg-white border border-border mb-6">
        <div className="station-strip bg-teal-brand mb-4"></div>
        <h2 className="text-base font-bold text-rail mb-3">Bảng mức đơn giá hiện hành</h2>

        {feeRates.length === 0 ? (
          <EmptyStatePanel
            title="Chưa có mức đơn giá nào được thiết lập"
            description="Thêm mức đơn giá mới cho phí quản lý hoặc phí rác để làm căn cứ tính hóa đơn."
            actionLabel="+ Thêm đơn giá"
            onAction={handleOpenAdd}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="ledger-table">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  <th className="py-2.5 px-3">Mã loại phí</th>
                  <th className="py-2.5 px-3">Tên loại phí</th>
                  <th className="py-2.5 px-3">Đơn vị tính</th>
                  <th className="py-2.5 px-3 text-right">Đơn giá áp dụng</th>
                  <th className="py-2.5 px-3">Hiệu lực từ ngày</th>
                  <th className="py-2.5 px-3">Trạng thái</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {feeRates.map((fr) => (
                  <tr key={fr.id} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-mono font-bold text-rail">{fr.code}</td>
                    <td className="py-2.5 px-3 font-medium text-rail">{fr.name}</td>
                    <td className="py-2.5 px-3 text-slate">{fr.unit}</td>
                    <td className="py-2.5 px-3 text-right font-mono font-bold text-teal-brand">
                      {formatVnd(fr.unitPrice)}
                    </td>
                    <td className="py-2.5 px-3 font-mono text-xs">{fr.effectiveFrom}</td>
                    <td className="py-2.5 px-3">
                      <span className="text-xs px-2 py-0.5 rounded bg-teal-50 text-teal-brand border border-teal-200 font-semibold">
                        Đang áp dụng
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}

        {/* Ví dụ tĩnh theo yêu cầu wireframe */}
        <div className="mt-4 p-3 bg-ground border border-border rounded text-xs text-slate space-y-1">
          <p className="font-semibold text-rail">Ví dụ minh họa cách tính (Snapshot Server):</p>
          <p>
            Căn hộ diện tích <strong>50,00 m²</strong> áp dụng đơn giá Phí quản lý <strong>10.000 đ/m²</strong>:{' '}
            <span className="font-mono font-bold text-teal-brand">50,00 × 10.000 đ = 500.000 đ</span>.
          </p>
          <p className="text-xs text-slate-500">
            * Toàn bộ phép nhân được thực hiện phía máy chủ với kiểu số học chính xác (BigDecimal). UI không tự ý làm tròn sai lệch.
          </p>
        </div>
      </div>

      <OpenQuestions
        items={[
          {
            code: 'Q6',
            text: 'Danh mục loại phí chính thức gồm những khoản nào? (PBI-013 nêu "phí quản lý, phí rác"; tài liệu giới thiệu nêu "phí dịch vụ, phí quản lý").',
            owner: 'Quân / Kiệt',
          },
          {
            code: 'Q7',
            text: 'Hiệu lực đơn giá tính theo ngày chính xác hay tròn tháng kỳ thu?',
            owner: 'Quân / Sơn',
          },
          {
            code: 'Q7.1',
            text: 'Thuật toán làm tròn tiền lẻ khi diện tích có phần thập phân: làm tròn từng dòng hay làm tròn trên tổng hóa đơn?',
            owner: 'Quân / Sơn',
          },
        ]}
      />

      {/* Modal thêm đơn giá */}
      {modalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6 border border-border">
            <h3 className="text-lg font-bold text-rail mb-3">Thêm mức đơn giá mới</h3>

            {errorMsg && (
              <div className="p-2 mb-3 bg-red-50 border border-red-200 rounded text-xs text-danger">
                {errorMsg}
              </div>
            )}

            <form onSubmit={handleSave} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Loại phí <span className="text-danger">*</span>
                </label>
                <select
                  value={feeType}
                  onChange={(e) => {
                    const val = e.target.value;
                    setFeeType(val);
                    setUnit(val === 'QUAN_LY' ? 'm2' : 'hộ');
                  }}
                  className="w-full px-3 py-2 border border-border rounded text-sm bg-white"
                >
                  <option value="QUAN_LY">Phí quản lý</option>
                  <option value="RAC">Phí rác sinh hoạt</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">Đơn vị tính</label>
                <input
                  type="text"
                  value={unit}
                  className="w-full px-3 py-2 border border-border rounded text-sm bg-slate-50"
                  readOnly
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Đơn giá (VND) <span className="text-danger">*</span>
                </label>
                <input
                  type="number"
                  min="1000"
                  step="500"
                  value={unitPrice}
                  onChange={(e) => setUnitPrice(e.target.value)}
                  className="w-full px-3 py-2 border border-border rounded text-sm font-mono"
                  required
                />
                <span className="text-xs text-slate mt-1 block">
                  Hiển thị: {formatVnd(unitPrice || '0')}
                </span>
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Hiệu lực từ ngày <span className="text-danger">*</span>
                </label>
                <input
                  type="date"
                  value={effectiveFrom}
                  onChange={(e) => setEffectiveFrom(e.target.value)}
                  className="w-full px-3 py-2 border border-border rounded text-sm font-mono"
                  required
                />
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setModalOpen(false)}
                  className="btn btn-secondary btn-sm"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
                >
                  Lưu đơn giá
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
