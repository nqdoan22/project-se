import { useState, useEffect, useCallback } from 'react';
import { transactionApi, lotApi } from '../services/api';
import Modal from '../components/Modal';

const TX_TYPES = ['Receipt', 'Usage', 'Split', 'Transfer', 'Adjustment', 'Disposal'];

const TX_ICONS = {
  Receipt:    { icon: '📥', cls: 'positive' },
  Usage:      { icon: '📤', cls: 'negative' },
  Split:      { icon: '✂️', cls: 'negative' },
  Adjustment: { icon: '🔧', cls: 'neutral'  },
  Transfer:   { icon: '🔄', cls: 'neutral'  },
  Disposal:   { icon: '🗑',  cls: 'negative' },
};

// ─── Create Transaction Form ────────────────────────────────────────────────────
function CreateTransactionForm({ lots, onSubmit, onClose, loading, error }) {
  const today = new Date().toISOString().split('T')[0];
  const [form, setForm] = useState({
    lotId: '',
    transactionType: 'Usage',
    quantity: '',
    unitOfMeasure: 'kg',
    referenceId: '',
    notes: '',
    performedBy: '',
    transactionDate: new Date().toISOString().slice(0, 16),
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(form); }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group form-full">
            <label className="form-label required">Lot</label>
            <select
              className="form-control"
              value={form.lotId}
              onChange={(e) => set('lotId', e.target.value)}
              required
              id="tx-lotId"
            >
              <option value="">-- Chọn lô hàng --</option>
              {lots.map((l) => (
                <option key={l.lotId} value={l.lotId}>
                  {l.lotId?.slice(0, 8)}… — {l.materialName} ({l.quantity} {l.unitOfMeasure})
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label required">Loại giao dịch</label>
            <select
              className="form-control"
              value={form.transactionType}
              onChange={(e) => set('transactionType', e.target.value)}
              required
              id="tx-type"
            >
              {TX_TYPES.map((t) => (
                <option key={t} value={t}>{t}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label required">Số lượng</label>
            <input
              className="form-control"
              type="number"
              step="10"
              value={form.quantity}
              onChange={(e) => set('quantity', e.target.value)}
              placeholder="VD: 10.5"
              required
              id="tx-quantity"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Đơn vị</label>
            <input
              className="form-control"
              value={form.unitOfMeasure}
              onChange={(e) => set('unitOfMeasure', e.target.value)}
              placeholder="kg / L / pcs"
              required
              id="tx-uom"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Mã tham chiếu</label>
            <input
              className="form-control"
              value={form.referenceId}
              onChange={(e) => set('referenceId', e.target.value)}
              placeholder="VD: PO-12345"
              id="tx-refId"
              maxLength={50}
            />
          </div>
          <div className="form-group form-full">
            <label className="form-label">Ghi chú</label>
            <textarea
              className="form-control"
              value={form.notes}
              onChange={(e) => set('notes', e.target.value)}
              placeholder="Ghi chú thêm..."
              id="tx-notes"
              maxLength={500}
              rows={3}
            />
          </div>
          <div className="form-group">
            <label className="form-label">Ngày giao dịch</label>
            <input
              className="form-control"
              type="datetime-local"
              value={form.transactionDate}
              onChange={(e) => set('transactionDate', e.target.value)}
              id="tx-date"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Người thực hiện</label>
            <input
              className="form-control"
              value={form.performedBy}
              onChange={(e) => set('performedBy', e.target.value)}
              placeholder="VD: jdoe"
              id="tx-performedBy"
              maxLength={50}
              required
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="btn-submit-tx">
          {loading ? '⏳ Đang tạo...' : '✔ Tạo giao dịch'}
        </button>
      </div>
    </form>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function TransactionsPage() {
  const [transactions, setTransactions] = useState([]);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [showCreate, setShowCreate] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadTransactions = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const res = await transactionApi.getAll({ size: 50 });
      setTransactions(res.data.content ?? res.data);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách giao dịch');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // Load lots for dropdown
    lotApi.getAll({ size: 100 })
      .then((r) => setLots(r.data.content ?? r.data))
      .catch(() => {});
  }, []);

  useEffect(() => { loadTransactions(); }, [loadTransactions]);

  const handleCreate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await transactionApi.create({ 
        ...form, 
        quantity: parseFloat(form.quantity) 
      });
      setShowCreate(false);
      flash('✅ Tạo giao dịch thành công');
      loadTransactions();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Tạo giao dịch thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const getTxConfig = (type) => TX_ICONS[type] ?? { icon: '📄', cls: 'neutral' };

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>📋 Lịch sử giao dịch (Transaction History)</h1>
          <p>Xem và quản lý tất cả giao dịch kho hàng</p>
        </div>
        <button
          id="btn-create-tx"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowCreate(true); }}
        >
          ➕ Tạo giao dịch
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        <div className="card">
          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : transactions.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📋</div>
              <p>Chưa có giao dịch nào. Nhấn <strong>Tạo giao dịch</strong> để bắt đầu.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Loại</th>
                    <th>Lot ID</th>
                    <th>Số lượng</th>
                    <th>Ngày giao dịch</th>
                    <th>Người thực hiện</th>
                    <th>Mã tham chiếu</th>
                    <th>Ghi chú</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => {
                    const cfg = getTxConfig(tx.transactionType);
                    return (
                      <tr key={tx.transactionId}>
                        <td>
                          <span style={{ fontSize: '1.2em', marginRight: 8 }}>{cfg.icon}</span>
                          {tx.transactionType}
                        </td>
                        <td className="td-mono">{tx.lotId?.slice(0, 8)}…</td>
                        <td className={`td-primary ${cfg.cls}`}>
                          {Number(tx.quantity) > 0 ? '+' : ''}{tx.quantity} <span className="text-muted">{tx.unitOfMeasure}</span>
                        </td>
                        <td className="text-muted">
                          {tx.transactionDate ? new Date(tx.transactionDate).toLocaleString('vi-VN') : '—'}
                        </td>
                        <td>{tx.performedBy || '—'}</td>
                        <td className="text-muted">{tx.referenceId || '—'}</td>
                        <td>{tx.notes || <span className="text-muted">—</span>}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Create Transaction Modal */}
      {showCreate && (
        <Modal title="➕ Tạo giao dịch mới" onClose={() => setShowCreate(false)} size="modal-lg">
          <CreateTransactionForm
            lots={lots}
            onSubmit={handleCreate}
            onClose={() => setShowCreate(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}
    </>
  );
}
