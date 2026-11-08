import React, { createContext, useContext, useEffect, useMemo, useRef, useState } from 'react';
import {
  ApiHttpError,
  clearCsrfMemory,
  createAccountApi,
  fetchCurrentUser,
  loginWithCredentials,
  logoutCurrentSession,
  manageAccountApi,
} from '../api/client';
import { messageForError } from '../api/errors';
import {
  createHouseholdApi,
  createDeclaration,
  approveDeclaration,
  rejectDeclaration,
  createResidentApi,
  fetchHouseholds,
  fetchDeclarations,
  fetchMyDeclarations,
  fetchMyResidents,
  fetchResidencyChanges,
  fetchResidents,
  moveOutApi,
  toHousehold,
  toResident,
  updateHouseholdApi,
} from '../api/residency';
import {
  DEMO_SESSIONS,
  INITIAL_ACCOUNTS,
  INITIAL_COLLECTION_RATE,
  INITIAL_HOUSEHOLDS,
  INITIAL_INVOICES,
  INITIAL_PAYMENTS,
  INITIAL_REPORT_PACKAGES,
  INITIAL_RESIDENCY_CHANGES,
  INITIAL_RESIDENTS,
  INITIAL_VOLUNTARY_CAMPAIGNS,
} from '../data/mockData';
import { maskIdentityNumber } from '../lib/format';
import type {
  AccountRecord,
  CollectionRateSummary,
  CreateAccountPayload,
  ForcedUiState,
  Household,
  InvoiceRecord,
  PaymentTransaction,
  ReportPackage,
  ResidencyChangeRecord,
  ResidentMember,
  SessionUser,
  VoluntaryCampaign,
} from '../types/domain';

export type DataSourceMode = 'api' | 'demo';

export interface AbsenceFormDraft {
  residentId: string;
  effectiveDate: string;
  endDateAssumed: string;
  reason: string;
}

interface AppStateValue {
  mode: DataSourceMode;
  setMode: (next: DataSourceMode) => void;
  backendReachable: boolean | null;
  authLoading: boolean;
  user: SessionUser | null;
  returnToPath: string | null;
  sessionExpiredBanner: boolean;
  clearSessionExpiredBanner: () => void;
  forcedUiState: ForcedUiState;
  setForcedUiState: (next: ForcedUiState) => void;
  qaDrawerOpen: boolean;
  setQaDrawerOpen: (open: boolean) => void;

  // Draft preserved across 401 session expiration (M-04 rule)
  absenceDraft: AbsenceFormDraft;
  setAbsenceDraft: React.Dispatch<React.SetStateAction<AbsenceFormDraft>>;

  // Auth Actions
  login: (username: string, password: string) => Promise<SessionUser>;
  loginDemoAs: (presetKey: keyof typeof DEMO_SESSIONS) => SessionUser;
  logout: () => Promise<void>;
  triggerSessionExpired: (currentPath: string) => void;
  completePasswordChangeDemo: () => Promise<void>;

  // Domain State (seeded from mock-data.json, interactive in Demo & hybrid S02)
  households: Household[];
  residents: ResidentMember[];
  accounts: AccountRecord[];
  residencyChanges: ResidencyChangeRecord[];
  invoices: InvoiceRecord[];
  payments: PaymentTransaction[];
  voluntaryCampaigns: VoluntaryCampaign[];
  reportPackages: ReportPackage[];
  collectionRate: CollectionRateSummary;

  // Real-API data loading (api mode only)
  dataLoadError: boolean;
  absenceCount: number;
  refreshAbsenceCount: () => Promise<void>;
  handleCallFailure: (err: unknown) => string;
  reloadHouseholds: () => Promise<void>;
  loadHouseholdDetail: (householdId: number) => Promise<void>;

  // Domain Actions
  saveHousehold: (
    input: Omit<Household, 'id' | 'currentResidents'> & { id?: number },
  ) => Promise<{ ok: boolean; error?: string; id?: number }>;
  addResident: (input: Parameters<AppStateValue['addResidentDemo']>[0]) => Promise<{ ok: boolean; error?: string }>;
  recordMoveOut: (
    residentId: number,
    effectiveDate: string,
    reason: string,
  ) => Promise<{ ok: boolean; error?: string }>;
  /** S04 (api mode: real endpoint, ADMIN only; demo mode: local state). */
  manageAccount: (
    accountId: number,
    action: 'disable' | 'enable' | 'unlock',
  ) => Promise<{ ok: boolean; error?: string }>;
  provisionAccount: (payload: CreateAccountPayload) => Promise<{ emailStatus: string }>;
  disableAccountDemo: (accountId: number) => void;
  unlockAccountDemo: (accountId: number) => void;
  saveHouseholdDemo: (input: Omit<Household, 'id' | 'currentResidents'> & { id?: number }) => {
    ok: boolean;
    error?: string;
  };
  addResidentDemo: (input: {
    householdId: number;
    fullName: string;
    identityNumberRaw: string;
    dateOfBirth: string;
    phone: string;
    relationshipToHead: string;
    residencyStatus: ResidentMember['residencyStatus'];
  }) => { ok: boolean; error?: string };
  recordMoveOutDemo: (residentId: number, effectiveDate: string, reason: string) => void;
  submitAbsenceDemo: (input: {
    residentId: number;
    effectiveDate: string;
    endDateAssumed: string;
    reason: string;
  }) => Promise<void>;
  decideAbsenceDemo: (
    changeId: number,
    decision: 'APPROVED' | 'REJECTED',
    rejectReason?: string,
  ) => Promise<void>;
  collectInvoiceCashDemo: (invoiceId: number, note?: string) => PaymentTransaction | null;
  publishVoluntaryCampaignDemo: (name: string, description: string, opensOn: string, closesOn: string) => void;
  approveReportPackageDemo: (period: string, allowedFields: string[]) => void;
}

const AppStateContext = createContext<AppStateValue | null>(null);

export function AppStateProvider({ children }: { children: React.ReactNode }) {
  const [mode, setModeState] = useState<DataSourceMode>('api');
  const [backendReachable, setBackendReachable] = useState<boolean | null>(null);
  const [authLoading, setAuthLoading] = useState<boolean>(true);
  const [user, setUser] = useState<SessionUser | null>(null);
  const [returnToPath, setReturnToPath] = useState<string | null>(null);
  const [sessionExpiredBanner, setSessionExpiredBanner] = useState<boolean>(false);
  const [forcedUiState, setForcedUiState] = useState<ForcedUiState>('normal');
  const [qaDrawerOpen, setQaDrawerOpen] = useState<boolean>(false);
  const [absenceCount, setAbsenceCount] = useState(0);
  const [dataLoadError, setDataLoadError] = useState<boolean>(false);

  const emptyDraft = (): AbsenceFormDraft => ({ residentId: '', effectiveDate: '', endDateAssumed: '', reason: '' });
  const demoDraft = (): AbsenceFormDraft => ({ residentId: '1', effectiveDate: '2026-11-12', endDateAssumed: '2026-11-18', reason: '' });
  const [absenceDraft, setAbsenceDraft] = useState<AbsenceFormDraft>(emptyDraft);
  const draftOwner = useRef<string | null>(null);
  const draftMode = useRef<DataSourceMode>('api');
  const selectDraftOwner = (nextMode: DataSourceMode, username?: string) => {
    if (draftMode.current !== nextMode || (username != null && draftOwner.current !== username)) {
      setAbsenceDraft(nextMode === 'api' ? emptyDraft() : demoDraft());
    }
    draftMode.current = nextMode;
    if (username != null) draftOwner.current = username;
  };

  const [households, setHouseholds] = useState<Household[]>(INITIAL_HOUSEHOLDS);
  const [residents, setResidents] = useState<ResidentMember[]>([]);
  const [accounts, setAccounts] = useState<AccountRecord[]>(INITIAL_ACCOUNTS);
  const [residencyChanges, setResidencyChanges] =
    useState<ResidencyChangeRecord[]>([]);
  const [invoices, setInvoices] = useState<InvoiceRecord[]>(INITIAL_INVOICES);
  const [payments, setPayments] = useState<PaymentTransaction[]>(INITIAL_PAYMENTS);
  const [voluntaryCampaigns, setVoluntaryCampaigns] =
    useState<VoluntaryCampaign[]>(INITIAL_VOLUNTARY_CAMPAIGNS);
  const [reportPackages, setReportPackages] = useState<ReportPackage[]>(INITIAL_REPORT_PACKAGES);
  const [collectionRate] = useState<CollectionRateSummary>(INITIAL_COLLECTION_RATE);

  useEffect(() => {
    let cancelled = false;
    async function bootstrapSession() {
      setAuthLoading(true);
      try {
        const me = await fetchCurrentUser();
        if (!cancelled) {
          setBackendReachable(true);
          selectDraftOwner('api', me.username);
          setModeState('api');
          setUser(me);
        }
      } catch (err) {
        if (cancelled) return;
        if (err instanceof ApiHttpError) {
          // Server answered with 401/403 -> backend IS running!
          setBackendReachable(true);
          setUser(null);
        } else {
          // Network/proxy refused -> backend container not started yet
          setBackendReachable(false);
          setUser(null);
        }
      } finally {
        if (!cancelled) {
          setAuthLoading(false);
        }
      }
    }
    bootstrapSession();
    return () => {
      cancelled = true;
    };
  }, []);

  const resetDemoData = () => {
    setHouseholds(INITIAL_HOUSEHOLDS);
    setResidents(INITIAL_RESIDENTS);
    setAccounts(INITIAL_ACCOUNTS);
    setResidencyChanges(INITIAL_RESIDENCY_CHANGES);
  };

  const setMode = (next: DataSourceMode) => {
    if (next === 'demo' && mode === 'api') resetDemoData();
    selectDraftOwner(next);
    setModeState(next);
    setForcedUiState('normal');
  };

  const login = async (username: string, password: string): Promise<SessionUser> => {
    if (mode === 'demo') {
      const normalized = username.trim().toLowerCase();
      const matched = Object.values(DEMO_SESSIONS).find(
        (s) => s.username.toLowerCase() === normalized,
      );
      if (!matched || password.trim().length < 4) {
        throw new ApiHttpError({
          status: 401,
          detail: 'Tên đăng nhập hoặc mật khẩu không đúng.',
        });
      }
      selectDraftOwner(mode, matched.username);
      setUser(matched);
      setSessionExpiredBanner(false);
      return matched;
    }

    const loggedIn = await loginWithCredentials(username, password);
    const verifiedMe = await fetchCurrentUser().catch(() => loggedIn);
    setBackendReachable(true);
    selectDraftOwner(mode, verifiedMe.username);
    setUser(verifiedMe);
    setSessionExpiredBanner(false);
    return verifiedMe;
  };

  const loginDemoAs = (presetKey: keyof typeof DEMO_SESSIONS): SessionUser => {
    const chosen = DEMO_SESSIONS[presetKey];
    if (mode === 'api') resetDemoData();
    selectDraftOwner('demo', chosen.username);
    setModeState('demo');
    setUser(chosen);
    setSessionExpiredBanner(false);
    setForcedUiState('normal');
    return chosen;
  };

  const logout = async (): Promise<void> => {
    if (mode === 'api') {
      await logoutCurrentSession().catch(() => undefined);
    } else {
      clearCsrfMemory();
    }
    setUser(null);
    setReturnToPath(null);
    setForcedUiState('normal');
  };

  const triggerSessionExpired = (currentPath: string) => {
    setUser(null);
    setReturnToPath(currentPath);
    setSessionExpiredBanner(true);
    setForcedUiState('normal');
  };

  const completePasswordChangeDemo = async () => {
    if (mode === 'api') {
      await logoutCurrentSession().catch(() => undefined);
    }
    setUser(null);
  };

  const provisionAccount = async (
    payload: CreateAccountPayload,
  ): Promise<{ emailStatus: string }> => {
    if (mode === 'api') {
      try {
        const created = await createAccountApi(payload);
        const hh = households.find((h) => h.id === created.householdId);
        setAccounts((prev) => [
          ...prev,
          {
            id: created.id,
            fullName: created.fullName,
            username: created.username,
            email: created.email,
            role: created.role,
            householdId: created.householdId,
            householdCode: hh ? hh.householdCode : null,
            status: 'ACTIVE',
            locked: false,
          },
        ]);
        return { emailStatus: created.emailStatus };
      } catch (err) {
        if (err instanceof ApiHttpError && err.problem.status === 401) {
          triggerSessionExpired('/admin/accounts/new');
        }
        throw err;
      }
    }

    // Demo mode validation mirroring backend S02 CreateAccountRequest & ConflictException
    const normalizedEmail = payload.email.trim().toLowerCase();
    const normalizedUsername = payload.username.trim().toLowerCase();
    if (accounts.some((a) => a.email.toLowerCase() === normalizedEmail)) {
      throw new ApiHttpError({ status: 409, code: 'EMAIL_ALREADY_USED' });
    }
    if (accounts.some((a) => a.username.toLowerCase() === normalizedUsername)) {
      throw new ApiHttpError({ status: 409, code: 'USERNAME_ALREADY_USED' });
    }
    if (payload.role === 'RESIDENT') {
      const hh = households.find((h) => h.id === payload.householdId && h.status === 'ACTIVE');
      if (!hh) {
        throw new ApiHttpError({ status: 400, code: 'HOUSEHOLD_INVALID' });
      }
    }

    const hh = households.find((h) => h.id === payload.householdId);
    setAccounts((prev) => [
      ...prev,
      {
        id: prev.length + 10,
        fullName: payload.fullName.trim(),
        username: payload.username.trim(),
        email: normalizedEmail,
        role: payload.role,
        householdId: payload.role === 'RESIDENT' ? payload.householdId : null,
        householdCode: payload.role === 'RESIDENT' && hh ? hh.householdCode : null,
        status: 'ACTIVE',
        locked: false,
      },
    ]);
    return { emailStatus: 'SIMULATED' };
  };

  const disableAccountDemo = (accountId: number) => {
    setAccounts((prev) =>
      prev.map((a) => (a.id === accountId ? { ...a, status: 'INACTIVE', locked: false } : a)),
    );
  };

  const unlockAccountDemo = (accountId: number) => {
    setAccounts((prev) =>
      prev.map((a) =>
        a.id === accountId ? { ...a, locked: false, lockedForMinutes: undefined } : a,
      ),
    );
  };

  /** Session expired mid-call: route to login and keep the page to return to. */
  const handleCallFailure = (err: unknown): string => {
    if (err instanceof ApiHttpError && err.problem.status === 401) {
      triggerSessionExpired(window.location.pathname);
    }
    return messageForError(err);
  };

  const refreshAbsenceCount = async () => {
    if (mode !== 'api' || !user) return;
    try {
      const page = user.role === 'ADMIN' ? await fetchDeclarations({ status: 'PENDING', page: 0, size: 1 }) : user.role === 'RESIDENT' ? await fetchMyDeclarations(0, 100) : null;
      if (user.role === 'ADMIN') setAbsenceCount(page?.totalElements ?? 0);
      else if (page) {
        let count = page.content.filter((d) => d.status === 'PENDING').length;
        for (let i = 1; i < page.totalPages; i++) count += (await fetchMyDeclarations(i, 100)).content.filter((d) => d.status === 'PENDING').length;
        setAbsenceCount(count);
      }
    } catch (err) { handleCallFailure(err); }
  };

  const reloadHouseholds = async () => {
    if (mode !== 'api' || user?.role !== 'ADMIN') return;
    try {
      setHouseholds(await fetchHouseholds());
      setDataLoadError(false);
    } catch (err) {
      handleCallFailure(err);
      setDataLoadError(true);
    }
  };

  const loadHouseholdDetail = async (householdId: number) => {
    if (mode !== 'api') return;
    try {
      const [members, changes] = await Promise.all([
        fetchResidents(householdId),
        fetchResidencyChanges(householdId),
      ]);
      setResidents((prev) => [...prev.filter((r) => r.householdId !== householdId), ...members]);
      setResidencyChanges((prev) => [...prev.filter((c) => c.householdId !== householdId), ...changes]);
      setHouseholds((prev) =>
        prev.map((h) =>
          h.id === householdId
            ? { ...h, currentResidents: members.filter((m) => m.residencyStatus !== 'Đã chuyển đi').length }
            : h,
        ),
      );
      setDataLoadError(false);
    } catch (err) {
      handleCallFailure(err);
      setDataLoadError(true);
    }
  };

  // api mode: replace the seeded sample data with what the server returns for this user.
  useEffect(() => {
    if (mode !== 'api' || !user) return;
    let cancelled = false;
    setResidents([]);
    setResidencyChanges([]);
    void refreshAbsenceCount();
    (async () => {
      try {
        if (user.role === 'ADMIN') {
          const list = await fetchHouseholds();
          if (cancelled) return;
          setHouseholds(list);
          setResidents([]);
          setResidencyChanges([]);
          setAccounts([]); // no list endpoint for accounts yet: only accounts created in this session appear
        } else if (user.role === 'RESIDENT' && user.householdId != null) {
          const mine = await fetchMyResidents(user.householdId);
          if (cancelled) return;
          setHouseholds([]);
          setResidents(mine);
        }
        setDataLoadError(false);
      } catch (err) {
        if (cancelled) return;
        handleCallFailure(err);
        setDataLoadError(true);
      }
    })();
    return () => {
      cancelled = true;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, user?.username]);

  const saveHousehold: AppStateValue['saveHousehold'] = async (input) => {
    if (mode !== 'api') return saveHouseholdDemo(input);
    try {
      if (input.id) {
        const saved = await updateHouseholdApi(input.id, { areaM2: input.areaM2, address: input.address });
        setHouseholds((prev) =>
          prev.map((h) => (h.id === saved.id ? { ...toHousehold(saved), currentResidents: h.currentResidents } : h)),
        );
        return { ok: true, id: saved.id };
      }
      const created = await createHouseholdApi({
        householdCode: input.householdCode.trim(),
        apartmentCode: input.apartmentCode.trim(),
        areaM2: input.areaM2,
        address: input.address,
      });
      setHouseholds((prev) => [...prev, toHousehold(created)]);
      return { ok: true, id: created.id };
    } catch (err) {
      return { ok: false, error: handleCallFailure(err) };
    }
  };

  const addResident: AppStateValue['addResident'] = async (input) => {
    if (mode !== 'api') return addResidentDemo(input);
    try {
      const created = await createResidentApi(input.householdId, {
        fullName: input.fullName.trim(),
        identityNumber: input.identityNumberRaw.trim(),
        dateOfBirth: input.dateOfBirth,
        phone: input.phone.trim(),
        relationshipToHead: input.relationshipToHead.trim(),
      });
      setResidents((prev) => [...prev, toResident(created)]);
      setHouseholds((prev) =>
        prev.map((h) => (h.id === input.householdId ? { ...h, currentResidents: (h.currentResidents ?? 0) + 1 } : h)),
      );
      return { ok: true };
    } catch (err) {
      return { ok: false, error: handleCallFailure(err) };
    }
  };

  const recordMoveOut: AppStateValue['recordMoveOut'] = async (residentId, effectiveDate, reason) => {
    if (mode !== 'api') {
      recordMoveOutDemo(residentId, effectiveDate, reason);
      return { ok: true };
    }
    try {
      const updated = await moveOutApi(residentId, effectiveDate, reason);
      await loadHouseholdDetail(updated.householdId);
      return { ok: true };
    } catch (err) {
      return { ok: false, error: handleCallFailure(err) };
    }
  };

  const manageAccount: AppStateValue['manageAccount'] = async (accountId, action) => {
    if (mode !== 'api') {
      if (action === 'unlock') unlockAccountDemo(accountId);
      else if (action === 'disable') disableAccountDemo(accountId);
      else setAccounts((prev) => prev.map((a) => (a.id === accountId ? { ...a, status: 'ACTIVE' } : a)));
      return { ok: true };
    }
    try {
      await manageAccountApi(accountId, action);
      setAccounts((prev) =>
        prev.map((a) => {
          if (a.id !== accountId) return a;
          if (action === 'disable') return { ...a, status: 'INACTIVE' };
          if (action === 'enable') return { ...a, status: 'ACTIVE' };
          return { ...a, locked: false, lockedForMinutes: undefined };
        }),
      );
      return { ok: true };
    } catch (err) {
      return { ok: false, error: handleCallFailure(err) };
    }
  };

  const saveHouseholdDemo: AppStateValue['saveHouseholdDemo'] = (input) => {
    const codeNorm = input.householdCode.trim().toUpperCase();
    const aptNorm = input.apartmentCode.trim().toUpperCase();
    const conflictCode = households.some(
      (h) => h.id !== input.id && h.householdCode.toUpperCase() === codeNorm,
    );
    if (conflictCode) {
      return { ok: false, error: 'Mã hộ đã tồn tại trên hệ thống. Vui lòng kiểm tra lại.' };
    }
    const conflictApt = households.some(
      (h) => h.id !== input.id && h.apartmentCode.toUpperCase() === aptNorm,
    );
    if (conflictApt) {
      return { ok: false, error: 'Mã căn hộ đã được gán cho hộ khác. Vui lòng chọn căn hộ khác.' };
    }
    if (input.id) {
      setHouseholds((prev) =>
        prev.map((h) =>
          h.id === input.id
            ? {
                ...h,
                householdCode: codeNorm,
                apartmentCode: aptNorm,
                areaM2: input.areaM2,
                address: input.address,
                status: input.status,
              }
            : h,
        ),
      );
    } else {
      setHouseholds((prev) => [
        ...prev,
        {
          id: prev.length + 10,
          householdCode: codeNorm,
          apartmentCode: aptNorm,
          areaM2: input.areaM2,
          address: input.address,
          status: input.status,
          currentResidents: 0,
        },
      ]);
    }
    return { ok: true };
  };

  const addResidentDemo: AppStateValue['addResidentDemo'] = (input) => {
    const masked = maskIdentityNumber(input.identityNumberRaw);
    if (
      masked &&
      residents.some(
        (r) => r.residencyStatus !== 'Đã chuyển đi' && r.identityNumberMasked === masked,
      )
    ) {
      // AGENTS.md: Lỗi trùng không được chứa số giấy tờ!
      return {
        ok: false,
        error:
          'Số giấy tờ định danh bị trùng với một nhân khẩu đang cư trú. Vui lòng đối chiếu hồ sơ gốc.',
      };
    }
    const nextResident: ResidentMember = {
      id: residents.length + 10,
      householdId: input.householdId,
      fullName: input.fullName.trim(),
      identityNumberMasked: masked,
      dateOfBirth: input.dateOfBirth || null,
      relationshipToHead: input.relationshipToHead.trim() || 'Thành viên',
      residencyStatus: input.residencyStatus,
    };
    setResidents((prev) => [...prev, nextResident]);
    if (input.residencyStatus !== 'Đã chuyển đi') {
      setHouseholds((prev) =>
        prev.map((h) =>
          h.id === input.householdId ? { ...h, currentResidents: (h.currentResidents ?? 0) + 1 } : h,
        ),
      );
    }
    return { ok: true };
  };

  const recordMoveOutDemo = (residentId: number, effectiveDate: string, reason: string) => {
    const target = residents.find((r) => r.id === residentId);
    if (!target) return;
    setResidents((prev) =>
      prev.map((r) => (r.id === residentId ? { ...r, residencyStatus: 'Đã chuyển đi' } : r)),
    );
    setHouseholds((prev) =>
      prev.map((h) =>
        h.id === target.householdId
          ? { ...h, currentResidents: Math.max(0, (h.currentResidents ?? 0) - 1) }
          : h,
      ),
    );
    setResidencyChanges((prev) => [
      {
        id: prev.length + 10,
        residentId: target.id,
        householdId: target.householdId,
        changeType: 'CHUYEN_DI',
        effectiveDate,
        endDateAssumed: null,
        reason,
        status: 'APPROVED',
        recordedBy: 1,
        approvedByAssumed: 1,
        approvedAtAssumed: new Date().toISOString(),
      },
      ...prev,
    ]);
  };

  const submitAbsenceDemo: AppStateValue['submitAbsenceDemo'] = async (input) => {
    if (mode === 'api') {
      try { await createDeclaration({ residentId: input.residentId, effectiveDate: input.effectiveDate, ...(input.endDateAssumed ? { endDate: input.endDateAssumed } : {}), reason: input.reason }); await refreshAbsenceCount(); }
      catch (err) { handleCallFailure(err); throw err; }
      return;
    }
    const target = residents.find((r) => r.id === input.residentId);
    const householdId = target ? target.householdId : user?.householdId || 1;
    setResidencyChanges((prev) => [
      {
        id: prev.length + 10,
        residentId: input.residentId,
        householdId,
        changeType: 'TAM_VANG',
        effectiveDate: input.effectiveDate,
        endDateAssumed: input.endDateAssumed || null,
        reason: input.reason.trim(),
        status: 'PENDING',
        recordedBy: 3,
      },
      ...prev,
    ]);
  };

  const decideAbsenceDemo = async (
    changeId: number,
    decision: 'APPROVED' | 'REJECTED',
    rejectReason?: string,
  ) => {
    if (mode === 'api') {
      try { if (decision === 'APPROVED') await approveDeclaration(changeId); else await rejectDeclaration(changeId, rejectReason ?? ''); await refreshAbsenceCount(); }
      catch (err) { handleCallFailure(err); throw err; }
      return;
    }
    const targetChange = residencyChanges.find((c) => c.id === changeId);
    setResidencyChanges((prev) =>
      prev.map((c) =>
        c.id === changeId
          ? {
              ...c,
              status: decision,
              approvedByAssumed: 1,
              approvedAtAssumed: new Date().toISOString(),
              rejectReasonAssumed: decision === 'REJECTED' ? rejectReason || null : null,
            }
          : c,
      ),
    );
    if (decision === 'APPROVED' && targetChange) {
      setResidents((prev) =>
        prev.map((r) =>
          r.id === targetChange.residentId ? { ...r, residencyStatus: 'Tạm vắng' } : r,
        ),
      );
    }
  };

  const collectInvoiceCashDemo = (invoiceId: number, note?: string): PaymentTransaction | null => {
    const inv = invoices.find((i) => i.id === invoiceId);
    if (!inv || inv.status === 'DA_DONG') return null;
    const tx: PaymentTransaction = {
      id: payments.length + 10,
      invoiceId: inv.id,
      apartmentCode: inv.apartmentCode,
      period: inv.period,
      amount: inv.remaining,
      paidAt: new Date().toISOString(),
      receivedBy: user?.fullName || 'Demo Kế Toán',
      note,
    };
    setInvoices((prev) =>
      prev.map((i) =>
        i.id === invoiceId ? { ...i, status: 'DA_DONG', paid: i.total, remaining: '0' } : i,
      ),
    );
    setPayments((prev) => [tx, ...prev]);
    return tx;
  };

  const publishVoluntaryCampaignDemo = (
    name: string,
    description: string,
    opensOn: string,
    closesOn: string,
  ) => {
    setVoluntaryCampaigns((prev) => [
      {
        id: prev.length + 10,
        name: name.trim(),
        description: description.trim(),
        status: 'PUBLISHED',
        opensOn,
        closesOn,
        note: 'Tự nguyện, không tính vào công nợ',
        contributionsCount: 0,
        totalCollectedDisplay: '0',
      },
      ...prev,
    ]);
  };

  const approveReportPackageDemo = (period: string, allowedFields: string[]) => {
    setReportPackages((prev) => [
      {
        id: prev.length + 10,
        type: 'DANH_SACH_LUU_TRU',
        typeLabel: 'Danh sách lưu trú',
        period,
        status: 'APPROVED',
        approvedBy: user?.fullName || 'Demo Ban Quản Trị',
        approvedAt: new Date().toISOString(),
        allowedFields,
      },
      ...prev,
    ]);
  };

  const value = useMemo<AppStateValue>(
    () => ({
      mode,
      setMode,
      backendReachable,
      authLoading,
      user,
      returnToPath,
      sessionExpiredBanner,
      clearSessionExpiredBanner: () => setSessionExpiredBanner(false),
      forcedUiState,
      setForcedUiState,
      qaDrawerOpen,
      setQaDrawerOpen,
      absenceDraft,
      setAbsenceDraft,
      login,
      loginDemoAs,
      logout,
      triggerSessionExpired,
      completePasswordChangeDemo,
      households,
      residents,
      accounts,
      residencyChanges,
      invoices,
      payments,
      voluntaryCampaigns,
      reportPackages,
      collectionRate,
      provisionAccount,
      dataLoadError,
      absenceCount,
      refreshAbsenceCount,
      handleCallFailure,
      reloadHouseholds,
      loadHouseholdDetail,
      saveHousehold,
      addResident,
      recordMoveOut,
      manageAccount,
      disableAccountDemo,
      unlockAccountDemo,
      saveHouseholdDemo,
      addResidentDemo,
      recordMoveOutDemo,
      submitAbsenceDemo,
      decideAbsenceDemo,
      collectInvoiceCashDemo,
      publishVoluntaryCampaignDemo,
      approveReportPackageDemo,
    }),
    [
      mode,
      backendReachable,
      authLoading,
      user,
      returnToPath,
      sessionExpiredBanner,
      forcedUiState,
      qaDrawerOpen,
      absenceDraft,
      households,
      residents,
      accounts,
      residencyChanges,
      invoices,
      payments,
      voluntaryCampaigns,
      reportPackages,
      collectionRate,
      absenceCount,
    ],
  );

  return <AppStateContext.Provider value={value}>{children}</AppStateContext.Provider>;
}

export function useAppState(): AppStateValue {
  const ctx = useContext(AppStateContext);
  if (!ctx) {
    throw new Error('useAppState must be used inside AppStateProvider');
  }
  return ctx;
}
