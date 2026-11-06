/**
 * True only in `npm run dev` or a build made with VITE_ENABLE_DEMO=true. Team-facing labels
 * (screen codes, contract badges, mock-data notes, open questions, demo tools) render only then;
 * a production build shows residents and staff the product, not the build notes.
 */
export const SHOW_DEV_TOOLS: boolean =
  import.meta.env.DEV || import.meta.env.VITE_ENABLE_DEMO === 'true';
