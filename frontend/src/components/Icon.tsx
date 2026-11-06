import React from 'react';

export type IconName =
  | 'building'
  | 'receipt'
  | 'users'
  | 'calendar'
  | 'heart'
  | 'shield'
  | 'file-spreadsheet'
  | 'package-check'
  | 'wallet'
  | 'coins'
  | 'eye'
  | 'eye-off'
  | 'lock'
  | 'unlock'
  | 'check'
  | 'alert'
  | 'info'
  | 'search'
  | 'plus'
  | 'arrow-left'
  | 'log-out'
  | 'user-cog'
  | 'sliders'
  | 'close'
  | 'archive';

interface IconProps {
  name: IconName;
  size?: number;
  title?: string;
}

export function Icon({ name, size = 18, title }: IconProps): React.ReactElement {
  const common = {
    width: size,
    height: size,
    viewBox: '0 0 24 24',
    fill: 'none',
    stroke: 'currentColor',
    strokeWidth: 1.75,
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
    'aria-hidden': title ? undefined : true,
    role: title ? ('img' as const) : undefined,
    style: { flexShrink: 0 },
  };

  switch (name) {
    case 'building':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="4" y="3" width="16" height="18" rx="2" />
          <path d="M9 7h2M13 7h2M9 11h2M13 11h2M9 15h2M13 15h2M10 21v-3h4v3" />
        </svg>
      );
    case 'receipt':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M5 3h14v18l-2.5-1.8L14 21l-2-1.8L10 21l-2.5-1.8L5 21V3Z" />
          <path d="M9 8h6M9 12h6M9 16h4" />
        </svg>
      );
    case 'users':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <circle cx="9" cy="8" r="3.5" />
          <path d="M3.5 19.5a5.5 5.5 0 0 1 11 0" />
          <path d="M15.5 5.2a3.2 3.2 0 0 1 0 5.6M17.5 19.5a4.8 4.8 0 0 0-3-4.4" />
        </svg>
      );
    case 'calendar':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="3" y="5" width="18" height="16" rx="2" />
          <path d="M8 3v4M16 3v4M3 10h18" />
        </svg>
      );
    case 'heart':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M12 20.5s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10.5c0 5.6-7 10-7 10Z" />
        </svg>
      );
    case 'shield':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M12 3 5 6v6c0 4.5 3 7.8 7 9 4-1.2 7-4.5 7-9V6l-7-3Z" />
          <path d="m9.5 12 1.8 1.8 3.4-3.6" />
        </svg>
      );
    case 'file-spreadsheet':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V9l-6-6Z" />
          <path d="M14 3v6h6M8 13h8M8 17h8M12 13v4" />
        </svg>
      );
    case 'package-check':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M12 3 4 7v10l8 4 8-4V7l-8-4Z" />
          <path d="M4 7l8 4 8-4M12 11v10" />
        </svg>
      );
    case 'wallet':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="3" y="6" width="18" height="13" rx="2" />
          <path d="M16 12.5h5M6 6l9-2.5v2.5" />
        </svg>
      );
    case 'coins':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <ellipse cx="10" cy="7" rx="6" ry="3" />
          <path d="M4 7v5c0 1.7 2.7 3 6 3s6-1.3 6-3V7" />
          <path d="M16 11.5c2.3.5 4 1.5 4 2.8v3.7c0 1.6-2.5 3-5.5 3-2.2 0-4.1-.7-5-1.8" />
        </svg>
      );
    case 'eye':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M2.5 12S6 6 12 6s9.5 6 9.5 6-3.5 6-9.5 6-9.5-6-9.5-6Z" />
          <circle cx="12" cy="12" r="2.8" />
        </svg>
      );
    case 'eye-off':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M3 3l18 18M10.6 10.6a2 2 0 0 0 2.8 2.8" />
          <path d="M9.9 5.2A10.4 10.4 0 0 1 12 5c6 0 9.5 7 9.5 7a16.5 16.5 0 0 1-3.2 4.1M6.2 6.3C4 7.9 2.5 12 2.5 12s3.5 7 9.5 7c1.6 0 3-.4 4.2-1" />
        </svg>
      );
    case 'lock':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="5" y="11" width="14" height="10" rx="2" />
          <path d="M8 11V7a4 4 0 0 1 8 0v4" />
        </svg>
      );
    case 'unlock':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="5" y="11" width="14" height="10" rx="2" />
          <path d="M8 11V7a4 4 0 0 1 7.6-1.8" />
        </svg>
      );
    case 'check':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M5 12.5 9.6 17 19 7.5" />
        </svg>
      );
    case 'alert':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M12 4 3 19h18L12 4Z" />
          <path d="M12 10v4M12 17h.01" />
        </svg>
      );
    case 'info':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <circle cx="12" cy="12" r="9" />
          <path d="M12 11v5M12 8h.01" />
        </svg>
      );
    case 'search':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.8-3.8" />
        </svg>
      );
    case 'plus':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M12 5v14M5 12h14" />
        </svg>
      );
    case 'arrow-left':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M19 12H5M11 6l-6 6 6 6" />
        </svg>
      );
    case 'log-out':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <path d="m16 17 5-5-5-5M21 12H9" />
        </svg>
      );
    case 'user-cog':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <circle cx="10" cy="8" r="3.5" />
          <path d="M3.5 19.5a6.5 6.5 0 0 1 11.2-4.4" />
          <circle cx="18" cy="17" r="2.5" />
          <path d="M18 13.5v1M18 19.5v1M14.5 17h1M20.5 17h1" />
        </svg>
      );
    case 'sliders':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M4 6h16M4 12h16M4 18h16" />
          <circle cx="9" cy="6" r="2" fill="currentColor" />
          <circle cx="15" cy="12" r="2" fill="currentColor" />
          <circle cx="10" cy="18" r="2" fill="currentColor" />
        </svg>
      );
    case 'close':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <path d="M6 6l12 12M18 6 6 18" />
        </svg>
      );
    case 'archive':
      return (
        <svg {...common}>
          {title ? <title>{title}</title> : null}
          <rect x="3" y="4" width="18" height="4" rx="1" />
          <path d="M5 8v11a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V8M10 12h4" />
        </svg>
      );
  }
}
