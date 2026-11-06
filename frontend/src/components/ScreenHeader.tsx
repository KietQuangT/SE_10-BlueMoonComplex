import React from 'react';
import type { ContractBadgeKind } from '../types/domain';
import { ContractBadge } from './Badge';
import { Icon } from './Icon';

interface ScreenHeaderProps {
  screenCode: string;
  title: string;
  description?: string;
  contract: ContractBadgeKind;
  tierLabel?: 'Mức A' | 'Mức B' | 'Mức C';
  actions?: React.ReactNode;
}

export function ScreenHeader({
  screenCode,
  title,
  description,
  contract,
  tierLabel,
  actions,
}: ScreenHeaderProps): React.ReactElement {
  return (
    <>
      <header className="screen-header">
        <div className="screen-title-group">
          <div className="screen-title-row">
            <h1 className="screen-title">{title}</h1>
            <span className="badge badge-code">{screenCode}</span>
            <ContractBadge kind={contract} />
            {tierLabel ? <span className="badge badge-neutral">{tierLabel}</span> : null}
          </div>
          {description ? <p className="screen-desc">{description}</p> : null}
        </div>
        {actions ? <div className="filter-row">{actions}</div> : null}
      </header>
      {contract !== 'api-live' ? (
        <p className="mock-note">
          <Icon name="info" size={15} />
          Màn này dùng dữ liệu mẫu (mock-data.json) vì backend chưa có API tương ứng. Thao tác chỉ
          thay đổi trạng thái trong trình duyệt.
        </p>
      ) : null}
    </>
  );
}

export interface OpenQuestion {
  code: string;
  text: string;
  owner: string;
}

export function OpenQuestions({ items }: { items: OpenQuestion[] }): React.ReactElement {
  return (
    <section className="surface-box" aria-labelledby="open-questions-heading">
      <div className="box-toolbar">
        <h2 id="open-questions-heading" className="box-title">
          Câu hỏi mở cần chốt trước khi ghép API
        </h2>
        <span className="badge badge-pending">Chờ chốt quy tắc</span>
      </div>
      <ul className="question-list">
        {items.map((q) => (
          <li key={q.code}>
            <span className="badge badge-code">{q.code}</span>
            <span className="question-text">{q.text}</span>
            <span className="question-owner">{q.owner}</span>
          </li>
        ))}
      </ul>
    </section>
  );
}
