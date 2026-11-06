import React from 'react';
import { useAppState } from '../../state/AppStateContext';
import { formatBusinessDate } from '../../lib/format';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel } from '../../components/StateFrame';

/** M-34 — Đợt đóng góp tự nguyện (cư dân chỉ xem). */
export function ResidentVoluntaryPage(): React.ReactElement {
  const { voluntaryCampaigns, forcedUiState } = useAppState();
  const published = forcedUiState === 'empty' ? [] : voluntaryCampaigns.filter((c) => c.status === 'PUBLISHED');

  return (
    <>
      <ScreenHeader
        screenCode="M-34"
        title="Đợt đóng góp tự nguyện"
        description="Các đợt vận động Ban quản trị đã xuất bản. Đóng góp bằng tiền mặt tại Văn phòng Ban quản lý."
        contract="assumed"
        tierLabel="Mức C"
      />
      {published.length === 0 ? (
        <EmptyStatePanel
          title="Hiện chưa có đợt đóng góp nào"
          description="Khi Ban quản trị mở đợt vận động mới, thông tin sẽ hiện ở đây."
        />
      ) : (
        published.map((c) => (
          <section key={c.id} className="surface-box surface-box-pad campaign-block">
            <div className="screen-title-row">
              <h2 className="box-title">{c.name}</h2>
              <span className="badge badge-paid">Đang mở</span>
              <span className="badge badge-neutral">Tự nguyện, không tính vào công nợ</span>
            </div>
            <p className="screen-desc">{c.description}</p>
            <dl className="detail-dl">
              <div>
                <dt>Thời gian</dt>
                <dd className="tabular">
                  {formatBusinessDate(c.opensOn)} – {formatBusinessDate(c.closesOn)}
                </dd>
              </div>
              <div>
                <dt>Cách đóng góp</dt>
                <dd>Tiền mặt qua Ban quản trị hoặc Kế toán</dd>
              </div>
              <div>
                <dt>Tổng đã đóng góp</dt>
                <dd>Chưa công bố (chờ quyết định hiển thị)</dd>
              </div>
            </dl>
          </section>
        ))
      )}
    </>
  );
}
