/**
 * S03 / D23: quy tắc mật khẩu MỚI. Máy chủ là nơi quyết định; đây chỉ là gợi ý tức thời.
 * Đếm 8 ký tự theo đơn vị UTF-16 (giống `String.length()` của backend), tối đa 72 byte UTF-8.
 */
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_BYTES = 72;

export interface PasswordRuleChecks {
  length: boolean;
  maxBytes: boolean;
  upper: boolean;
  lower: boolean;
  digit: boolean;
  special: boolean;
}

export function passwordRuleChecks(password: string): PasswordRuleChecks {
  return {
    length: password.length >= PASSWORD_MIN_LENGTH,
    maxBytes: new TextEncoder().encode(password).length <= PASSWORD_MAX_BYTES,
    upper: /\p{Lu}/u.test(password),
    lower: /\p{Ll}/u.test(password),
    digit: /\p{Nd}/u.test(password),
    // Ký tự đặc biệt = mọi ký tự không phải chữ hoặc số, kể cả khoảng trắng.
    special: /[^\p{L}\p{Nd}]/u.test(password),
  };
}

export function passwordRulesOk(checks: PasswordRuleChecks): boolean {
  return Object.values(checks).every(Boolean);
}
