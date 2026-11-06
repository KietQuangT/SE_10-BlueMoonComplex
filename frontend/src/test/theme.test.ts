import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { applyTheme, readPref, resolveTheme, savePref } from '../lib/theme';

// Node's experimental global localStorage shadows jsdom's; use a plain in-memory one.
beforeEach(() => {
  const m = new Map<string, string>();
  vi.stubGlobal('localStorage', {
    getItem: (k: string) => m.get(k) ?? null,
    setItem: (k: string, v: string) => void m.set(k, v),
    removeItem: (k: string) => void m.delete(k),
  });
});

afterEach(() => {
  vi.unstubAllGlobals();
  delete document.documentElement.dataset.theme;
});

describe('theme', () => {
  it('auto follows the system, an explicit choice wins', () => {
    expect(resolveTheme('auto', true)).toBe('dark');
    expect(resolveTheme('auto', false)).toBe('light');
    expect(resolveTheme('light', true)).toBe('light');
    expect(resolveTheme('dark', false)).toBe('dark');
  });

  it('saves an explicit choice, forgets it on auto, and sets data-theme', () => {
    savePref('dark');
    expect(readPref()).toBe('dark');
    expect(document.documentElement.dataset.theme).toBe('dark');
    savePref('auto');
    expect(readPref()).toBe('auto');
    expect(localStorage.getItem('bm-theme')).toBeNull();
  });

  it('ignores a corrupted stored value', () => {
    localStorage.setItem('bm-theme', 'purple');
    expect(readPref()).toBe('auto');
    applyTheme('auto'); // jsdom has no matchMedia: falls back to light
    expect(document.documentElement.dataset.theme).toBe('light');
  });
});
