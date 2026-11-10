import type {
  CreateAccountPayload,
  CreatedAccountDto,
  CsrfResponse,
  ProblemDetailError,
  SessionUser,
} from '../types/domain';

let csrfState: CsrfResponse | null = null;

export class ApiHttpError extends Error {
  readonly problem: ProblemDetailError;

  constructor(problem: ProblemDetailError) {
    super(problem.detail || problem.title || `HTTP ${problem.status}`);
    this.name = 'ApiHttpError';
    this.problem = problem;
  }
}

export function clearCsrfMemory(): void {
  csrfState = null;
}

export async function parseProblem(res: Response): Promise<ProblemDetailError> {
  const requestId = res.headers.get('X-Request-Id') || undefined;
  let payload: Record<string, unknown> = {};
  try {
    const text = await res.text();
    if (text) {
      payload = JSON.parse(text) as Record<string, unknown>;
    }
  } catch {
    // Ignore malformed JSON body; rely on HTTP status
  }
  return {
    status: res.status,
    title: typeof payload.title === 'string' ? payload.title : undefined,
    detail: typeof payload.detail === 'string' ? payload.detail : undefined,
    code: typeof payload.code === 'string' ? payload.code : undefined,
    requestId,
  };
}

export async function fetchCsrfToken(): Promise<CsrfResponse> {
  const res = await fetch('/api/v1/auth/csrf', {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  if (!res.ok) {
    throw new ApiHttpError(await parseProblem(res));
  }
  const data = (await res.json()) as CsrfResponse;
  csrfState = data;
  return data;
}

async function ensureCsrfToken(): Promise<CsrfResponse> {
  if (csrfState) return csrfState;
  return fetchCsrfToken();
}

export async function fetchCurrentUser(): Promise<SessionUser> {
  const res = await fetch('/api/v1/auth/me', {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  if (!res.ok) {
    throw new ApiHttpError(await parseProblem(res));
  }
  return (await res.json()) as SessionUser;
}

export async function loginWithCredentials(
  username: string,
  password: string,
): Promise<SessionUser> {
  const csrf = await ensureCsrfToken();
  const attempt = async (tokenToUse: CsrfResponse): Promise<Response> =>
    fetch('/api/v1/auth/login', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        [tokenToUse.headerName]: tokenToUse.token,
      },
      body: JSON.stringify({ username, password }),
    });

  let res = await attempt(csrf);
  if (res.status === 403) {
    const freshCsrf = await fetchCsrfToken();
    res = await attempt(freshCsrf);
  }
  if (!res.ok) {
    throw new ApiHttpError(await parseProblem(res));
  }
  const sessionUser = (await res.json()) as SessionUser;
  // S01 contract: session ID rotated on login, so immediately refresh CSRF token
  await fetchCsrfToken().catch(() => undefined);
  return sessionUser;
}

export async function logoutCurrentSession(): Promise<void> {
  const csrf = await ensureCsrfToken();
  const res = await fetch('/api/v1/auth/logout', {
    method: 'POST',
    credentials: 'include',
    headers: {
      [csrf.headerName]: csrf.token,
    },
  });
  // Refresh CSRF for subsequent login per 08-hop-dong-api-hien-co.md step 6
  clearCsrfMemory();
  await fetchCsrfToken().catch(() => undefined);
  if (!res.ok && res.status !== 401) {
    throw new ApiHttpError(await parseProblem(res));
  }
}

/**
 * Mutating request wrapper implementing S01/S02 CSRF + 403 disambiguation:
 * 1. If 403 carries code === 'PASSWORD_CHANGE_REQUIRED', propagate immediately.
 * 2. On other 403, probe GET /api/v1/auth/me:
 *    - 401 => session expired while writing; convert to 401 so caller preserves form draft.
 *    - 200 => refresh CSRF token once and retry; if still 403, throw genuine 403.
 */
export async function sendAuthenticatedWrite<TResponse>(
  url: string,
  method: 'POST' | 'PUT' | 'PATCH' | 'DELETE',
  body?: unknown,
  extraHeaders?: Record<string, string>,
  beforeSend?: () => Promise<void>,
): Promise<TResponse> {
  const csrf = await ensureCsrfToken();
  const execute = async (tokenObj: CsrfResponse): Promise<Response> => {
    if (beforeSend) await beforeSend();
    return fetch(url, {
      method,
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'application/json',
        [tokenObj.headerName]: tokenObj.token,
        ...extraHeaders,
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  };

  let res = await execute(csrf);
  if (res.status === 403) {
    const firstProblem = await parseProblem(res);
    if (firstProblem.code === 'PASSWORD_CHANGE_REQUIRED') {
      throw new ApiHttpError(firstProblem);
    }
    try {
      await fetchCurrentUser();
    } catch (meErr) {
      if (meErr instanceof ApiHttpError && meErr.problem.status === 401) {
        throw meErr;
      }
    }
    const refreshedCsrf = await fetchCsrfToken();
    res = await execute(refreshedCsrf);
  }

  if (!res.ok) {
    throw new ApiHttpError(await parseProblem(res));
  }
  if (res.status === 204) {
    return undefined as TResponse;
  }
  return (await res.json()) as TResponse;
}

export async function createAccountApi(
  payload: CreateAccountPayload,
): Promise<CreatedAccountDto> {
  return sendAuthenticatedWrite<CreatedAccountDto>('/api/v1/accounts', 'POST', payload);
}

/** Authenticated GET with the same error shape as the writes (ApiHttpError carries status, code, requestId). */
export async function getAuthenticated<TResponse>(url: string): Promise<TResponse> {
  const res = await fetch(url, {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: 'application/json' },
  });
  if (!res.ok) {
    throw new ApiHttpError(await parseProblem(res));
  }
  return (await res.json()) as TResponse;
}

/**
 * S03: đổi mật khẩu của chính người đang đăng nhập, trả 204. Thành công nghĩa là MỌI session của
 * tài khoản (kể cả session này) đã bị hủy: lấy lại CSRF rồi đăng nhập lại. Lỗi 400 có `code`:
 * CURRENT_PASSWORD_INCORRECT, PASSWORD_TOO_SHORT, PASSWORD_TOO_LONG, PASSWORD_WEAK, PASSWORD_SAME_AS_CURRENT.
 * 401 khi session còn sống nghĩa là tài khoản đang bị khóa do nhập sai (session KHÔNG bị hủy).
 */
export async function changePasswordApi(
  currentPassword: string,
  newPassword: string,
): Promise<void> {
  await sendAuthenticatedWrite<void>('/api/v1/auth/change-password', 'POST', {
    currentPassword,
    newPassword,
  });
  clearCsrfMemory();
  await fetchCsrfToken().catch(() => undefined);
}

/** S04: ADMIN only, 204. Disabling revokes every session of the account on the server. */
export async function manageAccountApi(
  id: number,
  action: 'disable' | 'enable' | 'unlock',
): Promise<void> {
  await sendAuthenticatedWrite<void>(`/api/v1/accounts/${id}/${action}`, 'POST');
}

/** Trang tài khoản chỉ dành cho ADMIN. */
export async function fetchAccountPage(page: number, signal?: AbortSignal): Promise<{
  content: Array<{ id: number; username: string; fullName: string; email: string | null; role: import('../types/domain').RoleCode; householdId: number | null; status: 'ACTIVE' | 'INACTIVE'; locked: boolean }>;
  page: number; totalPages: number; totalElements: number;
}> {
  const res = await fetch(`/api/v1/accounts?page=${page}&size=100`, { credentials: 'include', headers: { Accept: 'application/json' }, signal });
  if (!res.ok) throw new ApiHttpError(await parseProblem(res));
  return res.json();
}
