import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatVnd } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

export function CollectionRatePage() {
  const { invoices, forcedUiState } = useAppState();

  const [selectedPeriod, setSelectedPeriod] = useState('2026-10');
  const [exporting, setExporting] = useState(false);
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  // Compute stats for selected period
  const periodInvoices = invoices.filter((i) => i.period === selectedPeriod);
  const periodTotalRequired = periodInvoices.reduce((sum, inv) => sum + BigInt(inv.total), 0n);
  const periodTotalPaid = periodInvoices.reduce((sum, inv) => sum + BigInt(inv.paid), 0n);
  const periodTotalRemaining = periodInvoices.reduce((sum, inv) => sum + BigInt(inv.remaining), 0n);

  const ratePercent =
    periodTotalRequired > 0n
      ? ((Number(periodTotalPaid) / Number(periodTotalRequired)) * 100).toFixed(1)
      : '0.0';

  const handleExport = () => {
    setExporting(true);
    setTimeout(() => {
      setExporting(false);
      setSuccessBanner(
        `Đã xuất tệp "Bao-cao-ty-le-thu-${selectedPeriod}.xlsx" chứa tổng thu, tổng nợ và tỷ lệ thu.`,
      );
    }, 700);
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-22"
          contract="assumed"
          tierLabel="Mức C"
          title="Báo cáo tỷ lệ thu phí"
          description="Thống kê tỷ lệ hoàn thành thu các khoản phí dịch vụ theo từng kỳ."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-22"
          contract="assumed"
          tierLabel="Mức C"
          title="Báo cáo tỷ lệ thu phí"
          description="Thống kê tỷ lệ hoàn thành thu các khoản phí dịch vụ theo từng kỳ."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <ScreenHeader
          screenCode="M-22"
          contract="assumed"
          tierLabel="Mức C"
          title="Báo cáo tỷ lệ thu phí"
          description="PBI-021: Theo dõi tỷ lệ hoàn thành thu phí và kết xuất bảng tổng hợp kế toán."
        />
        <div>
          <button
            onClick={handleExport}
            disabled={exporting || periodInvoices.length === 0}
            className="btn btn-primary"
          >
            <span>{exporting ? 'Đang tạo bảng tính...' : '⤓ Xuất bảng Excel'}</span>
          </button>
        </div>
      </div>

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Công thức tỷ lệ thu: Chờ chốt quy tắc</p>
        <p className="text-sm mt-1">
          Chưa có quy định chính thức về mẫu số của tỷ lệ: tính theo tổng số tiền phải thu bắt buộc hay tính theo số lượng căn hộ? Các khoản tự nguyện hiện được hạch toán tách rời và không nằm trong mẫu số này.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {/* Bộ chọn kỳ */}
      <div className="station-panel p-4 bg-white border border-border mb-6">
        <div className="flex flex-wrap items-center gap-4">
          <div>
            <label className="block text-xs font-bold uppercase text-slate mb-1">
              Chọn kỳ thu phí
            </label>
            <select
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(e.target.value)}
              className="px-4 py-2 border border-border rounded text-sm font-mono bg-white"
            >
              <option value="2026-10">Kỳ 10/2026</option>
              <option value="2026-09">Kỳ 09/2026</option>
              <option value="2026-08">Kỳ 08/2026</option>
            </select>
          </div>
          <div className="text-xs text-slate pt-4">
            Số hóa đơn phát hành trong kỳ:{' '}
            <strong className="text-rail font-mono">{periodInvoices.length} căn</strong>
          </div>
        </div>
      </div>

      {periodInvoices.length === 0 ? (
        <EmptyStatePanel
          title="Không có dữ liệu hóa đơn"
          description={`Kỳ ${selectedPeriod} chưa có hóa đơn nào được phát hành.`}
        />
      ) : (
        <>
          {/* Ba con số tổng quan */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
            <div className="station-panel p-5 bg-white border border-border">
              <div className="station-strip bg-teal-brand mb-3"></div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate block">
                Tổng tiền đã thu
              </span>
              <span className="text-2xl font-bold font-mono text-teal-brand mt-1 block">
                {formatVnd(periodTotalPaid.toString())}
              </span>
              <span className="text-xs text-slate mt-2 block">
                Đã gạch nợ vào sổ quỹ
              </span>
            </div>

            <div className="station-panel p-5 bg-white border border-border">
              <div className="station-strip bg-amber-stripe mb-3"></div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate block">
                Tổng công nợ còn lại
              </span>
              <span className="text-2xl font-bold font-mono text-amber-700 mt-1 block">
                {formatVnd(periodTotalRemaining.toString())}
              </span>
              <span className="text-xs text-slate mt-2 block">
                Chưa thanh toán hoặc quá hạn
              </span>
            </div>

            <div className="station-panel p-5 bg-white border border-border">
              <div className="station-strip bg-rail mb-3"></div>
              <span className="text-xs font-bold uppercase tracking-wider text-slate block">
                Tỷ lệ hoàn thành thu
              </span>
              <span className="text-3xl font-bold font-mono text-rail mt-1 block">
                {ratePercent} %
              </span>
              <span className="text-xs text-slate mt-2 block">
                Công thức: Tổng thu / Tổng phải thu bắt buộc
              </span>
            </div>
          </div>

          {/* Bảng chi tiết từng căn */}
          <div className="station-panel p-5 bg-white border border-border mb-8">
            <h3 className="font-bold text-rail text-sm mb-3">
              Chi tiết theo từng căn hộ trong kỳ {selectedPeriod}
            </h3>
            <div className="overflow-x-auto">
              <table className="ledger-table">
                <thead>
                  <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                    <th className="py-2.5 px-3">Căn hộ</th>
                    <th className="py-2.5 px-3 text-right">Phải thu</th>
                    <th className="py-2.5 px-3 text-right">Đã nộp</th>
                    <th className="py-2.5 px-3 text-right">Còn nợ</th>
                    <th className="py-2.5 px-3">Trạng thái</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {periodInvoices.map((inv) => (
                    <tr key={inv.id} className="hover:bg-slate-50">
                      <td className="py-2.5 px-3 font-mono font-bold text-rail">
                        Căn {inv.apartmentCode}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono">{formatVnd(inv.total)}</td>
                      <td className="py-2.5 px-3 text-right font-mono text-teal-brand font-bold">
                        {formatVnd(inv.paid)}
                      </td>
                      <td className="py-2.5 px-3 text-right font-mono text-amber-700 font-bold">
                        {formatVnd(inv.remaining)}
                      </td>
                      <td className="py-2.5 px-3">
                        <span
                          className={`text-xs px-2 py-0.5 rounded font-bold ${
                            inv.status === 'DA_DONG'
                              ? 'bg-teal-50 text-teal-brand border border-teal-200'
                              : 'bg-amber-50 text-amber-700 border border-amber-200'
                          }`}
                        >
                          {inv.status === 'DA_DONG' ? 'Đã đóng đủ' : 'Chưa đóng'}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}

      <OpenQuestions
        items={[
          {
            code: 'Q10',
            text: 'Công thức tính tỷ lệ thu phí tính theo số tiền (Doanh thu thu được / Doanh thu dự kiến) hay theo số căn hộ (Số hộ đã đóng / Tổng số hộ)?',
            owner: 'Quân / Sơn',
          },
          {
            code: 'Q10.1',
            text: 'Các khoản thu nộp muộn của các kỳ trước được kết chuyển vào tỷ lệ của kỳ phát sinh hay kỳ hiện tại?',
            owner: 'Quân / Kiệt',
          },
        ]}
      />
    </div>
  );
}
