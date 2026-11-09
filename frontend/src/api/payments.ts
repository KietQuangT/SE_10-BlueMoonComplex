import { fetchCurrentUser, getAuthenticated, sendAuthenticatedWrite } from './client';
import type { Page } from './billing';

export interface PaymentInvoice { invoiceId: number; periodId: number; householdId: number; householdCode: string; apartmentCode: string; period: string; dueDate: string; total: string; settled: string; remaining: string; paymentStatus: 'UNPAID' | 'PARTIALLY_PAID' | 'PAID' }
export interface PaymentReceipt { receiptId: number; invoiceId: number; paidOn: string; amount: string; appliedAmount: string; creditAmount: string; debtAfter: string; status: 'POSTED' | 'VOIDED'; recordedAt: string; recordedBy: number; hasLateReason: boolean; voidedBy: number | null; voidedAt: string | null; hasVoidReason: boolean; activeAllocationCount: number; canVoid: boolean }
export interface AllocationSource { receiptId: number; amount: string }
export interface CreditAllocation { allocationId: number; invoiceId: number; householdId: number; amount: string; debtAfter: string; status: 'ACTIVE' | 'REVERSED'; recordedBy: number; recordedAt: string; reversedBy: number | null; reversedAt: string | null; hasReverseReason: boolean; sources: AllocationSource[] }
export interface InvoiceDetail { id: number; issuedAt: string; lines: { feeTypeCode: string; feeTypeName: string; amount: string; quantity: string; unitPrice: string }[] }
export interface Summary { invoiceId: number; total: string; settled: string; remaining: string; status: string }
export interface PendingPayment { kind: 'receipt' | 'allocation' | 'void' | 'reverse'; url: string; body: Record<string, string | number>; label: string }

export function previewReceipt(debt: string, amount: string): { applied: string; credit: string; remaining: string } | null {
  if (!/^\d{1,24}$/.test(amount) || !/^\d{1,24}$/.test(debt)) return null;
  const received = BigInt(amount), due = BigInt(debt);
  if (received <= 0n) return null;
  const applied = received < due ? received : due;
  return { applied: applied.toString(), credit: (received - applied).toString(), remaining: (due - applied).toString() };
}
export function businessToday(): string { return businessDate(new Date().toISOString()); }
export function businessDate(instant: string): string {
  const parts = new Intl.DateTimeFormat('en-GB', { timeZone: 'Asia/Ho_Chi_Minh', year: 'numeric', month: '2-digit', day: '2-digit' }).formatToParts(new Date(instant));
  const values = Object.fromEntries(parts.map((part) => [part.type, part.value]));
  return `${values.year}-${values.month}-${values.day}`;
}
const storageKey = (actor: string) => `bluemoon.s09.pending.${encodeURIComponent(actor)}`;
export function savePending(actor: string, command: PendingPayment): void { sessionStorage.setItem(storageKey(actor), JSON.stringify(command)); }
export function loadPending(actor: string): PendingPayment | null {
  const raw = sessionStorage.getItem(storageKey(actor));
  if (!raw) return null;
  try {
    const value = JSON.parse(raw) as PendingPayment;
    if (!value || !['receipt', 'allocation', 'void', 'reverse'].includes(value.kind) || typeof value.url !== 'string' || !/^\/api\/v1\/(invoices\/\d+\/receipts|households\/\d+\/credit-allocations|receipts\/\d+\/void|credit-allocations\/\d+\/reverse)$/.test(value.url) || !value.body || typeof value.body.idempotencyKey !== 'string' || typeof value.label !== 'string') throw new Error('invalid');
    return value;
  } catch { throw new Error('Không đọc được giao dịch đang chờ. Không tạo giao dịch mới; liên hệ quản trị viên để đối soát.'); }
}
export function clearPending(actor: string): void { sessionStorage.removeItem(storageKey(actor)); }
export class PaymentActorChangedError extends Error {}
export function sendPending(command: PendingPayment, actor: string): Promise<unknown> {
  return sendAuthenticatedWrite(command.url, 'POST', command.body, undefined, async () => {
    const current = await fetchCurrentUser();
    if (current.username !== actor) throw new PaymentActorChangedError('Tài khoản đã thay đổi.');
  });
}
export function listPaymentInvoices(filter: { q: string; period: string; paymentStatus: string; page: number }): Promise<Page<PaymentInvoice>> {
  const params = new URLSearchParams();
  if (filter.q.trim()) params.set('q', filter.q.trim());
  if (filter.period) params.set('period', filter.period);
  if (filter.paymentStatus) params.set('paymentStatus', filter.paymentStatus);
  params.set('page', String(filter.page)); params.set('size', '20');
  return getAuthenticated(`/api/v1/payment-invoices?${params}`);
}
export const invoiceDetail = (id: number) => getAuthenticated<InvoiceDetail>(`/api/v1/invoices/${id}`);
export const invoiceSummary = (id: number) => getAuthenticated<Summary>(`/api/v1/invoices/${id}/payment-summary`);
export const creditBalance = (id: number) => getAuthenticated<{ householdId: number; balance: string }>(`/api/v1/households/${id}/credit-balance`);
export const receiptHistory = (id: number, page: number) => getAuthenticated<Page<PaymentReceipt>>(`/api/v1/invoices/${id}/receipts?page=${page}&size=20`);
export function allocationHistory(householdId: number, filter: { invoiceId?: number; receiptId?: number; page: number }) {
  const params = new URLSearchParams();
  if (filter.invoiceId !== undefined) params.set('invoiceId', String(filter.invoiceId));
  if (filter.receiptId !== undefined) params.set('receiptId', String(filter.receiptId));
  params.set('page', String(filter.page)); params.set('size', '20');
  return getAuthenticated<Page<CreditAllocation>>(`/api/v1/households/${householdId}/credit-allocations?${params}`);
}
