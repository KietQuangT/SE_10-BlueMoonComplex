import { ApiHttpError, getAuthenticated, parseProblem, sendAuthenticatedWrite } from './client';

const BASE = '/api/v1/agency-data-packages';
const XLSX_MIME = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';

export interface AgencyPackageSummary {
  id: number;
  status: string;
  version: number;
  createdAt: string;
  createdBy: number;
  approvedAt: string | null;
  approvedBy: number | null;
  downloadUntil: string | null;
  contentDeleteAfter: string;
  contentDeletedAt: string | null;
  householdCount: number;
  residentCount: number;
  handedOffAt: string | null;
  revokedAt: string | null;
  canApprove: boolean;
  canReject: boolean;
  canDownload: boolean;
  canRevoke: boolean;
  canHandoff: boolean;
}

export interface AgencyPackageDetail extends AgencyPackageSummary {
  templateId: string;
  templateVersion: string;
  householdIds: number[];
  recipientAgencyCode: string;
  requestReference: string;
  licenceReference: string;
  contentSha256: string;
  rejectedBy: number | null;
  rejectedAt: string | null;
  revokedBy: number | null;
  handedOffBy: number | null;
}

export interface AgencyPackagePage {
  content: AgencyPackageSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AgencyPackageRow {
  householdCode: string;
  apartmentCode: string;
  fullName: string;
  residencyStatus: string;
}

export interface AgencyPackagePreview {
  packageId: number;
  version: number;
  contentSha256: string;
  content: AgencyPackageRow[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CreateAgencyPackageRequest {
  householdIds: number[];
  recipientAgencyCode: string;
  requestReference: string;
  licenceReference: string;
}

export type AgencyPackageAction = 'approve' | 'reject' | 'revoke' | 'handoff';

function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && String(value).trim() !== '') search.set(key, String(value));
  }
  const encoded = search.toString();
  return encoded ? `?${encoded}` : '';
}

export const fetchAgencyPackages = (status: string, page: number, size = 20) =>
  getAuthenticated<AgencyPackagePage>(`${BASE}${query({ status: status === 'ALL' ? undefined : status, page, size })}`);

export const fetchAgencyPackage = (id: number) =>
  getAuthenticated<AgencyPackageDetail>(`${BASE}/${id}`);

export const fetchAgencyPackagePreview = (id: number, page: number, size = 10) =>
  getAuthenticated<AgencyPackagePreview>(`${BASE}/${id}/preview${query({ page, size })}`);

export const createAgencyPackage = (body: CreateAgencyPackageRequest) =>
  sendAuthenticatedWrite<AgencyPackageDetail>(BASE, 'POST', body);

export const actOnAgencyPackage = (id: number, action: AgencyPackageAction, expectedVersion: number, contentSha256: string) =>
  sendAuthenticatedWrite<AgencyPackageDetail>(`${BASE}/${id}/${action}`, 'POST', { expectedVersion, contentSha256 });

export async function downloadAgencyPackage(id: number): Promise<{ blob: Blob; filename: string }> {
  const response = await fetch(`${BASE}/${id}/content.xlsx`, {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: XLSX_MIME },
  });
  if (!response.ok) throw new ApiHttpError(await parseProblem(response));

  const contentType = (response.headers.get('Content-Type') || '').split(';')[0].trim().toLowerCase();
  const disposition = response.headers.get('Content-Disposition') || '';
  const filename = /filename="?([^";\r\n]+)"?/i.exec(disposition)?.[1]?.trim();
  if (response.status !== 200 || contentType !== XLSX_MIME || filename !== `goi-cu-tru-${id}-v1.xlsx`) {
    throw new ApiHttpError({
      status: response.status,
      code: 'INVALID_PACKAGE_FILE',
      detail: 'Máy chủ không trả về đúng tệp gói dữ liệu Excel.',
      requestId: response.headers.get('X-Request-Id') || undefined,
    });
  }

  const blob = await response.blob();
  if (blob.size === 0) {
    throw new ApiHttpError({ status: response.status, code: 'EMPTY_PACKAGE_FILE', detail: 'Tệp gói dữ liệu trống.' });
  }
  return { blob, filename };
}
