import { getAuthenticated } from './client';

export type InvoicePaymentStatus = 'UNPAID' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELLED';
export type InvoiceFilterStatus = 'ALL' | 'OUTSTANDING' | 'UNPAID' | 'PARTIALLY_PAID' | 'PAID' | 'CANCELLED';
export type ReceiptStatus = 'POSTED' | 'VOIDED';
export type ReceiptFilterStatus = 'ALL' | 'POSTED' | 'VOIDED';
export type AllocationStatus = 'ACTIVE' | 'REVERSED';
export type AllocationFilterStatus = 'ALL' | 'ACTIVE' | 'REVERSED';

export interface ResidentInvoiceSummary {
  invoiceId: number;
  period: string;
  householdCode: string;
  apartmentCode: string;
  dueDate: string;
  issuedAt: string;
  cancelledAt: string | null;
  invoiceStatus: 'ISSUED' | 'CANCELLED';
  paymentStatus: InvoicePaymentStatus;
  total: string;
  settled: string;
  remaining: string;
}

export interface ResidentInvoiceLine {
  feeTypeCode: string;
  feeTypeName: string;
  calculationMethod: 'AREA' | 'PER_HOUSEHOLD';
  quantity: string;
  unit: 'M2' | 'HOUSEHOLD';
  areaM2: string | null;
  unitPrice: string;
  amount: string;
}

export interface ResidentInvoiceDetail {
  invoiceId: number;
  period: string;
  householdCode: string;
  apartmentCode: string;
  dueDate: string;
  issuedAt: string;
  cancelledAt: string | null;
  invoiceStatus: 'ISSUED' | 'CANCELLED';
  paymentStatus: InvoicePaymentStatus;
  total: string;
  settled: string;
  remaining: string;
  lines: ResidentInvoiceLine[];
}

export interface ResidentReceipt {
  receiptId: number;
  invoiceId: number;
  period: string;
  paidOn: string;
  recordedAt: string;
  amount: string;
  appliedAmount: string;
  creditAmount: string;
  status: ReceiptStatus;
  voidedAt: string | null;
  hasLateReason: boolean;
  hasVoidReason: boolean;
}

export interface ResidentAllocation {
  allocationId: number;
  invoiceId: number;
  period: string;
  amount: string;
  status: AllocationStatus;
  recordedAt: string;
  reversedAt: string | null;
  hasReverseReason: boolean;
}

export interface ResidentBillingSummary {
  totalOutstanding: string;
  outstandingInvoiceCount: number;
  nearestDueDate: string | null;
  nearestDuePeriod: string | null;
  creditBalance: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface InvoiceFilterParams {
  period?: string;
  paymentStatus?: InvoiceFilterStatus;
  page?: number;
  size?: number;
}

export interface ReceiptFilterParams {
  invoiceId?: number;
  status?: ReceiptFilterStatus;
  page?: number;
  size?: number;
}

export interface AllocationFilterParams {
  invoiceId?: number;
  status?: AllocationFilterStatus;
  page?: number;
  size?: number;
}

/**
 * GET /api/v1/resident/invoices
 * Allowed query keys: period, paymentStatus, page, size.
 */
export async function fetchResidentInvoices(
  params: InvoiceFilterParams = {},
): Promise<PageResponse<ResidentInvoiceSummary>> {
  const page = params.page ?? 0;
  const size = params.size ?? 20;
  const queryParts = [`page=${encodeURIComponent(page)}`, `size=${encodeURIComponent(size)}`];

  if (params.period && params.period.trim()) {
    queryParts.push(`period=${encodeURIComponent(params.period.trim())}`);
  }
  if (params.paymentStatus && params.paymentStatus !== 'ALL') {
    queryParts.push(`paymentStatus=${encodeURIComponent(params.paymentStatus)}`);
  }

  const url = `/api/v1/resident/invoices?${queryParts.join('&')}`;
  return getAuthenticated<PageResponse<ResidentInvoiceSummary>>(url);
}

/**
 * GET /api/v1/resident/invoices/{invoiceId}
 * No query parameters permitted.
 */
export async function fetchResidentInvoiceDetail(
  invoiceId: number,
): Promise<ResidentInvoiceDetail> {
  const url = `/api/v1/resident/invoices/${encodeURIComponent(invoiceId)}`;
  return getAuthenticated<ResidentInvoiceDetail>(url);
}

/**
 * GET /api/v1/resident/receipts
 * Allowed query keys: invoiceId, status, page, size.
 */
export async function fetchResidentReceipts(
  params: ReceiptFilterParams = {},
): Promise<PageResponse<ResidentReceipt>> {
  const page = params.page ?? 0;
  const size = params.size ?? 20;
  const queryParts = [`page=${encodeURIComponent(page)}`, `size=${encodeURIComponent(size)}`];

  if (params.invoiceId != null) {
    queryParts.push(`invoiceId=${encodeURIComponent(params.invoiceId)}`);
  }
  if (params.status && params.status !== 'ALL') {
    queryParts.push(`status=${encodeURIComponent(params.status)}`);
  }

  const url = `/api/v1/resident/receipts?${queryParts.join('&')}`;
  return getAuthenticated<PageResponse<ResidentReceipt>>(url);
}

/**
 * GET /api/v1/resident/credit-allocations
 * Allowed query keys: invoiceId, status, page, size.
 */
export async function fetchResidentCreditAllocations(
  params: AllocationFilterParams = {},
): Promise<PageResponse<ResidentAllocation>> {
  const page = params.page ?? 0;
  const size = params.size ?? 20;
  const queryParts = [`page=${encodeURIComponent(page)}`, `size=${encodeURIComponent(size)}`];

  if (params.invoiceId != null) {
    queryParts.push(`invoiceId=${encodeURIComponent(params.invoiceId)}`);
  }
  if (params.status && params.status !== 'ALL') {
    queryParts.push(`status=${encodeURIComponent(params.status)}`);
  }

  const url = `/api/v1/resident/credit-allocations?${queryParts.join('&')}`;
  return getAuthenticated<PageResponse<ResidentAllocation>>(url);
}

/**
 * GET /api/v1/resident/billing-summary
 * No query parameters permitted.
 */
export async function fetchResidentBillingSummary(): Promise<ResidentBillingSummary> {
  const url = '/api/v1/resident/billing-summary';
  return getAuthenticated<ResidentBillingSummary>(url);
}
