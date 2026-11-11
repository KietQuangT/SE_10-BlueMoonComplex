import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { actOnAgencyPackage, createAgencyPackage, downloadAgencyPackage, fetchAgencyPackage, fetchAgencyPackagePreview, fetchAgencyPackages } from '../../api/agencyPackages';
import type { AgencyPackageAction, AgencyPackageDetail, AgencyPackagePage, AgencyPackagePreview, AgencyPackageSummary } from '../../api/agencyPackages';
import { ApiHttpError } from '../../api/client';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { ScreenHeader } from '../../components/ScreenHeader';
import { EmptyStatePanel, ErrorRetryPanel, SkeletonRows } from '../../components/StateFrame';
import { formatInstantVn } from '../../lib/format';
import { useAppState } from '../../state/AppStateContext';

const PAGE_SIZE = 20;
const PREVIEW_SIZE = 10;

const STATUS_LABEL: Record<string, string> = {
  DRAFT: 'Chờ duyệt',
  APPROVED: 'Đã duyệt',
  REJECTED: 'Đã từ chối',
  REVOKED: 'Đã thu hồi',
  EXPIRED: 'Hết hạn',
};

const ACTION_COPY: Record<AgencyPackageAction, { title: string; label: string; consequence: string; tone?: 'primary' | 'danger' }> = {
  approve: { title: 'Duyệt gói dữ liệu', label: 'Duyệt gói', consequence: 'Gói sẽ được cho phép tải trong thời hạn còn lại. Việc duyệt được ghi vào nhật ký kiểm toán.' },
  reject: { title: 'Từ chối bản nháp', label: 'Từ chối bản nháp', consequence: 'Bản nháp sẽ bị từ chối và không thể tải xuống.', tone: 'danger' },
  revoke: { title: 'Thu hồi gói dữ liệu', label: 'Thu hồi gói', consequence: 'Gói sẽ không thể tải xuống nữa. Bản ghi kiểm toán vẫn được giữ.', tone: 'danger' },
  handoff: { title: 'Ghi nhận bàn giao ngoài hệ thống', label: 'Xác nhận đã bàn giao', consequence: 'Hệ thống ghi nhận BQT đã bàn giao gói cho cơ quan bên ngoài. Việc chuyển tệp diễn ra ngoài hệ thống.' },
};

function errorMessage(error: unknown): { message: string; requestId?: string } {
  if (error instanceof ApiHttpError) {
    return {
      message: error.problem.detail || error.problem.code || 'Không thể hoàn tất yêu cầu.',
      requestId: error.problem.requestId,
    };
  }
  return { message: 'Không thể kết nối máy chủ. Hãy thử lại.' };
}

function latestExpiration(pkg: AgencyPackageSummary): string {
  return pkg.downloadUntil || pkg.contentDeleteAfter;
}

function residencyLabel(value: string): string {
  if (value === 'RESIDING') return 'Đang cư trú';
  if (value === 'TEMPORARILY_ABSENT') return 'Tạm vắng';
  return value;
}

export function ReportPackagesPage(): React.ReactElement {
  const { mode, households, dataLoadError, reloadHouseholds } = useAppState();
  const activeHouseholds = useMemo(() => dataLoadError ? [] : households.filter((household) => household.status === 'ACTIVE'), [dataLoadError, households]);

  const [statusFilter, setStatusFilter] = useState('ALL');
  const [pageIndex, setPageIndex] = useState(0);
  const [pageData, setPageData] = useState<AgencyPackagePage | null>(null);
  const [loading, setLoading] = useState(true);
  const [refreshKey, setRefreshKey] = useState(0);
  const [error, setError] = useState<{ message: string; requestId?: string } | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [selected, setSelected] = useState<AgencyPackageDetail | null>(null);
  const [preview, setPreview] = useState<AgencyPackagePreview | null>(null);
  const [previewPage, setPreviewPage] = useState(0);
  const [previewError, setPreviewError] = useState<string | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [selectedHouseholds, setSelectedHouseholds] = useState<number[]>([]);
  const [agencyCode, setAgencyCode] = useState('');
  const [requestReference, setRequestReference] = useState('');
  const [licenceReference, setLicenceReference] = useState('');
  const [busy, setBusy] = useState(false);
  const [pendingAction, setPendingAction] = useState<AgencyPackageAction | null>(null);

  useEffect(() => {
    if (mode !== 'api') {
      setLoading(false);
      return;
    }
    let current = true;
    setLoading(true);
    setError(null);
    fetchAgencyPackages(statusFilter, pageIndex, PAGE_SIZE)
      .then((data) => { if (current) setPageData(data); })
      .catch((cause: unknown) => { if (current) setError(errorMessage(cause)); })
      .finally(() => { if (current) setLoading(false); });
    return () => { current = false; };
  }, [mode, statusFilter, pageIndex, refreshKey]);

  const loadDetail = useCallback(async (id: number, requestedPreviewPage = 0) => {
    setDetailLoading(true);
    setError(null);
    setPreviewError(null);
    try {
      const detail = await fetchAgencyPackage(id);
      setSelected(detail);
      setPreviewPage(requestedPreviewPage);
      if (detail.status === 'REJECTED' || detail.status === 'REVOKED' || detail.contentDeletedAt) {
        setPreview(null);
      } else {
        try {
          const rows = await fetchAgencyPackagePreview(id, requestedPreviewPage, PREVIEW_SIZE);
          setPreview(rows);
        } catch (cause) {
          setPreview(null);
          setPreviewError(errorMessage(cause).message);
        }
      }
    } catch (cause) {
      setError(errorMessage(cause));
    } finally {
      setDetailLoading(false);
    }
  }, []);

  const changePreviewPage = async (nextPage: number) => {
    if (!selected) return;
    setPreviewPage(nextPage);
    setPreviewError(null);
    try {
      setPreview(await fetchAgencyPackagePreview(selected.id, nextPage, PREVIEW_SIZE));
    } catch (cause) {
      setPreview(null);
      setPreviewError(errorMessage(cause).message);
    }
  };

  const toggleHousehold = (id: number) => {
    setSelectedHouseholds((current) => current.includes(id) ? current.filter((value) => value !== id) : [...current, id]);
  };

  const submitCreate = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (selectedHouseholds.length === 0) {
      setError({ message: 'Chọn ít nhất một hộ đang hoạt động.' });
      return;
    }
    setBusy(true);
    setError(null);
    setSuccess(null);
    try {
      const created = await createAgencyPackage({
        householdIds: selectedHouseholds,
        recipientAgencyCode: agencyCode.trim(),
        requestReference: requestReference.trim(),
        licenceReference: licenceReference.trim(),
      });
      setCreateOpen(false);
      setSelectedHouseholds([]);
      setAgencyCode('');
      setRequestReference('');
      setLicenceReference('');
      setSuccess(`Đã tạo bản nháp gói #${created.id}. Xem trước nội dung trước khi duyệt.`);
      setStatusFilter('ALL');
      setPageIndex(0);
      setRefreshKey((value) => value + 1);
      await loadDetail(created.id);
    } catch (cause) {
      setError(errorMessage(cause));
    } finally {
      setBusy(false);
    }
  };

  const confirmAction = async () => {
    if (!selected || !pendingAction) return;
    setBusy(true);
    setError(null);
    setSuccess(null);
    try {
      const updated = await actOnAgencyPackage(selected.id, pendingAction, selected.version, selected.contentSha256);
      const copy = ACTION_COPY[pendingAction];
      setPendingAction(null);
      setSuccess(`${copy.label}: gói #${updated.id}.`);
      setRefreshKey((value) => value + 1);
      await loadDetail(updated.id);
    } catch (cause) {
      setError(errorMessage(cause));
      setPendingAction(null);
    } finally {
      setBusy(false);
    }
  };

  const download = async () => {
    if (!selected) return;
    setBusy(true);
    setError(null);
    setSuccess(null);
    try {
      const file = await downloadAgencyPackage(selected.id);
      const url = URL.createObjectURL(file.blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = file.filename;
      document.body.append(anchor);
      anchor.click();
      anchor.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
      setSuccess(`Đã tải ${file.filename}. Bàn giao cho cơ quan được thực hiện ngoài hệ thống.`);
    } catch (cause) {
      setError(errorMessage(cause));
    } finally {
      setBusy(false);
    }
  };

  const header = (
    <ScreenHeader
      screenCode="M-19"
      contract="api-live"
      tierLabel="Mức C"
      title="Gói dữ liệu cơ quan"
      description="BQT tạo, kiểm tra và kiểm soát việc bàn giao gói dữ liệu. Cơ quan không đăng nhập hoặc truy vấn trực tiếp hệ thống."
      actions={mode === 'api' ? <button type="button" className="btn btn-primary" onClick={() => { setError(null); setCreateOpen(true); }}>Tạo gói dữ liệu</button> : undefined}
    />
  );

  if (mode !== 'api') {
    return (
      <div className="wayfinding-page">
        {header}
        <div className="notice-box notice-warning">
          Màn hình nhạy cảm này chỉ khả dụng ở chế độ Backend thật để bảo đảm kiểm soát phạm vi và ghi nhận kiểm toán tại máy chủ. Chuyển sang API mode để tiếp tục.
        </div>
      </div>
    );
  }

  if (loading && !pageData) return <div className="wayfinding-page">{header}<SkeletonRows rows={5} /></div>;
  if (error && !pageData) return <div className="wayfinding-page">{header}<ErrorRetryPanel message={error.message} requestId={error.requestId} onRetry={() => setRefreshKey((value) => value + 1)} /></div>;

  const list = pageData?.content ?? [];
  const actionCopy = pendingAction ? ACTION_COPY[pendingAction] : null;

  return (
    <div className="wayfinding-page">
      {header}

      <section className="notice-box notice-warning mb-6" aria-label="Quy trình xử lý yêu cầu cơ quan">
        <strong>Quy trình BQT:</strong> nhận yêu cầu và kiểm tra giấy phép bên ngoài hệ thống, tạo gói trong phạm vi hộ, xem trước rồi nhờ ADMIN khác duyệt. BQT tải tệp và bàn giao ngoài hệ thống; ứng dụng chỉ lưu mã tham chiếu tối thiểu.
      </section>

      {error && <div className="notice-box notice-warning mb-4" role="alert"><p>{error.message}</p>{error.requestId && <small>Mã yêu cầu: {error.requestId}</small>}</div>}
      {success && <div className="notice-box notice-success mb-4" role="status">{success}</div>}

      <section className="surface-box mb-6" aria-labelledby="package-list-title">
        <div className="box-toolbar">
          <h2 id="package-list-title" className="box-title">Các gói dữ liệu</h2>
          <div className="filter-row"><button type="button" className="btn btn-secondary btn-sm" disabled={loading} onClick={() => setRefreshKey((value) => value + 1)}>Tải lại</button>
            <label className="inline-field"><span className="field-hint">Trạng thái</span>
              <select className="select" value={statusFilter} onChange={(event) => { setStatusFilter(event.target.value); setPageIndex(0); }}>
                <option value="ALL">Tất cả</option><option value="DRAFT">Chờ duyệt</option><option value="APPROVED">Đã duyệt</option><option value="REJECTED">Đã từ chối</option><option value="REVOKED">Đã thu hồi</option>
              </select>
            </label>
          </div>
        </div>
        {loading ? <SkeletonRows rows={3} /> : list.length === 0 ? (
          <EmptyStatePanel title="Chưa có gói dữ liệu" description="Tạo bản nháp sau khi BQT đã tiếp nhận yêu cầu và kiểm tra giấy phép ngoài hệ thống." actionLabel="Tạo gói dữ liệu" onAction={() => setCreateOpen(true)} />
        ) : (
          <div className="overflow-x-auto"><table className="ledger-table">
            <thead><tr><th>Mã gói</th><th>Trạng thái</th><th>Hộ</th><th>Nhân khẩu</th><th>Tạo lúc</th><th>Hạn tải / xóa</th><th>Thao tác</th></tr></thead>
            <tbody>{list.map((pkg) => <tr key={pkg.id}>
              <td className="font-mono">#{pkg.id}</td><td>{STATUS_LABEL[pkg.status] || pkg.status}</td><td>{pkg.householdCount}</td><td>{pkg.residentCount}</td>
              <td>{formatInstantVn(pkg.createdAt)}</td><td>{formatInstantVn(latestExpiration(pkg))}</td>
              <td><button type="button" className="btn btn-secondary btn-sm" onClick={() => void loadDetail(pkg.id)}>Xem chi tiết</button></td>
            </tr>)}</tbody>
          </table></div>
        )}
        {pageData && pageData.totalPages > 1 && <nav className="filter-row justify-between mt-4" aria-label="Phân trang gói dữ liệu">
          <span>Trang {pageData.page + 1}/{pageData.totalPages} · {pageData.totalElements} gói</span>
          <div className="filter-row"><button type="button" className="btn btn-secondary btn-sm" disabled={pageIndex === 0} onClick={() => setPageIndex((value) => value - 1)}>Trước</button><button type="button" className="btn btn-secondary btn-sm" disabled={pageIndex + 1 >= pageData.totalPages} onClick={() => setPageIndex((value) => value + 1)}>Sau</button></div>
        </nav>}
      </section>

      {selected && <section className="surface-box mb-6" aria-labelledby="package-detail-title" aria-busy={detailLoading}>
        <div className="box-toolbar"><div><h2 id="package-detail-title" className="box-title">Gói #{selected.id} · {STATUS_LABEL[selected.status] || selected.status}</h2><p className="field-hint">Cơ quan: {selected.recipientAgencyCode} · Yêu cầu: {selected.requestReference} · Giấy phép: {selected.licenceReference}</p></div>
          <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setSelected(null); setPreview(null); }}>Đóng</button></div>
        <dl className="grid grid-cols-1 md:grid-cols-3 gap-4 mt-4"><div><dt className="field-hint">Phạm vi hộ</dt><dd>{selected.householdCount} hộ / {selected.householdIds.join(', ')}</dd></div><div><dt className="field-hint">Người tạo</dt><dd>Account #{selected.createdBy}</dd></div><div><dt className="field-hint">Tạo lúc</dt><dd>{formatInstantVn(selected.createdAt)}</dd></div><div><dt className="field-hint">Hạn tải</dt><dd>{formatInstantVn(selected.downloadUntil)}</dd></div><div><dt className="field-hint">Xóa nội dung sau</dt><dd>{formatInstantVn(selected.contentDeleteAfter)}</dd></div><div><dt className="field-hint">SHA-256</dt><dd className="font-mono text-xs" style={{ wordBreak: 'break-all' }}>{selected.contentSha256}</dd></div></dl>

        <div className="filter-row mt-4">
          {selected.canApprove && <button type="button" className="btn btn-primary" onClick={() => setPendingAction('approve')}>Duyệt gói</button>}
          {selected.canReject && <button type="button" className="btn btn-danger" onClick={() => setPendingAction('reject')}>Từ chối bản nháp</button>}
          {selected.canDownload && <button type="button" className="btn btn-secondary" disabled={busy} onClick={() => void download()}>Tải XLSX</button>}
          {selected.canHandoff && <button type="button" className="btn btn-secondary" onClick={() => setPendingAction('handoff')}>Ghi nhận bàn giao</button>}
          {selected.canRevoke && <button type="button" className="btn btn-danger" onClick={() => setPendingAction('revoke')}>Thu hồi</button>}
        </div>

        <div className="box-toolbar mt-5"><h3 className="box-title">Xem trước nội dung</h3>{preview && <span className="field-hint">{preview.totalElements} dòng · trang {preview.page + 1}/{Math.max(preview.totalPages, 1)}</span>}</div>
        {previewError ? <p className="field-hint" role="status">Bản xem trước chưa khả dụng: {previewError}</p> : preview ? (
          <><div className="overflow-x-auto"><table className="ledger-table"><thead><tr><th>Mã hộ</th><th>Căn hộ</th><th>Họ tên</th><th>Trạng thái cư trú</th></tr></thead><tbody>{preview.content.map((row, index) => <tr key={`${row.householdCode}-${row.fullName}-${index}`}><td>{row.householdCode}</td><td>{row.apartmentCode}</td><td>{row.fullName}</td><td>{residencyLabel(row.residencyStatus)}</td></tr>)}</tbody></table></div>
            {preview.totalPages > 1 && <div className="filter-row justify-between mt-3"><button type="button" className="btn btn-secondary btn-sm" disabled={previewPage === 0} onClick={() => void changePreviewPage(Math.max(0, previewPage - 1))}>Dòng trước</button><button type="button" className="btn btn-secondary btn-sm" disabled={previewPage + 1 >= preview.totalPages} onClick={() => void changePreviewPage(previewPage + 1)}>Dòng tiếp</button></div>}
          </>
        ) : detailLoading ? <SkeletonRows rows={3} /> : <p className="field-hint">Không có nội dung xem trước.</p>}
      </section>}

      {createOpen && <section className="surface-box mb-6" aria-labelledby="create-package-title">
        <div className="box-toolbar"><h2 id="create-package-title" className="box-title">Tạo bản nháp gói</h2><button type="button" className="btn btn-ghost btn-sm" onClick={() => setCreateOpen(false)}>Đóng</button></div>
        <p className="field-hint mb-4">Chỉ chọn hộ thuộc đúng yêu cầu đã tiếp nhận. Hệ thống lưu mã tham chiếu, không lưu bản giấy phép hoặc nội dung yêu cầu.</p>
        <form onSubmit={(event) => void submitCreate(event)}>
          <div className="grid gap-4 md:grid-cols-3">
            <label className="form-field" htmlFor="agency-code">Mã cơ quan nhận gói<input id="agency-code" className="input" required maxLength={80} value={agencyCode} onChange={(event) => setAgencyCode(event.target.value)} /></label>
            <label className="form-field" htmlFor="request-reference">Mã tham chiếu yêu cầu<input id="request-reference" className="input" required maxLength={120} value={requestReference} onChange={(event) => setRequestReference(event.target.value)} /></label>
            <label className="form-field" htmlFor="licence-reference">Mã tham chiếu giấy phép<input id="licence-reference" className="input" required maxLength={120} value={licenceReference} onChange={(event) => setLicenceReference(event.target.value)} /></label>
          </div>
          <fieldset className="field-options mt-5"><legend>Hộ trong phạm vi ({selectedHouseholds.length} đã chọn)</legend>
            {dataLoadError ? <div><p className="field-hint">Không tải được danh sách hộ. Tải lại danh sách trước khi tạo gói.</p><button type="button" className="btn btn-secondary btn-sm" onClick={() => void reloadHouseholds()}>Tải lại danh sách hộ</button></div> : activeHouseholds.length === 0 ? <p className="field-hint">Chưa có hộ đang hoạt động để chọn.</p> : activeHouseholds.map((household) => <label className="field-check" key={household.id}><input type="checkbox" checked={selectedHouseholds.includes(household.id)} onChange={() => toggleHousehold(household.id)} /><span><strong>{household.apartmentCode}</strong> · {household.householdCode} · {household.currentResidents ?? 0} nhân khẩu</span></label>)}
          </fieldset>
          <div className="filter-row mt-5"><button type="submit" className="btn btn-primary" disabled={busy || activeHouseholds.length === 0}>{busy ? 'Đang tạo...' : 'Tạo bản nháp'}</button><button type="button" className="btn btn-secondary" onClick={() => setCreateOpen(false)} disabled={busy}>Hủy</button></div>
        </form>
      </section>}

      {actionCopy && <ConfirmDialog open title={`${actionCopy.title} #${selected?.id ?? ''}`} confirmLabel={actionCopy.label} confirmTone={actionCopy.tone} consequence={actionCopy.consequence} busy={busy} onConfirm={() => void confirmAction()} onCancel={() => setPendingAction(null)} />}
    </div>
  );
}
