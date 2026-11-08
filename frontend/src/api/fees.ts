import { getAuthenticated, sendAuthenticatedWrite } from './client';

export interface FeeType {
  id: number;
  code: string;
  name: string;
  calculationMethod: 'AREA' | 'PER_HOUSEHOLD';
  unit: string;
}

export interface FeeRate {
  id: number;
  feeTypeId: number;
  feeTypeCode: string;
  feeTypeName: string;
  unitPrice: string | number;
  effectiveFrom: string;
  createdAt: string;
  editable: boolean;
}

export interface FeeRatePage {
  content: FeeRate[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export const fetchFeeTypes = () => getAuthenticated<FeeType[]>('/api/v1/fee-types');
export const fetchFeeRates = (feeTypeId: string, page: number, size: number) => {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (feeTypeId) params.set('feeTypeId', feeTypeId);
  return getAuthenticated<FeeRatePage>(`/api/v1/fee-rates?${params}`);
};
export const createFeeRate = (body: { feeTypeId: number; unitPrice: string; effectiveFrom: string }) =>
  sendAuthenticatedWrite<FeeRate>('/api/v1/fee-rates', 'POST', body);
export const updateFeeRate = (id: number, unitPrice: string) =>
  sendAuthenticatedWrite<FeeRate>(`/api/v1/fee-rates/${id}`, 'PATCH', { unitPrice });
export const deleteFeeRate = (id: number) =>
  sendAuthenticatedWrite<void>(`/api/v1/fee-rates/${id}`, 'DELETE');
