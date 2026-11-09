import { getAuthenticated, sendAuthenticatedWrite } from './client';
export interface Page<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
export interface Period { id: number; period: string; dueDate: string; status: string; version: number; latestRevision: number; expired: boolean; editable: boolean; allowedActions: string[]; householdCount: number | null; total: string | null; returnReason: string | null; cancelReason: string | null }
export interface Line { feeTypeCode: string; feeTypeName: string; quantity: string; unitPrice: string; amount: string }
export interface Household { householdId: number; householdCode: string; apartmentCode: string; areaM2: string; lines: Line[]; total: string }
export interface Submission { revision: number | null; householdCount: number; total: string; version: number; households: Page<Household> }
export const listBillingPeriods = (page: number) => getAuthenticated<Page<Period>>(`/api/v1/billing-periods?page=${page}&size=10`);
export const createPeriod = (period: string, dueDate: string) => sendAuthenticatedWrite<Period>('/api/v1/billing-periods', 'POST', { period, dueDate });
export const updatePeriod = (id: number, dueDate: string, version: number) => sendAuthenticatedWrite<Period>(`/api/v1/billing-periods/${id}`, 'PATCH', { dueDate, version });
export const periodAction = (id: number, action: string, body: object) => sendAuthenticatedWrite<Period>(`/api/v1/billing-periods/${id}/${action}`, 'POST', body);
export const periodPreview = (id: number, page: number) => getAuthenticated<Submission>(`/api/v1/billing-periods/${id}/preview?page=${page}&size=10`);
export interface Invoice { id: number; householdCode: string; apartmentCode: string; dueDate: string; status: string; lines: Line[]; total: string }
export const periodInvoices = (id: number, page: number) => getAuthenticated<Page<Invoice>>(`/api/v1/billing-periods/${id}/invoices?page=${page}&size=10`);
export const periodSubmission = (id: number, revision: number, page: number) => getAuthenticated<Submission>(`/api/v1/billing-periods/${id}/submissions?revision=${revision}&page=${page}&size=10`);
