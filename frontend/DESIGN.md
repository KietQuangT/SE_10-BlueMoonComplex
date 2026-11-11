# DESIGN.md — BlueMoon Complex Frontend Design System

**Project:** SE_10 — BlueMoon Complex Residential & Fee Management System
**Visual References:** `/fe-reference/dashboard-desktop.png` (Dark Mode) & `/fe-reference/dashboard-responsive.png` (Light Mode)
**Theme Persistence:** `<html data-theme="dark|light">`, `localStorage['bm-theme']` (`src/lib/theme.ts`, `src/components/ThemeToggle.tsx`)

---

## 1. Visual Identity & Aesthetic Direction

- **Domain:** Administrative ledger & residential management portal for a high-rise apartment complex (Ban quản trị, Kế toán, Cư dân).
- **Signature Identity:** Calm, authoritative midnight-navy & cobalt-blue administrative workspace with the recognizable BlueMoon crescent emblem and subtle atmospheric moonlit radial glow.
- **Dual-Theme Architecture:**
  - **Dark Mode (`[data-theme='dark']`):** Deep midnight navy ground (`#050b18`), elevated navy panels (`#0a152c`), translucent topbar (`rgba(7, 15, 34, 0.86)`), gradient sidebar (`#071124` → `#0b1c3d`) with subtle crescent moon artwork, luminous periwinkle-blue primary actions (`#709dff` → `#5585f7`), and high-contrast ice-white typography (`#f2f6ff`).
  - **Light Mode (`[data-theme='light']`):** Pale moonlit porcelain ground (`#eef4fb`), crisp white panels (`#ffffff`), soft ice-blue sidebar (`#eff5fd` → `#dfebfa`), royal cobalt primary actions (`#3472f7` → `#2563eb`), and deep navy ink (`#0b1730`).

---

## 2. Color Tokens (`src/styles/tokens.css`)

### Surface & Ink Tokens

| Token | Light Mode | Dark Mode | Role |
|---|---|---|---|
| `--bg-ground` | `#eef4fb` | `#050b18` | Page canvas background |
| `--bg-surface` | `#ffffff` | `#0a152c` | Primary cards, tables, forms |
| `--bg-subtle` | `#f4f8fe` | `#0e1d3a` | Table headers, filter bars, card footers |
| `--bg-muted` | `#e6effa` | `#15284d` | Hover states, secondary fills |
| `--bg-input` | `#ffffff` | `#071124` | Form inputs, selects, textareas |
| `--ink-primary` | `#0b1730` | `#f2f6ff` | Headings, primary data, amounts |
| `--ink-secondary` | `#33476b` | `#b8c8e6` | Table headers, descriptions, labels |
| `--ink-muted` | `#5c7196` | `#8296bb` | Hints, timestamps, secondary metadata |
| `--border-subtle` | `#d5e1f2` | `rgba(74, 108, 172, 0.28)` | Panel borders, row dividers |
| `--border-strong` | `#8aa1c4` | `rgba(98, 138, 210, 0.48)` | Input borders, secondary buttons, role plates |
| `--accent-primary` | `#2563eb` | `#6392ff` | Active states, focus rings, links |

### Semantic Status Tokens (Pill Badges with Leading Dot)

| Status Tone | Class | Meaning | Light (`bg` / `ink`) | Dark (`bg` / `ink`) |
|---|---|---|---|---|
| **Paid / Active** | `.badge-paid` | Đang cư trú, Hoạt động, Đã đóng, Đã duyệt | `#e6f7ef` / `#0d6847` | `rgba(16,85,62,0.48)` / `#6ee7b7` |
| **Pending / Warn** | `.badge-pending` | Tạm vắng, Chưa đóng, Chờ duyệt, Nháp | `#fef5e4` / `#8c4b08` | `rgba(98,60,14,0.52)` / `#fcd34d` |
| **Danger / Locked** | `.badge-danger` | Đã khóa, Từ chối, Quá hạn, Lỗi | `#feebed` / `#991b1b` | `rgba(104,24,34,0.52)` / `#fca5a5` |
| **Neutral / Closed** | `.badge-neutral` | Đã chuyển đi, Ngừng dùng, Đã đóng, Đã hủy | `#edf2f9` / `#3b4c6b` | `rgba(38,54,86,0.62)` / `#c5d3eb` |
| **Assumed / Info** | `.badge-assumed` | Thông tin đối chiếu, Tạm trú | `#e8f1ff` / `#1e40af` | `rgba(26,62,132,0.52)` / `#93c5fd` |

---

## 3. Typography & Numeric Discipline

- **Display / Page Titles (`--font-display`):** `'Newsreader', Georgia, serif` — weight `500`, `clamp(1.85rem, 2.3vw, 2.35rem)` for `.screen-title` (`h1`), `1.35rem–1.55rem` for section headings and KPI figures.
- **Application UI & Controls (`--font-sans`):** `'Be Vietnam Pro', sans-serif` — full Vietnamese diacritic support, weights `400 / 500 / 600 / 700`.
- **Monospace & Codes (`--font-mono`):** `'JetBrains Mono', monospace` — used for apartment codes, household codes, invoice codes, and SHA-256 hashes.
- **Tabular Figures (`.tabular`):** `font-variant-numeric: tabular-nums` is enforced on all VND amounts, m² areas, dates (`DD/MM/YYYY`), periods (`MM/YYYY`), and counts.

---

## 4. Application Shell & Component Conventions

1. **Sidebar Navigation (`.app-rail`, `248px`):**
   - Left-aligned `.rail-brand` with glowing crescent emblem and stacked `BlueMoon` / `Complex` serif title.
   - Grouped navigation links (`.rail-group-heading`, `.rail-link`) with active royal-blue gradient pill (`.rail-link.active`), pending count badges (`.rail-badge`), user profile footer (`.rail-profile`), and decorative crescent moon backdrop (`.rail-moon-art`).
2. **Top Header Bar (`.app-topbar`, `62px`):**
   - Left scope: `.role-plate` for `ADMIN` / `ACCOUNTANT`, or `.apartment-sign` + area metadata for `RESIDENT`.
   - Right actions: `<ThemeToggle />`, user profile link (`Hồ sơ và đổi mật khẩu`), optional QA drawer trigger (`SHOW_DEV_TOOLS`), and logout button (`Đăng xuất`).
3. **Screen Header (`<ScreenHeader />`):**
   - Renders serif `h1.screen-title`, operational subtitle `p.screen-desc`, and right-aligned primary/secondary `.screen-actions`.
4. **KPI Summary Strip (`.wayfinding-strip` / `.s09-summary-strip`):**
   - Elevated cards (`.wayfinding-station`) displaying a status dot (`.station-node`), label (`.station-title`), serif tabular metric (`.station-figure`), and context subtitle (`.station-sub`).
5. **Surface Panels & Data Tables (`.surface-box`, `.box-toolbar`, `.ledger-table`, `<PaginationBar />`):**
   - `12px` radius panels with subtle elevation shadow, integrated filter toolbar (`.box-toolbar`), high-legibility table headers (`thead th`), hoverable/selectable rows, right-aligned monetary columns (`.align-right.tabular`), and bottom pagination bar.
6. **Dialogs & Modals (`<ConfirmDialog />`, `<FormDialog />`, `.s08-modal`, `.s09-modal`):**
   - Native `<dialog>` and accessible modal overlays with blurred backdrop, serif header, clear consequence copy, and right-aligned footer actions.
