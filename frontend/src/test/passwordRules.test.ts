import { describe, expect, it } from 'vitest';
import { passwordRuleChecks, passwordRulesOk } from '../lib/passwordRules';

describe('passwordRules (D23, phản chiếu quy tắc máy chủ S03)', () => {
  it('chấp nhận mật khẩu đủ hoa, thường, số, đặc biệt và 8 ký tự', () => {
    expect(passwordRulesOk(passwordRuleChecks('Abcdef1!'))).toBe(true);
  });

  it('từ chối 7 ký tự và thiếu từng loại ký tự', () => {
    expect(passwordRuleChecks('Abcde1!').length).toBe(false);
    expect(passwordRuleChecks('abcdef1!').upper).toBe(false);
    expect(passwordRuleChecks('ABCDEF1!').lower).toBe(false);
    expect(passwordRuleChecks('Abcdefg!').digit).toBe(false);
    expect(passwordRuleChecks('Abcdefg1').special).toBe(false);
  });

  it('chữ có dấu tính là chữ hoa/thường; khoảng trắng tính là ký tự đặc biệt', () => {
    expect(passwordRulesOk(passwordRuleChecks('Ăbcdef1!'))).toBe(true);
    expect(passwordRulesOk(passwordRuleChecks('Abcdef1 '))).toBe(true);
  });

  it('giới hạn 72 byte UTF-8: 72 qua, 73 bị từ chối, chữ ậ chiếm 3 byte', () => {
    const base = 'Aa1!';
    expect(passwordRuleChecks(base + 'x'.repeat(68)).maxBytes).toBe(true);
    expect(passwordRuleChecks(base + 'x'.repeat(69)).maxBytes).toBe(false);
    expect(passwordRuleChecks(base + 'ậ'.repeat(22)).maxBytes).toBe(true); // 4 + 66 = 70 byte
    expect(passwordRuleChecks(base + 'ậ'.repeat(23)).maxBytes).toBe(false); // 4 + 69 = 73 byte
  });
});
