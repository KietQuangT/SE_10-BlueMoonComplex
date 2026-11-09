import type { Campaign } from '../../api/voluntary';
import { StatusBadge } from '../../components/Badge';
import { formatBusinessDate, formatVndDecimal } from '../../lib/format';

export function campaignStatus(campaign: Campaign) {
  const today = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit',
  }).format(new Date());
  if (campaign.status === 'DRAFT') return 'Nháp';
  if (campaign.status === 'CLOSED' || campaign.closesOn < today) return 'Đã kết thúc';
  return campaign.opensOn > today ? 'Sắp bắt đầu' : 'Đang mở';
}

export function CampaignCard({ campaign, resident, selected, disabled, onSelect }: {
  campaign: Campaign;
  resident: boolean;
  selected: boolean;
  disabled: boolean;
  onSelect?: () => void;
}) {
  const label = campaignStatus(campaign);
  return (
    <article className={`surface-box surface-box-pad s08-campaign${selected ? ' s08-campaign-selected' : ''}`} aria-labelledby={`campaign-${campaign.id}`}>
      <div className="s08-campaign-head">
        <div className="s08-campaign-copy">
          <div className="s08-title-row">
            <h2 id={`campaign-${campaign.id}`} tabIndex={-1} className="box-title">{campaign.name}</h2>
            <StatusBadge label={label} tone={label === 'Đang mở' ? 'paid' : label === 'Sắp bắt đầu' ? 'pending' : 'neutral'} />
            <span className="badge badge-neutral">Không tính vào công nợ</span>
          </div>
          {campaign.description && <p className="s08-description">{campaign.description}</p>}
        </div>
        {!resident && <dl className="s08-campaign-total">
          <dt>Tổng thu đợt này</dt>
          <dd className="tabular">{formatVndDecimal(campaign.totalCollected ?? '0')}</dd>
        </dl>}
      </div>
      <div className="s08-campaign-foot">
        <dl className="s08-campaign-dates">
          <div><dt>Thời gian mở</dt><dd><time dateTime={campaign.opensOn}>{formatBusinessDate(campaign.opensOn)}</time></dd></div>
          <div><dt>Hạn đóng</dt><dd><time dateTime={campaign.closesOn}>{formatBusinessDate(campaign.closesOn)}</time></dd></div>
          {!resident && <div><dt>Đã ghi nhận</dt><dd>{campaign.postedCount ?? 0} lượt đóng góp</dd></div>}
        </dl>
        {!resident && onSelect && <button type="button" className="btn btn-secondary" disabled={disabled}
          aria-expanded={selected} aria-controls={selected ? 'campaign-details' : undefined} aria-describedby={`campaign-${campaign.id}`} onClick={onSelect}>
          Xem chi tiết
        </button>}
      </div>
    </article>
  );
}
