import { describe, expect, it } from 'vitest';
import { labelTableCells } from '../lib/stackTables';

describe('labelTableCells (narrow tables print one card per row)', () => {
  it('copies each header text onto the cell under it', () => {
    document.body.innerHTML = `
      <table class="ledger-table">
        <thead><tr><th>Kỳ thu</th><th>Hạn nộp</th><th><span class="sr-only">Thao tác</span></th></tr></thead>
        <tbody><tr><td>10/2026</td><td>15/11/2026</td><td><button>Xem</button></td></tr></tbody>
      </table>`;
    labelTableCells(document.body);
    const cells = Array.from(document.querySelectorAll('td'));
    expect(cells.map((c) => c.dataset.label)).toEqual(['Kỳ thu', 'Hạn nộp', 'Thao tác']);
  });

  it('gives a colspan row no label and keeps later columns aligned', () => {
    document.body.innerHTML = `
      <table class="ledger-table">
        <thead><tr><th>A</th><th>B</th></tr></thead>
        <tbody>
          <tr><td colspan="2">Không có dữ liệu</td></tr>
          <tr><td>1</td><td>2</td></tr>
        </tbody>
      </table>`;
    labelTableCells(document.body);
    const cells = Array.from(document.querySelectorAll('td'));
    expect(cells.map((c) => c.dataset.label)).toEqual(['', 'A', 'B']);
  });

  it('leaves tables without the ledger-table class alone', () => {
    document.body.innerHTML = `<table><thead><tr><th>X</th></tr></thead><tbody><tr><td>1</td></tr></tbody></table>`;
    labelTableCells(document.body);
    expect(document.querySelector('td')?.dataset.label).toBeUndefined();
  });
});
