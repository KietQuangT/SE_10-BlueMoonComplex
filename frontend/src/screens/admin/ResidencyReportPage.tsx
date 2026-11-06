import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';

export function ResidencyReportPage() {
  const { households, residents, forcedUiState } = useAppState();

  const [selectedStatus, setSelectedStatus] = useState<string>('ALL');
  const [selectedHousehold, setSelectedHousehold] = useState<string>('ALL');
  const [downloading, setDownloading] = useState(false);
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const filteredResidents = residents.filter((r) => {
    if (selectedStatus !== 'ALL' && r.residencyStatus !== selectedStatus) return false;
    if (selectedHousehold !== 'ALL' && String(r.householdId) !== selectedHousehold) return false;
    return true;
  });

  const handleExport = () => {
    setDownloading(true);
    setTimeout(() => {
      setDownloading(false);
      setSuccessBanner(
        `Đã xuất tệp dữ liệu "Bao-cao-luu-tru-${new Date().toISOString().slice(0, 10)}.xlsx" (${filteredResidents.length} dòng).`,
      );
    }, 800);
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-18"
          contract="assumed"
          tierLabel="Mức C"
          title="Xuất báo cáo lưu trú"
          description="Kết xuất danh sách cư trú và nhân khẩu căn hộ theo định dạng bảng tính."
        />
        <SkeletonRows rows={5} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-18"
          contract="assumed"
          tierLabel="Mức C"
          title="Xuất báo cáo lưu trú"
          description="Kết xuất danh sách cư trú và nhân khẩu căn hộ theo định dạng bảng tính."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <ScreenHeader
          screenCode="M-18"
          contract="assumed"
          tierLabel="Mức C"
          title="Xuất báo cáo lưu trú"
          description="PBI-022: Tải xuống danh sách nhân khẩu cư trú tại chung cư BlueMoon theo mẫu tổng hợp."
        />
        <div>
          <button
            onClick={handleExport}
            disabled={downloading || filteredResidents.length === 0}
            className="px-4 py-2 bg-teal-brand text-white font-semibold rounded hover:bg-teal-700 disabled:opacity-50 transition flex items-center gap-2"
          >
            <span>{downloading ? 'Đang tạo file Excel...' : '⤓ Tải xuống tệp Excel'}</span>
          </button>
        </div>
      </div>

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">🟡 Lưu ý về mẫu chuẩn báo cáo pháp lý (Q17)</p>
        <p className="text-sm mt-1">
          Hiện tại chưa có mẫu danh mục cột chính thức được cơ quan công an địa phương phê duyệt. Giao diện dưới đây hiển thị bản xem trước theo cấu trúc trường hiện có trong hệ thống và chưa cam kết đạt chuẩn pháp lý nhà nước.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {/* Bộ lọc */}
      <div className="station-panel p-4 bg-white border border-border mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-bold uppercase text-slate mb-1">Hộ gia đình</label>
            <select
              value={selectedHousehold}
              onChange={(e) => setSelectedHousehold(e.target.value)}
              className="w-full px-3 py-2 border border-border rounded text-sm bg-white"
            >
              <option value="ALL">-- Tất cả các hộ --</option>
              {households.map((h) => (
                <option key={h.id} value={h.id}>
                  Căn {h.apartmentCode} ({h.householdCode})
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase text-slate mb-1">Trạng thái cư trú</label>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="w-full px-3 py-2 border border-border rounded text-sm bg-white"
            >
              <option value="ALL">-- Tất cả trạng thái --</option>
              <option value="Đang cư trú">Đang cư trú</option>
              <option value="Tạm vắng">Tạm vắng</option>
              <option value="Đã chuyển đi">Đã chuyển đi (Lưu trữ)</option>
            </select>
          </div>

          <div className="flex items-end">
            <div className="text-xs text-slate">
              Khớp dữ liệu: <strong className="text-rail">{filteredResidents.length}</strong> nhân khẩu
            </div>
          </div>
        </div>
      </div>

      {/* Xem trước danh sách xuất */}
      {filteredResidents.length === 0 ? (
        <EmptyStatePanel
          title="Không có nhân khẩu nào khớp bộ lọc"
          description="Thử chọn lại trạng thái cư trú hoặc hộ gia đình khác để xem danh sách xuất."
        />
      ) : (
        <div className="station-panel p-5 bg-white border border-border mb-8 overflow-x-auto">
          <div className="flex justify-between items-center mb-3">
            <h3 className="font-bold text-rail text-sm">Xem trước dữ liệu sẽ xuất ({filteredResidents.length} dòng)</h3>
            <span className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate font-mono">
              Placeholder Mẫu cột: chờ chốt (Q17)
            </span>
          </div>

          <table className="w-full text-left text-sm border-collapse">
            <thead>
              <tr className="border-b border-border bg-ground text-slate text-xs uppercase font-bold">
                <th className="py-2.5 px-3">Căn hộ</th>
                <th className="py-2.5 px-3">Họ và tên</th>
                <th className="py-2.5 px-3">Quan hệ với chủ hộ</th>
                <th className="py-2.5 px-3">Ngày sinh</th>
                <th className="py-2.5 px-3">CCCD/Định danh</th>
                <th className="py-2.5 px-3">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filteredResidents.map((r) => {
                const hh = households.find((h) => h.id === r.householdId);
                return (
                  <tr key={r.id} className="hover:bg-slate-50">
                    <td className="py-2.5 px-3 font-mono font-bold text-teal-brand">
                      {hh?.apartmentCode || '-'}
                    </td>
                    <td className="py-2.5 px-3 font-medium text-rail">{r.fullName}</td>
                    <td className="py-2.5 px-3 text-slate">{r.relationshipToHead}</td>
                    <td className="py-2.5 px-3 font-mono text-xs">{r.dateOfBirth}</td>
                    <td className="py-2.5 px-3 font-mono text-xs text-slate">
                      {r.identityNumberMasked || 'Chưa cung cấp'}
                    </td>
                    <td className="py-2.5 px-3">
                      <span className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate font-medium">
                        {r.residencyStatus}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      <OpenQuestions
        items={[
          {
            code: 'Q17',
            text: 'Định dạng và danh sách cột chuẩn của báo cáo lưu trú theo yêu cầu của công an phường gồm những trường cụ thể nào?',
            owner: 'Sơn / Công an',
          },
          {
            code: 'Q17.1',
            text: 'Số CCCD khi xuất báo cáo lưu trú cho BQT có được hiện đầy đủ hay vẫn phải che mờ (masking) theo chính sách bảo vệ dữ liệu cá nhân?',
            owner: 'Tuấn / Kiệt',
          },
        ]}
      />
    </div>
  );
}
