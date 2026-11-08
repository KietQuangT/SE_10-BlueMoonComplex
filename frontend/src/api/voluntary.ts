import { getAuthenticated, sendAuthenticatedWrite } from './client';
import type { Page } from './billing';
export interface Campaign { id: number; name: string; description: string | null; opensOn: string; closesOn: string; status: string; version?: number; postedCount?: number; totalCollected?: string }
export interface Donor { id: number; fullName: string; householdCode: string; apartmentCode: string }
export interface Contribution { id: number; donorNameSnapshot: string; householdCodeSnapshot: string; amount: string; paidOn: string; status: string; version: number; voidReason: string | null }
export interface CampaignInput { name: string; description: string; opensOn: string; closesOn: string }
export interface ContributionInput { residentId: number; amount: string; paidOn: string; lateReason: string }
export const listCampaigns = (page: number) => getAuthenticated<Page<Campaign>>(`/api/v1/voluntary-campaigns?page=${page}&size=10`);
export const saveCampaign = (body: CampaignInput, campaign?: Campaign) => sendAuthenticatedWrite<Campaign>(campaign ? `/api/v1/voluntary-campaigns/${campaign.id}` : '/api/v1/voluntary-campaigns', campaign ? 'PATCH' : 'POST', campaign ? { ...body, version: campaign.version } : body);
export const campaignAction = (campaign: Campaign, action: 'publish' | 'close') => sendAuthenticatedWrite<Campaign>(`/api/v1/voluntary-campaigns/${campaign.id}/${action}`, 'POST', { version: campaign.version });
export const listDonors = (q: string, page = 0) => getAuthenticated<Page<Donor>>(`/api/v1/voluntary-donors?${new URLSearchParams({ q, page: String(page), size: '10' })}`);
export const listContributions = (id: number, page: number) => getAuthenticated<Page<Contribution>>(`/api/v1/voluntary-campaigns/${id}/contributions?page=${page}&size=10`);
export const recordContribution = (id: number, body: ContributionInput, key: string) => sendAuthenticatedWrite<Contribution>(`/api/v1/voluntary-campaigns/${id}/contributions`, 'POST', body, { 'Idempotency-Key': key });
export const voidContribution = (item: Contribution, reason: string) => sendAuthenticatedWrite<Contribution>(`/api/v1/voluntary-contributions/${item.id}/void`, 'POST', { version: item.version, reason });
