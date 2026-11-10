export type RoleCode = 'ADMIN' | 'ACCOUNTANT' | 'RESIDENT' | 'AUTHORITY';

export type ContractBadgeKind = 'api-live' | 'db-ready' | 'assumed' | 'pending-rule';

export type ForcedUiState =
  | 'normal'
  | 'loading'
  | 'empty'
  | 'error'
  | 'forbidden-403'
  | 'unauthorized-401'
  | 'submitting'
  | 'success';

export interface SessionUser {
  username: string;
  fullName: string;
  role: RoleCode;
  householdId: number | null;
  mustChangePassword: boolean;
}

export interface CsrfResponse {
  headerName: string;
  token: string;
}

export interface CreateAccountPayload {
  username: string;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'ACCOUNTANT' | 'RESIDENT';
  householdId: number | null;
}

export interface CreatedAccountDto {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'ACCOUNTANT' | 'RESIDENT';
  householdId: number | null;
  emailStatus: string;
}

export interface ProblemDetailError {
  status: number;
  title?: string;
  detail?: string;
  code?: string;
  requestId?: string;
}

export interface Household {
  id: number;
  householdCode: string;
  apartmentCode: string;
  areaM2: string;
  address: string;
  status: 'ACTIVE' | 'INACTIVE';
  currentResidents: number | null;
}

export interface ResidentMember {
  id: number;
  householdId: number;
  fullName: string;
  identityNumberMasked: string | null;
  phoneMasked?: string | null;
  dateOfBirth: string | null;
  relationshipToHead: string;
  residencyStatus: 'Đang cư trú' | 'Tạm vắng' | 'Đã chuyển đi';
}

export interface AccountRecord {
  id: number;
  fullName: string;
  username: string;
  email: string;
  role: RoleCode;
  householdCode: string | null;
  householdId: number | null;
  status: 'ACTIVE' | 'INACTIVE';
  locked: boolean;
  lockedForMinutes?: number;
}

export interface ResidencyChangeRecord {
  id: number;
  residentId: number;
  householdId: number;
  changeType: 'TAM_VANG' | 'CHUYEN_DI' | 'QUAY_LAI';
  effectiveDate: string;
  endDateAssumed: string | null;
  reason: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
  recordedBy: number;
  approvedByAssumed?: number | null;
  approvedAtAssumed?: string | null;
  rejectReasonAssumed?: string | null;
}

export interface InvoiceLine {
  feeCode: string;
  feeName: string;
  quantity: string;
  unit: 'm2' | 'hộ';
  unitPrice: string;
  amount: string;
}

export interface InvoiceRecord {
  id: number;
  householdId: number;
  apartmentCode: string;
  period: string;
  issuedAt: string;
  dueDate: string;
  status: 'CHUA_DONG' | 'DA_DONG';
  lines: InvoiceLine[];
  total: string;
  paid: string;
  remaining: string;
}

export interface PaymentTransaction {
  id: number;
  invoiceId: number;
  apartmentCode: string;
  period: string;
  amount: string;
  paidAt: string;
  receivedBy: string;
  note?: string;
}

export interface VoluntaryCampaign {
  id: number;
  name: string;
  description: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CLOSED';
  opensOn: string;
  closesOn: string;
  note: string;
  contributionsCount: number;
  totalCollectedDisplay: string;
}

export interface ReportPackage {
  id: number;
  type: string;
  typeLabel: string;
  period: string;
  status: 'APPROVED' | 'REVOKED';
  approvedBy: string;
  approvedAt: string;
  allowedFields: string[];
}

export interface CollectionRateSummary {
  period: string;
  totalCollected: string;
  totalOutstanding: string;
  rate: string;
  formulaNote: string;
}

export interface FieldChangeProposal {
  id: number;
  screenCode: string;
  fieldOrScreen: string;
  reason: string;
  required: boolean;
  ownerQuestion: string;
}
