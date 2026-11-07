import { getAuthenticated, sendAuthenticatedWrite } from './client';
import type { Household, ResidencyChangeRecord, ResidentMember } from '../types/domain';

/** S05 wire shapes (backend residency.api records). */
export interface HouseholdDto {
  id: number;
  householdCode: string;
  apartmentCode: string;
  areaM2: number | string;
  address: string | null;
  status: 'ACTIVE' | 'INACTIVE';
}
export interface ResidentDto {
  id: number;
  householdId: number;
  fullName: string;
  identityNumber: string | null;
  dateOfBirth: string | null;
  phone: string | null;
  relationshipToHead: string | null;
  residencyStatus: 'RESIDING' | 'TEMPORARILY_ABSENT' | 'MOVED_OUT';
}
export interface MyResidentDto {
  id: number;
  fullName: string;
  dateOfBirth: string | null;
  relationshipToHead: string | null;
  residencyStatus: ResidentDto['residencyStatus'];
}
export interface ResidencyChangeDto {
  id: number;
  residentId: number;
  changeType: 'MOVE_OUT' | 'TEMPORARY_ABSENCE' | 'RETURN';
  effectiveDate: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  reason?: string | null;
  endDate?: string | null;
  decisionNote?: string | null;
  decidedAt?: string | null;
  recordedBy: number;
}
interface Page<T> {
  content: T[];
  totalPages: number;
}

const STATUS: Record<ResidentDto['residencyStatus'], ResidentMember['residencyStatus']> = {
  RESIDING: 'Đang cư trú',
  TEMPORARILY_ABSENT: 'Tạm vắng',
  MOVED_OUT: 'Đã chuyển đi',
};

/** Keep only the last 4 characters (PRODUCT.md: identity numbers are masked on screen). */
export function maskTail(value: string | null | undefined): string | null {
  if (!value) return null;
  return value.length <= 4 ? value : `${'*'.repeat(value.length - 4)}${value.slice(-4)}`;
}

export const toHousehold = (d: HouseholdDto): Household => ({
  id: d.id,
  householdCode: d.householdCode,
  apartmentCode: d.apartmentCode,
  areaM2: String(d.areaM2),
  address: d.address ?? '',
  status: d.status,
  currentResidents: null, // the household list has no count; the detail page loads members
});

export const toResident = (d: ResidentDto): ResidentMember => ({
  id: d.id,
  householdId: d.householdId,
  fullName: d.fullName,
  identityNumberMasked: maskTail(d.identityNumber),
  phoneMasked: maskTail(d.phone),
  dateOfBirth: d.dateOfBirth,
  relationshipToHead: d.relationshipToHead ?? '',
  residencyStatus: STATUS[d.residencyStatus],
});

export const toMyResident = (d: MyResidentDto, householdId: number): ResidentMember => ({
  id: d.id,
  householdId,
  fullName: d.fullName,
  identityNumberMasked: null, // residents never receive identity numbers (D20)
  phoneMasked: null,
  dateOfBirth: d.dateOfBirth,
  relationshipToHead: d.relationshipToHead ?? '',
  residencyStatus: STATUS[d.residencyStatus],
});

export const toChange = (d: ResidencyChangeDto, householdId: number): ResidencyChangeRecord => ({
  id: d.id,
  residentId: d.residentId,
  householdId,
  changeType: d.changeType === 'MOVE_OUT' ? 'CHUYEN_DI' : d.changeType === 'RETURN' ? 'QUAY_LAI' : 'TAM_VANG',
  effectiveDate: d.effectiveDate,
  endDateAssumed: d.endDate ?? null,
  reason: d.reason ?? '',
  rejectReasonAssumed: d.decisionNote ?? null,
  approvedAtAssumed: d.decidedAt ?? null,
  status: d.status,
  recordedBy: d.recordedBy,
});

/** Read every page of a paged list (size 100); stops at the server's totalPages. */
async function allPages<T>(url: string): Promise<T[]> {
  const out: T[] = [];
  for (let page = 0; ; page += 1) {
    const sep = url.includes('?') ? '&' : '?';
    const res = await getAuthenticated<Page<T>>(`${url}${sep}page=${page}&size=100`);
    out.push(...res.content);
    if (page + 1 >= res.totalPages) return out;
  }
}

export const fetchHouseholds = async () =>
  (await allPages<HouseholdDto>('/api/v1/households')).map(toHousehold);

export const fetchResidents = async (householdId: number) =>
  (await allPages<ResidentDto>(`/api/v1/households/${householdId}/residents`)).map(toResident);

export const fetchResidencyChanges = async (householdId: number) =>
  (await allPages<ResidencyChangeDto>(`/api/v1/households/${householdId}/residency-changes`)).map((c) =>
    toChange(c, householdId),
  );

export const fetchMyResidents = async (householdId: number) =>
  (await getAuthenticated<MyResidentDto[]>('/api/v1/households/me/residents')).map((r) =>
    toMyResident(r, householdId),
  );

export const createHouseholdApi = (body: {
  householdCode: string;
  apartmentCode: string;
  areaM2: string;
  address: string;
}) => sendAuthenticatedWrite<HouseholdDto>('/api/v1/households', 'POST', { ...body, areaM2: Number(body.areaM2) });

export const updateHouseholdApi = (id: number, body: { areaM2: string; address: string }) =>
  sendAuthenticatedWrite<HouseholdDto>(`/api/v1/households/${id}`, 'PATCH', {
    areaM2: Number(body.areaM2),
    address: body.address,
  });

export const createResidentApi = (
  householdId: number,
  body: {
    fullName: string;
    identityNumber: string;
    dateOfBirth: string;
    phone: string;
    relationshipToHead: string;
  },
) =>
  sendAuthenticatedWrite<ResidentDto>(`/api/v1/households/${householdId}/residents`, 'POST', {
    fullName: body.fullName,
    identityNumber: body.identityNumber || null,
    dateOfBirth: body.dateOfBirth || null,
    phone: body.phone || null,
    relationshipToHead: body.relationshipToHead || null,
  });

export const moveOutApi = (residentId: number, effectiveDate: string, reason: string) =>
  sendAuthenticatedWrite<ResidentDto>(`/api/v1/residents/${residentId}/move-out`, 'POST', {
    effectiveDate,
    reason: reason || null,
  });

export type DeclarationStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export interface DeclarationDto {
  id: number; residentId: number; residentName: string; householdId: number;
  householdCode: string; apartmentCode: string; changeType: 'TEMPORARY_ABSENCE';
  status: DeclarationStatus; effectiveDate: string; endDate: string | null;
  reason: string; decisionNote: string | null; decidedAt: string | null;
  createdAt: string; cancellable: boolean;
}
export interface DeclarationPage { content: DeclarationDto[]; page: number; size: number; totalElements: number; totalPages: number }
export interface ResidentSuggestion { residentId: number; fullName: string; householdId: number; householdCode: string; apartmentCode: string }
export interface DeclarationFilters { status?: DeclarationStatus; apartment?: string; q?: string; page?: number; size?: number }
function query(filters: DeclarationFilters): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(filters)) {
    if (value !== undefined && String(value).trim() !== '') params.set(key, String(value).trim());
  }
  const result = params.toString();
  return result ? `?${result}` : '';
}
export const createDeclaration = (body: { residentId: number; effectiveDate: string; endDate?: string; reason: string }) =>
  sendAuthenticatedWrite<DeclarationDto>('/api/v1/residency-declarations', 'POST', body);
export const fetchMyDeclarations = (page = 0, size = 10) =>
  getAuthenticated<DeclarationPage>(`/api/v1/residency-declarations/mine${query({ page, size })}`);
export const cancelDeclaration = (id: number) => sendAuthenticatedWrite<DeclarationDto>(`/api/v1/residency-declarations/${id}/cancel`, 'POST');
export const fetchDeclarations = (filters: DeclarationFilters = {}) => getAuthenticated<DeclarationPage>(`/api/v1/residency-declarations${query(filters)}`);
export const approveDeclaration = (id: number) => sendAuthenticatedWrite<DeclarationDto>(`/api/v1/residency-declarations/${id}/approve`, 'POST');
export const rejectDeclaration = (id: number, decisionNote: string) => sendAuthenticatedWrite<DeclarationDto>(`/api/v1/residency-declarations/${id}/reject`, 'POST', { decisionNote });
export const returnResident = (id: number) => sendAuthenticatedWrite<ResidentDto>(`/api/v1/residents/${id}/return`, 'POST');
export const suggestResidents = (q: string) => getAuthenticated<ResidentSuggestion[]>(`/api/v1/residents/suggest${query({ q })}`);
