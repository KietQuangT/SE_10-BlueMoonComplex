import React, { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useAppState } from '../../state/AppStateContext';
import { ScreenHeader } from '../../components/ScreenHeader';
import { Icon } from '../../components/Icon';
import { ErrorPage } from '../common/ErrorPage';
import type { Household, ResidentMember } from '../../types/domain';

const AREA_RE = /^\d{1,8}(\.\d{1,2})?$/;

/** M-14 — Thêm/sửa hộ (mode household) và thêm nhân khẩu (mode resident). */
export function HouseholdFormPage({ mode }: { mode: 'household' | 'resident' }): React.ReactElement {
  const { id } = useParams();
  const { households, saveHousehold, addResident, mode: dataMode } = useAppState();
  const [saving, setSaving] = useState(false);
  const live = dataMode === 'api';
  const navigate = useNavigate();
  const existing = id ? households.find((h) => String(h.id) === id) : undefined;
  const isEdit = mode === 'household' && Boolean(id);

  const [hh, setHh] = useState({
    householdCode: existing?.householdCode ?? '',
    apartmentCode: existing?.apartmentCode ?? '',
    areaM2: existing?.areaM2 ?? '',
    address: existing?.address ?? '',
    status: (existing?.status ?? 'ACTIVE') as Household['status'],
  });
  const [rs, setRs] = useState({
    fullName: '',
    identityNumberRaw: '',
    dateOfBirth: '',
    phone: '',
    relationshipToHead: '',
    residencyStatus: 'Đang cư trú' as ResidentMember['residencyStatus'],
  });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState<string | null>(null);

  if ((id && !existing) || (mode === 'resident' && !existing)) return <ErrorPage code={404} />;

  const backTo = existing ? `/admin/households/${existing.id}` : '/admin/households';

  const submitHousehold = async (e: React.FormEvent) => {
    e.preventDefault();
    const next: Record<string, string> = {};
    if (!hh.householdCode.trim()) next.householdCode = 'Nhập mã hộ.';
    if (!hh.apartmentCode.trim()) next.apartmentCode = 'Nhập mã căn hộ.';
    const area = hh.areaM2.trim().replace(',', '.');
    if (!AREA_RE.test(area) || /^0+(\.0+)?$/.test(area)) next.areaM2 = 'Diện tích phải lớn hơn 0, tối đa 2 chữ số thập phân.';
    setErrors(next);
    setFormError(null);
    if (Object.keys(next).length) return;
    setSaving(true);
    const result = await saveHousehold({ ...hh, areaM2: area, id: existing?.id });
    setSaving(false);
    if (!result.ok) {
      setFormError(result.error ?? 'Không lưu được hộ.');
      return;
    }
    navigate(result.id && !existing ? `/admin/households/${result.id}` : backTo);
  };

  const submitResident = async (e: React.FormEvent) => {
    e.preventDefault();
    const next: Record<string, string> = {};
    if (!rs.fullName.trim()) next.fullName = 'Nhập họ tên nhân khẩu.';
    if (rs.identityNumberRaw && !/^\d{9,12}$/.test(rs.identityNumberRaw.trim()))
      next.identityNumberRaw = 'Số giấy tờ gồm 9 đến 12 chữ số.';
    setErrors(next);
    setFormError(null);
    if (Object.keys(next).length || !existing) return;
    setSaving(true);
    const result = await addResident({ householdId: existing.id, ...rs });
    setSaving(false);
    if (!result.ok) {
      setFormError(result.error ?? 'Không lưu được nhân khẩu.');
      return;
    }
    navigate(backTo);
  };

  const err = (key: string) =>
    errors[key] ? (
      <span id={`f-${key}-err`} className="field-error">
        {errors[key]}
      </span>
    ) : null;

  return (
    <>
      <Link to={backTo} className="btn btn-ghost btn-sm back-link">
        <Icon name="arrow-left" size={16} />
        Quay lại
      </Link>
      <ScreenHeader
        screenCode="M-14"
        title={
          mode === 'resident'
            ? `Thêm nhân khẩu cho hộ ${existing?.householdCode}`
            : isEdit
              ? `Sửa hộ ${existing?.householdCode}`
              : 'Thêm hộ gia đình'
        }
        contract={live ? 'api-live' : 'db-ready'}
        tierLabel="Mức B"
      />
      {formError ? (
        <div className="banner banner-error" role="alert">
          <div className="banner-content">
            <Icon name="alert" />
            <span>{formError}</span>
          </div>
        </div>
      ) : null}

      <section className="surface-box surface-box-pad">
        {mode === 'household' ? (
          <form onSubmit={submitHousehold} noValidate className="form-grid">
            <div className="form-field">
              <label className="field-label" htmlFor="f-householdCode">
                Mã hộ
              </label>
              <input
                id="f-householdCode"
                className="input tabular"
                maxLength={50}
                value={hh.householdCode}
                disabled={live && isEdit}
                onChange={(e) => setHh({ ...hh, householdCode: e.target.value })}
                aria-invalid={Boolean(errors.householdCode)}
                aria-describedby={errors.householdCode ? 'f-householdCode-err' : undefined}
              />
              {err('householdCode')}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-apartmentCode">
                Mã căn hộ
              </label>
              <input
                id="f-apartmentCode"
                className="input tabular"
                maxLength={50}
                value={hh.apartmentCode}
                disabled={live && isEdit}
                onChange={(e) => setHh({ ...hh, apartmentCode: e.target.value })}
                aria-invalid={Boolean(errors.apartmentCode)}
                aria-describedby={errors.apartmentCode ? 'f-apartmentCode-err' : undefined}
              />
              {err('apartmentCode')}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-areaM2">
                Diện tích (m²)
              </label>
              <input
                id="f-areaM2"
                className="input tabular"
                inputMode="decimal"
                placeholder="50.00"
                value={hh.areaM2}
                onChange={(e) => setHh({ ...hh, areaM2: e.target.value })}
                aria-invalid={Boolean(errors.areaM2)}
                aria-describedby={errors.areaM2 ? 'f-areaM2-err' : 'f-areaM2-hint'}
              />
              {errors.areaM2 ? (
                err('areaM2')
              ) : (
                <span id="f-areaM2-hint" className="field-hint">
                  Dùng để tính phí theo m²; hóa đơn cũ giữ diện tích lúc phát hành.
                </span>
              )}
            </div>
            {live ? null : (
            <div className="form-field">
              <label className="field-label" htmlFor="f-status">
                Trạng thái
              </label>
              <select
                id="f-status"
                className="select"
                value={hh.status}
                onChange={(e) => setHh({ ...hh, status: e.target.value as Household['status'] })}
              >
                <option value="ACTIVE">Hoạt động</option>
                <option value="INACTIVE">Không hoạt động</option>
              </select>
            </div>
            )}
            <div className="form-field span-2">
              <label className="field-label" htmlFor="f-address">
                Địa chỉ (tùy chọn)
              </label>
              <input
                id="f-address"
                className="input"
                maxLength={255}
                value={hh.address}
                onChange={(e) => setHh({ ...hh, address: e.target.value })}
              />
            </div>
            <div className="form-field span-2 form-actions">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {isEdit ? 'Lưu thay đổi' : 'Thêm hộ'}
              </button>
              <Link to={backTo} className="btn btn-secondary">
                Hủy
              </Link>
            </div>
          </form>
        ) : (
          <form onSubmit={submitResident} noValidate className="form-grid">
            <div className="form-field">
              <label className="field-label" htmlFor="f-fullName">
                Họ tên
              </label>
              <input
                id="f-fullName"
                className="input"
                maxLength={150}
                value={rs.fullName}
                onChange={(e) => setRs({ ...rs, fullName: e.target.value })}
                aria-invalid={Boolean(errors.fullName)}
                aria-describedby={errors.fullName ? 'f-fullName-err' : undefined}
              />
              {err('fullName')}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-identity">
                Số giấy tờ định danh (tùy chọn)
              </label>
              <input
                id="f-identity"
                className="input tabular"
                inputMode="numeric"
                autoComplete="off"
                value={rs.identityNumberRaw}
                onChange={(e) => setRs({ ...rs, identityNumberRaw: e.target.value })}
                aria-invalid={Boolean(errors.identityNumberRaw)}
                aria-describedby={errors.identityNumberRaw ? 'f-identityNumberRaw-err' : 'f-identity-hint'}
              />
              {errors.identityNumberRaw ? (
                err('identityNumberRaw')
              ) : (
                <span id="f-identity-hint" className="field-hint">
                  Sau khi lưu chỉ hiển thị 4 số cuối. Trùng với người đang cư trú sẽ bị từ chối.
                </span>
              )}
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-dob">
                Ngày sinh
              </label>
              <input
                id="f-dob"
                type="date"
                className="input"
                value={rs.dateOfBirth}
                onChange={(e) => setRs({ ...rs, dateOfBirth: e.target.value })}
              />
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-phone">
                Số điện thoại (tùy chọn)
              </label>
              <input
                id="f-phone"
                className="input tabular"
                inputMode="tel"
                autoComplete="off"
                maxLength={30}
                value={rs.phone}
                onChange={(e) => setRs({ ...rs, phone: e.target.value })}
              />
            </div>
            <div className="form-field">
              <label className="field-label" htmlFor="f-relation">
                Quan hệ với chủ hộ
              </label>
              <input
                id="f-relation"
                className="input"
                maxLength={100}
                placeholder="Chủ hộ, Vợ/Chồng, Con..."
                value={rs.relationshipToHead}
                onChange={(e) => setRs({ ...rs, relationshipToHead: e.target.value })}
              />
            </div>
            {live ? null : (
            <div className="form-field">
              <label className="field-label" htmlFor="f-residency">
                Trạng thái cư trú
              </label>
              <select
                id="f-residency"
                className="select"
                value={rs.residencyStatus}
                onChange={(e) =>
                  setRs({ ...rs, residencyStatus: e.target.value as ResidentMember['residencyStatus'] })
                }
              >
                <option value="Đang cư trú">Đang cư trú</option>
                <option value="Tạm vắng">Tạm vắng</option>
              </select>
              <span className="field-hint">Tập giá trị chờ chốt.</span>
            </div>
            )}
            <div className="form-field span-2 form-actions">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                Thêm nhân khẩu
              </button>
              <Link to={backTo} className="btn btn-secondary">
                Hủy
              </Link>
            </div>
          </form>
        )}
      </section>
    </>
  );
}
