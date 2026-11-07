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
  changeType: 'MOVE_OUT' | 'TEMPORARY_ABSENCE';
  effectiveDate: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
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
  changeType: d.changeType === 'MOVE_OUT' ? 'CHUYEN_DI' : 'TAM_VANG',
  effectiveDate: d.effectiveDate,
  endDateAssumed: null,
  reason: '',
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
