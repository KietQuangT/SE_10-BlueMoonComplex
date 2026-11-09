import { ConfirmDialog } from '../../components/ConfirmDialog';
import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatVnd, formatBusinessDate, formatPeriod, normalizeApartmentCode } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';
import type { InvoiceRecord, PaymentTransaction } from '../../types/domain';

export function CollectionsPage() {
  const { invoices, collectInvoiceCashDemo, forcedUiState } = useAppState();

  const [searchTerm, setSearchTerm] = useState('');
  const [selectedInvoice, setSelectedInvoice] = useState<InvoiceRecord | null>(null);
  const [note, setNote] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [recentReceipt, setRecentReceipt] = useState<PaymentTransaction | null>(null);
  const [errorBanner, setErrorBanner] = useState<string | null>(null);

  const unpaidInvoices = invoices.filter((i) => i.status === 'CHUA_DONG');

  const filteredInvoices = searchTerm.trim()
    ? unpaidInvoices.filter((i) => {
        const query = searchTerm.trim().toLowerCase();
        const normalized = normalizeApartmentCode(searchTerm.trim()).toLowerCase();
        return (
          i.apartmentCode.toLowerCase().includes(query) ||
          i.apartmentCode.toLowerCase().includes(normalized)
        );
      })
    : unpaidInvoices;

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorBanner(null);
  };

  const handleInitiateCollect = (inv: InvoiceRecord) => {
    setErrorBanner(null);
    setRecentReceipt(null);
    setSelectedInvoice(inv);
    setNote('');
  };

  const handleConfirmCollect = () => {
    if (!selectedInvoice) return;
    setIsSubmitting(true);

    try {
      const receipt = collectInvoiceCashDemo(selectedInvoice.id, note.trim() || undefined);
      setIsSubmitting(false);
      setSelectedInvoice(null);
      if (receipt) {
        setRecentReceipt(receipt);
      }
    } catch {
      setIsSubmitting(false);
      setErrorBanner('Khoản này đã được thu trước đó hoặc hệ thống đang xử lý giao dịch đồng thời.');
      setSelectedInvoice(null);
    }
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-21"
          contract="assumed"
          tierLabel="Mức C"
          title="Thu tiền mặt tại quầy"
          description="Ghi nhận thanh toán tiền mặt trực tiếp cho các hóa đơn phí dịch vụ."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-21"
          contract="assumed"
          tierLabel="Mức C"
          title="Thu tiền mặt tại quầy"
          description="Ghi nhận thanh toán tiền mặt trực tiếp cho các hóa đơn phí dịch vụ."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <ScreenHeader
        screenCode="M-21"
        contract="assumed"
        tierLabel="Mức C"
        title="Thu tiền mặt tại quầy"
        description="Tìm căn hộ, kiểm tra khoản còn nợ và xác nhận tiền mặt đã nhận."
      />

      {errorBanner && (
        <div className="notice-box notice-danger mb-6">
          <p>{errorBanner}</p>
        </div>
      )}

      {/* Thẻ tóm tắt biên lai sau khi thu thành công */}
      {recentReceipt && (
        <div className="station-panel p-5 bg-teal-50 border-2 border-teal-600 rounded-lg mb-6 shadow-sm">
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-bold uppercase tracking-wider text-teal-800">
              ✓ Đã ghi nhận thu tiền thành công (Biên lai thu phí)
            </span>
            <span className="text-xs font-mono text-slate">
              Mã giao dịch: TX-{recentReceipt.id}
            </span>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-3">
            <div>
              <span className="text-xs text-slate block">Căn hộ</span>
              <span className="font-mono font-bold text-lg text-rail">
                Căn {recentReceipt.apartmentCode}
              </span>
            </div>
            <div>
              <span className="text-xs text-slate block">Kỳ hóa đơn</span>
              <span className="font-mono font-bold text-lg text-rail">
                {formatPeriod(recentReceipt.period)}
              </span>
            </div>
            <div>
              <span className="text-xs text-slate block">Số tiền đã thu</span>
              <span className="font-mono font-bold text-lg text-teal-brand">
                {formatVnd(recentReceipt.amount)}
              </span>
            </div>
            <div>
              <span className="text-xs text-slate block">Người thu / Thời điểm</span>
              <span className="text-xs font-medium text-rail block">
                {recentReceipt.receivedBy}
              </span>
              <span className="text-xs font-mono text-slate">
                {new Date(recentReceipt.paidAt).toLocaleTimeString('vi-VN')}{' '}
                {new Date(recentReceipt.paidAt).toLocaleDateString('vi-VN')}
              </span>
            </div>
          </div>
          {recentReceipt.note && (
            <p className="text-xs text-slate mt-2 italic">Ghi chú: {recentReceipt.note}</p>
          )}
        </div>
      )}

      {/* Tìm kiếm căn hộ */}
      <div className="station-panel p-4 bg-white border border-border mb-6">
        <form onSubmit={handleSearchSubmit} className="filter-row">
          <label className="form-field flex-1">
            <span className="sr-only">Mã căn hộ cần thu</span>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Nhập mã căn hộ cần thu (Ví dụ: A01, A1, B05)..."
              className="w-full px-4 py-2 border border-border rounded text-sm font-mono"
            />
          </label>
          <button
            type="submit"
            className="btn btn-secondary btn-sm"
          >
            Tìm kiếm
          </button>
        </form>
        <span className="text-xs text-slate mt-2 block">
          * Gõ mã căn và bấm Enter hoặc chọn trực tiếp từ danh sách hóa đơn đang chờ nộp bên dưới.
        </span>
      </div>

      {/* Danh sách hóa đơn chưa đóng */}
      <div className="station-panel p-5 bg-white border border-border mb-8">
        <div className="station-strip bg-amber-stripe mb-4"></div>
        <div className="filter-row justify-between mb-3">
          <h2 className="text-base font-bold text-rail">
            Các khoản đang nợ phí ({filteredInvoices.length} khoản)
          </h2>
          <span className="text-xs text-slate">
            Kế toán chỉ xem mã căn và số tiền (không hiển thị CCCD/SĐT cư dân)
          </span>
        </div>

        {filteredInvoices.length === 0 ? (
          <EmptyStatePanel
            title="Không có khoản nợ nào"
            description={
              searchTerm.trim()
                ? `Không tìm thấy khoản nợ nào cho căn "${searchTerm}".`
                : 'Hiện tại tất cả các căn hộ đã hoàn thành nghĩa vụ đóng phí.'
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="ledger-table">
              <thead>
                <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                  <th className="py-2.5 px-3">Căn hộ</th>
                  <th className="py-2.5 px-3">Kỳ hóa đơn</th>
                  <th className="py-2.5 px-3">Chi tiết các khoản</th>
                  <th className="py-2.5 px-3 text-right">Tổng phải thu</th>
                  <th className="py-2.5 px-3">Hạn nộp</th>
                  <th className="py-2.5 px-3 text-center">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-border">
                {filteredInvoices.map((inv) => (
                  <tr key={inv.id} className="hover:bg-slate-50">
                    <td className="py-3 px-3 font-mono font-bold text-base text-rail">
                      {inv.apartmentCode}
                    </td>
                    <td className="py-3 px-3 font-mono text-sm">{formatPeriod(inv.period)}</td>
                    <td className="py-3 px-3 text-xs text-slate">
                      {inv.lines.map((l, idx) => (
                        <div key={idx}>
                          • {l.feeName}: {formatVnd(l.amount)}
                        </div>
                      ))}
                    </td>
                    <td className="py-3 px-3 text-right font-mono font-bold text-base text-amber-700">
                      {formatVnd(inv.remaining)}
                    </td>
                    <td className="py-3 px-3 font-mono text-xs text-slate">{formatBusinessDate(inv.dueDate)}</td>
                    <td className="py-3 px-3 text-center">
                      <button
                        onClick={() => handleInitiateCollect(inv)}
                        className="btn btn-primary btn-sm"
                      >
                        Đã thu tiền
                      </button>
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
            code: 'Q8',
            text: 'Quy định về thu thiếu hoặc thu thừa tiền (ví dụ cư dân chỉ nộp một phần hoặc đóng trước nhiều tháng)? Giao diện hiện tại cố định số tiền thu bằng đúng số nợ (chỉ đọc).',
            owner: 'Quân / Kiệt',
          },
          {
            code: 'Q8.1',
            text: 'Quy trình sửa/hủy giao dịch thu tiền nhầm căn hộ chưa được định nghĩa trong tài liệu yêu cầu.',
            owner: 'Quân / Sơn',
          },
          {
            code: 'Q20',
            text: 'Quyền tra cứu danh sách căn hộ cho Kế toán hiện đang bị hoãn trong ma trận F01 nhưng là yêu cầu bắt buộc để Kế toán chọn căn thu tiền.',
            owner: 'Sơn / Quân',
          },
        ]}
      />

      <ConfirmDialog open={!!selectedInvoice}
        title={`Xác nhận thu tiền căn ${selectedInvoice?.apartmentCode ?? ''}`}
        consequence={`Kỳ ${formatPeriod(selectedInvoice?.period)} · ${formatVnd(selectedInvoice?.remaining ?? '0')}. Kiểm đếm tiền mặt trước khi ghi nhận.`}
        confirmLabel="Xác nhận đã thu tiền" busy={isSubmitting}
        onCancel={() => setSelectedInvoice(null)} onConfirm={() => void handleConfirmCollect()}>
        <p className="field-hint">Số tiền bằng toàn bộ khoản còn nợ. Quy tắc thu thiếu/thừa đang chờ chốt.</p>
        <label className="form-field" htmlFor="collection-note">Ghi chú giao dịch (không bắt buộc)
          <input id="collection-note" className="input" value={note} disabled={isSubmitting} onChange={(e) => setNote(e.target.value)} />
        </label>
      </ConfirmDialog>
    </div>
  );
}
