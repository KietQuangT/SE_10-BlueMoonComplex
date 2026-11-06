import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import { AppStateProvider } from './state/AppStateContext';

import './styles/tokens.css';
import './styles/app.css';
import './styles/utilities.css';

const rootEl = document.getElementById('root');
if (!rootEl) {
  throw new Error('Element #root not found');
}

ReactDOM.createRoot(rootEl).render(
  <React.StrictMode>
    <AppStateProvider>
      <App />
    </AppStateProvider>
  </React.StrictMode>,
);
