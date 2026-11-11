import { FormDialog } from '../../components/FormDialog';
import { CampaignCard } from '../s08/CampaignCard';
import '../s08/s08.css';
import { useState } from 'react';
import { OpenQuestions, ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { SHOW_DEV_TOOLS } from '../../lib/devTools';
import { useAppState } from '../../state/AppStateContext';

export function VoluntaryAdminPage() {
  const { voluntaryCampaigns, publishVoluntaryCampaignDemo, forcedUiState } = useAppState();

  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);
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


  return (
    <div className="wayfinding-page">
      <ScreenHeader
          screenCode="M-17"
          contract="assumed"
          tierLabel="Mức C"
          title="Quản lý đợt vận động tự nguyện"
          description="Các khoản vận động không bắt buộc, không cộng vào công nợ chung cư."
          actions={<button
            onClick={handleOpenCreate}
            className="btn btn-primary"
          >
            <span>+ Tạo đợt vận động mới</span>
          </button>}
        />

      {SHOW_DEV_TOOLS && (
        <div className="notice-box notice-warning mb-6">
          <p className="font-semibold">Lưu ý quy tắc nghiệp vụ quan trọng</p>
          <p className="text-sm mt-1">
            Các khoản thu tự nguyện (quỹ đền ơn đáp nghĩa, vì người nghèo, khuyến học...) tuyệt đối <strong>không tính vào công nợ bắt buộc</strong> của hộ gia đình. Không tính tiền phạt hoặc chặn quyền lợi nếu cư dân không tham gia.
          </p>
        </div>
      )}

      {successBanner && (
        <div className="notice-box notice-success mb-6" role="status">
          <p>{successBanner}</p>
        </div>
      )}

      <div className="s08-campaign-list">
        {forcedUiState === 'empty' || voluntaryCampaigns.length === 0 ? <EmptyStatePanel
          title="Chưa có đợt vận động tự nguyện nào"
          description="Chọn Tạo đợt vận động mới để nhập nội dung và thời gian."
        /> : voluntaryCampaigns.map((camp) => <CampaignCard key={camp.id}
          campaign={{ ...camp, postedCount: camp.contributionsCount, totalCollected: camp.totalCollectedDisplay.replace(/\./g, '') }}
          resident={false}
          selected={selectedId === camp.id}
          disabled={false}
          onSelect={() => setSelectedId((prev) => (prev === camp.id ? null : camp.id))}
        />)}
      </div>

      {selectedId ? (() => {
        const selected = voluntaryCampaigns.find((c) => c.id === selectedId);
        if (!selected) return null;
        return (
          <section id="campaign-details" className="surface-box s08-details mb-6" aria-labelledby="campaign-detail-title">
            <div className="box-toolbar">
              <div>
                <h2 id="campaign-detail-title" className="box-title">Chi tiết: {selected.name}</h2>
                <p className="field-hint">Đang mở · {selected.opensOn} – {selected.closesOn}</p>
              </div>
              <button type="button" className="btn btn-ghost" onClick={() => setSelectedId(null)}>
                Đóng chi tiết
              </button>
            </div>
            <section className="s08-ledger" aria-labelledby="contribution-ledger-title">
              <div className="s08-section">
                <h3 id="contribution-ledger-title" className="box-title">Sổ đóng góp</h3>
                <p className="field-hint">Khoản đã hủy vẫn lưu lịch sử và không tính vào tổng thu.</p>
              </div>
              <p className="s08-section">Đã ghi nhận {selected.contributionsCount} lượt đóng góp ({selected.totalCollectedDisplay} đ).</p>
            </section>
          </section>
        );
      })() : null}

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

      <FormDialog open={createModalOpen} title="Tạo đợt vận động tự nguyện mới"
        description="Đợt được công bố trong chế độ Demo, không tạo khoản nợ cho hộ gia đình."
        submitLabel="Công bố đợt" onSubmit={handlePublish} onCancel={() => setCreateModalOpen(false)}>
        <label className="form-field" htmlFor="demo-campaign-name">Tên đợt vận động
          <input id="demo-campaign-name" className="input" required maxLength={150} value={name} onChange={(e) => setName(e.target.value)} />
        </label>
        <label className="form-field" htmlFor="demo-campaign-description">Mục đích / Nội dung vận động
          <textarea id="demo-campaign-description" className="textarea" rows={3} maxLength={2000} value={description} onChange={(e) => setDescription(e.target.value)} />
        </label>
        <div className="form-grid">
          <label className="form-field">Ngày bắt đầu<input className="input" type="date" required max={closesOn || undefined} value={opensOn} onChange={(e) => setOpensOn(e.target.value)} /></label>
          <label className="form-field">Ngày kết thúc<input className="input" type="date" required min={opensOn || undefined} value={closesOn} onChange={(e) => setClosesOn(e.target.value)} /></label>
        </div>
      </FormDialog>
    </div>
  );
}
