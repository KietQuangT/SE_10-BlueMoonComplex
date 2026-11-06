import { useEffect, useState } from 'react';
import { applyTheme, readPref, savePref, type ThemePref } from '../lib/theme';

const NEXT: Record<ThemePref, ThemePref> = { auto: 'light', light: 'dark', dark: 'auto' };
const LABEL: Record<ThemePref, string> = { auto: 'Tự động', light: 'Sáng', dark: 'Tối' };

const ICON: Record<ThemePref, string> = {
  auto: 'M12 3a9 9 0 1 0 0 18V3Z',
  light:
    'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm0-6v2m0 16v2M2 12h2m16 0h2M5 5l1.5 1.5M17.5 17.5 19 19M19 5l-1.5 1.5M6.5 17.5 5 19',
  dark: 'M20 14.5A8 8 0 0 1 9.5 4 8 8 0 1 0 20 14.5Z',
};

export function ThemeToggle() {
  const [pref, setPref] = useState<ThemePref>(readPref);

  // In "auto" the page follows the system setting while it is open.
  useEffect(() => {
    if (pref !== 'auto' || !window.matchMedia) return;
    const mq = window.matchMedia('(prefers-color-scheme: dark)');
    const onChange = () => applyTheme('auto');
    mq.addEventListener('change', onChange);
    return () => mq.removeEventListener('change', onChange);
  }, [pref]);

  const next = NEXT[pref];
  return (
    <button
      type="button"
      className="btn btn-ghost btn-sm theme-toggle"
      aria-label={`Giao diện: ${LABEL[pref]}. Bấm để đổi sang ${LABEL[next]}`}
      onClick={() => {
        savePref(next);
        setPref(next);
      }}
    >
      <svg
        width="18"
        height="18"
        viewBox="0 0 24 24"
        fill={pref === 'light' ? 'none' : 'currentColor'}
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <path d={ICON[pref]} />
      </svg>
      <span className="theme-toggle-label">{LABEL[pref]}</span>
    </button>
  );
}
