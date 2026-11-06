import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { useAppState } from '../../state/AppStateContext';

export function VoluntaryAdminPage() {
  const { voluntaryCampaigns, publishVoluntaryCampaignDemo, forcedUiState } = useAppState();

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [opensOn, setOpensOn] = useState('2026-11-01');
  const [closesOn, setClosesOn] = useState('2026-11-30');
  const [successBanner, setSuccessBanner] = useState<string | null>(null);

  const handleOpenCreate = () => {
    setName('');
    setDescription('');
    setOpensOn('2026-11-01');
    setClosesOn('2026-11-30');
    setCreateModalOpen(true);
  };

  const handlePublish = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    publishVoluntaryCampaignDemo(name, description, opensOn, closesOn);
    setCreateModalOpen(false);
    setSuccessBanner(`Đã xuất bản đợt vận động tự nguyện "${name.trim()}". Đợt này sẽ hiển thị trên ứng dụng cư dân.`);
  };

  if (forcedUiState === 'loading') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-17"
          contract="assumed"
          tierLabel="Mức C"
          title="Quản lý đợt vận động tự nguyện"
          description="Tạo, xuất bản và theo dõi các đợt vận động đóng góp từ thiện, vì người nghèo, khuyến học."
        />
        <SkeletonRows rows={4} />
      </div>
    );
  }

  if (forcedUiState === 'error') {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-17"
          contract="assumed"
          tierLabel="Mức C"
          title="Quản lý đợt vận động tự nguyện"
          description="Tạo, xuất bản và theo dõi các đợt vận động đóng góp từ thiện, vì người nghèo, khuyến học."
        />
        <ErrorRetryPanel onRetry={() => window.location.reload()} />
      </div>
    );
  }

  if (forcedUiState === 'empty' || voluntaryCampaigns.length === 0) {
    return (
      <div className="wayfinding-page">
        <ScreenHeader
          screenCode="M-17"
          contract="assumed"
          tierLabel="Mức C"
          title="Quản lý đợt vận động tự nguyện"
          description="Tạo, xuất bản và theo dõi các đợt vận động đóng góp từ thiện, vì người nghèo, khuyến học."
        />
        <EmptyStatePanel
          title="Chưa có đợt vận động tự nguyện nào"
          description="Tạo đợt vận động mới để hiển thị thông báo kêu gọi ủng hộ tới ứng dụng cư dân."
          actionLabel="+ Tạo đợt vận động mới"
          onAction={handleOpenCreate}
        />
      </div>
    );
  }

  return (
    <div className="wayfinding-page">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-4">
        <ScreenHeader
          screenCode="M-17"
          contract="assumed"
          tierLabel="Mức C"
          title="Quản lý đợt vận động tự nguyện"
          description="PBI-012: Các khoản vận động không bắt buộc, không cộng vào công nợ chung cư."
        />
        <div>
          <button
            onClick={handleOpenCreate}
            className="btn btn-primary"
          >
            <span>+ Tạo đợt vận động mới</span>
          </button>
        </div>
      </div>

      <div className="notice-box notice-warning mb-6">
        <p className="font-semibold">Lưu ý quy tắc nghiệp vụ quan trọng</p>
        <p className="text-sm mt-1">
          Các khoản thu tự nguyện (quỹ đền ơn đáp nghĩa, vì người nghèo, khuyến học...) tuyệt đối <strong>không tính vào công nợ bắt buộc</strong> của hộ gia đình. Không tính tiền phạt hoặc chặn quyền lợi nếu cư dân không tham gia.
        </p>
      </div>

      {successBanner && (
        <div className="notice-box notice-success mb-6">
          <p>{successBanner}</p>
        </div>
      )}

      {/* Danh sách đợt vận động */}
      <div className="space-y-4 mb-8">
        {voluntaryCampaigns.map((camp) => (
          <div key={camp.id} className="station-panel p-5 bg-white border border-border">
            <div className="flex flex-col md:flex-row md:items-start justify-between gap-4">
              <div>
                <div className="flex items-center gap-3">
                  <h2 className="text-lg font-bold text-rail">{camp.name}</h2>
                  <span className="text-xs px-2.5 py-0.5 rounded-full font-bold uppercase bg-teal-50 text-teal-brand border border-teal-200">
                    {camp.status === 'PUBLISHED' ? 'Đã xuất bản' : camp.status}
                  </span>
                  <span className="text-xs px-2 py-0.5 rounded bg-amber-50 text-amber-700 border border-amber-200 font-semibold">
                    Không tính vào công nợ
                  </span>
                </div>
                <p className="text-sm text-slate mt-2">{camp.description}</p>
                <div className="flex flex-wrap gap-4 text-xs text-slate mt-3">
                  <div>
                    <span className="font-semibold text-rail">Thời gian mở:</span> {camp.opensOn}
                  </div>
                  <div>
                    <span className="font-semibold text-rail">Hạn đóng:</span> {camp.closesOn}
                  </div>
                  <div>
                    <span className="font-semibold text-rail">Đã ghi nhận:</span>{' '}
                    <span className="font-bold text-teal-brand">{camp.contributionsCount} lượt đóng góp</span>
                  </div>
                </div>
              </div>

              <div className="text-right">
                <span className="text-xs text-slate block uppercase font-bold">Tổng thu đợt này</span>
                <span className="text-xl font-bold font-mono text-teal-brand">
                  {camp.totalCollectedDisplay} đ
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      <OpenQuestions
        items={[
          {
            code: 'Q12',
            text: 'Ai là người trực tiếp thu và ghi nhận các khoản tự nguyện: Ban Quản Trị, Tổ dân phố hay Kế toán chung cư?',
            owner: 'Sơn / Quân',
          },
          {
            code: 'Q12.1',
            text: 'Khi cư dân nộp tiền mặt cho đợt tự nguyện, có cần xuất biên lai riêng ngoài hóa đơn dịch vụ hàng tháng không?',
            owner: 'Quân / Kiệt',
          },
          {
            code: 'Q12.2',
            text: 'Chưa có bảng riêng trong database cho các đợt vận động và danh sách đóng góp (đang là giả định UI theo PBI-012).',
            owner: 'Tuấn / Kiệt',
          },
        ]}
      />

      {/* Modal tạo đợt mới */}
      {createModalOpen && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-lg w-full p-6 border border-border">
            <h3 className="text-lg font-bold text-rail mb-3">Tạo đợt vận động tự nguyện mới</h3>
            <form onSubmit={handlePublish} className="space-y-4">
              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Tên đợt vận động <span className="text-danger">*</span>
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Ví dụ: Quỹ vì người nghèo năm 2026"
                  className="w-full px-3 py-2 border border-border rounded text-sm"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold uppercase text-slate mb-1">
                  Mục đích / Nội dung vận động
                </label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={3}
                  placeholder="Mô tả mục đích sử dụng số tiền quyên góp..."
                  className="w-full px-3 py-2 border border-border rounded text-sm"
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-bold uppercase text-slate mb-1">
                    Ngày bắt đầu <span className="text-danger">*</span>
                  </label>
                  <input
                    type="date"
                    value={opensOn}
                    onChange={(e) => setOpensOn(e.target.value)}
                    className="w-full px-3 py-2 border border-border rounded text-sm font-mono"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase text-slate mb-1">
                    Ngày kết thúc <span className="text-danger">*</span>
                  </label>
                  <input
                    type="date"
                    value={closesOn}
                    onChange={(e) => setClosesOn(e.target.value)}
                    className="w-full px-3 py-2 border border-border rounded text-sm font-mono"
                    required
                  />
                </div>
              </div>

              <div className="p-3 bg-amber-50 rounded border border-amber-200 text-xs text-amber-900">
                Lưu ý: Đợt vận động sau khi xuất bản sẽ hiển thị công khai tới mọi cư dân nhưng hoàn toàn không bắt buộc và không ghi nợ.
              </div>

              <div className="flex justify-end gap-3 pt-3">
                <button
                  type="button"
                  onClick={() => setCreateModalOpen(false)}
                  className="btn btn-secondary btn-sm"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="btn btn-primary btn-sm"
                >
                  Xuất bản ngay
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
