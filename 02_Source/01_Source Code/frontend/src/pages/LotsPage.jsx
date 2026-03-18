import { useState, useEffect, useCallback } from 'react';
import { materialApi, lotApi } from '../services/api';
import Modal from '../components/Modal';
import StatusBadge from '../components/StatusBadge';

const LOT_STATUSES = ['Quarantine', 'Accepted', 'Rejected', 'Depleted'];

const TX_ICONS = {
  Receipt:    { icon: '📥', cls: 'positive' },
  Usage:      { icon: '📤', cls: 'negative' },
  Split:      { icon: '✂️', cls: 'negative' },
  Adjustment: { icon: '🔧', cls: 'neutral'  },
  Transfer:   { icon: '🔄', cls: 'neutral'  },
  Disposal:   { icon: '🗑',  cls: 'negative' },
};

const ALLOWED_TRANSITIONS = {
  Quarantine: ['Accepted', 'Rejected'],
  Accepted:   ['Depleted'],
  Rejected:   ['Depleted'],
  Depleted:   [],
};

// ─── Receive Lot Form ────────────────────────────────────────────────────────
function ReceiveLotForm({ materials, onSubmit, onClose, loading, error }) {
  const today = new Date().toISOString().split('T')[0];
  const [form, setForm] = useState({
    materialId: '',
    manufacturerLot: '',
    quantity: '',
    unitOfMeasure: 'kg',
    receivedDate: today,
    expirationDate: '',
    storageLocation: '',
    performedBy: '',
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(form); }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group form-full">
            <label className="form-label required">Vật tư</label>
            <select
              className="form-control"
              value={form.materialId}
              onChange={(e) => set('materialId', e.target.value)}
              required
              id="lot-materialId"
            >
              <option value="">-- Chọn vật tư --</option>
              {materials.map((m) => (
                <option key={m.materialId} value={m.materialId}>
                  {m.partNumber} — {m.materialName}
                </option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Mã lô nhà sản xuất</label>
            <input
              className="form-control"
              value={form.manufacturerLot}
              onChange={(e) => set('manufacturerLot', e.target.value)}
              placeholder="VD: LOT-A01"
              id="lot-manufacturerLot"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Số lượng</label>
            <input
              className="form-control"
              type="number"
              min="0.001"
              step="0.001"
              value={form.quantity}
              onChange={(e) => set('quantity', e.target.value)}
              placeholder="VD: 25.5"
              required
              id="lot-quantity"
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
              id="lot-uom"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Vị trí lưu kho</label>
            <input
              className="form-control"
              value={form.storageLocation}
              onChange={(e) => set('storageLocation', e.target.value)}
              placeholder="VD: Kho A - Kệ 3"
              id="lot-location"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Ngày nhận</label>
            <input
              className="form-control"
              type="date"
              value={form.receivedDate}
              onChange={(e) => set('receivedDate', e.target.value)}
              id="lot-receivedDate"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Ngày hết hạn</label>
            <input
              className="form-control"
              type="date"
              value={form.expirationDate}
              onChange={(e) => set('expirationDate', e.target.value)}
              id="lot-expirationDate"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Người thực hiện</label>
            <input
              className="form-control"
              value={form.performedBy}
              onChange={(e) => set('performedBy', e.target.value)}
              placeholder="VD: jdoe"
              id="lot-performedBy"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="btn-submit-receive">
          {loading ? '⏳ Đang nhập kho...' : '📥 Nhập kho'}
        </button>
      </div>
    </form>
  );
}

// ─── Update Status Modal ─────────────────────────────────────────────────────
function UpdateStatusModal({ lot, onSubmit, onClose, loading, error }) {
  const allowed = ALLOWED_TRANSITIONS[lot.status] ?? [];
  const [newStatus, setNewStatus] = useState(allowed[0] ?? '');
  const [performedBy, setPerformedBy] = useState('');

  if (allowed.length === 0) return (
    <Modal title="Cập nhật trạng thái" onClose={onClose}>
      <div className="alert alert-error">Lot này đã ở trạng thái cuối ({lot.status}), không thể chuyển tiếp.</div>
      <div className="modal-footer"><button className="btn btn-outline" onClick={onClose}>Đóng</button></div>
    </Modal>
  );

  return (
    <Modal
      title={`🔄 Cập nhật trạng thái Lot`}
      onClose={onClose}
      footer={
        <>
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
          <button
            id="btn-confirm-status"
            className="btn btn-primary"
            disabled={loading || !newStatus}
            onClick={() => onSubmit(newStatus, performedBy)}
          >
            {loading ? 'Đang cập nhật...' : '✔ Xác nhận'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error">⚠ {error}</div>}
      <p style={{ marginBottom: 12 }}>
        Lot hiện tại: <strong>{lot.lotId?.slice(0, 8)}…</strong> — <StatusBadge status={lot.status} />
      </p>
      <div className="form-group">
        <label className="form-label required">Trạng thái mới</label>
        <select
          className="form-control"
          value={newStatus}
          onChange={(e) => setNewStatus(e.target.value)}
          id="new-status-select"
        >
          {allowed.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
      <div className="form-group">
        <label className="form-label">Người thực hiện</label>
        <input
          className="form-control"
          value={performedBy}
          onChange={(e) => setPerformedBy(e.target.value)}
          placeholder="VD: jdoe"
          id="status-performedBy"
        />
      </div>
    </Modal>
  );
}

// ─── Transaction History Panel ────────────────────────────────────────────────
function TransactionHistoryModal({ lotId, onClose }) {
  const [txs, setTxs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    lotApi.getTransactions(lotId)
      .then((r) => setTxs(r.data))
      .catch(() => setError('Không thể tải lịch sử giao dịch'))
      .finally(() => setLoading(false));
  }, [lotId]);

  return (
    <Modal title="📋 Lịch sử giao dịch" onClose={onClose} size="modal-lg"
      footer={<button className="btn btn-outline" onClick={onClose}>Đóng</button>}
    >
      {loading && <div className="loading-center"><div className="spinner" /></div>}
      {error  && <div className="alert alert-error">{error}</div>}
      {!loading && !error && txs.length === 0 && (
        <div className="empty-state"><p>Chưa có giao dịch nào</p></div>
      )}
      {txs.map((tx) => {
        const cfg = TX_ICONS[tx.transactionType] ?? { icon: '📄', cls: 'neutral' };
        const qty = Number(tx.quantity);
        return (
          <div key={tx.transactionId} className="tx-item">
            <span className="tx-icon">{cfg.icon}</span>
            <div className="tx-body">
              <div className="tx-type">{tx.transactionType}</div>
              <div className="tx-meta">
                {tx.performedBy && <span>👤 {tx.performedBy} · </span>}
                {tx.notes && <span>{tx.notes} · </span>}
                {tx.transactionDate && new Date(tx.transactionDate).toLocaleString('vi-VN')}
              </div>
            </div>
            <span className={`tx-qty ${cfg.cls}`}>
              {qty > 0 ? `+${qty}` : qty}
            </span>
          </div>
        );
      })}
    </Modal>
  );
}

// ─── Main Page ────────────────────────────────────────────────────────────────
export default function LotsPage() {
  const [lots, setLots] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [statusFilter, setStatusFilter] = useState('');
  const [materialFilter, setMaterialFilter] = useState('');

  const [showReceive, setShowReceive] = useState(false);
  const [statusTarget, setStatusTarget] = useState(null);
  const [txLotId, setTxLotId] = useState(null);

  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadLots = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;
      if (materialFilter) params.materialId = materialFilter;
      const res = await lotApi.getAll(params);
      setLots(res.data.content ?? res.data);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách lot');
    } finally {
      setLoading(false);
    }
  }, [statusFilter, materialFilter]);

  useEffect(() => {
    materialApi.getAll().then((r) => setMaterials(r.data.content ?? r.data)).catch(() => {});
  }, []);

  useEffect(() => { loadLots(); }, [loadLots]);

  // Stats
  const statsMap = LOT_STATUSES.reduce((acc, s) => {
    acc[s] = lots.filter((l) => l.status === s).length;
    return acc;
  }, {});

  const handleReceive = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await lotApi.receive({ ...form, quantity: parseFloat(form.quantity) });
      setShowReceive(false);
      flash('✅ Nhập kho thành công');
      loadLots();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Nhập kho thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus, performedBy) => {
    setFormLoading(true);
    setFormError('');
    try {
      await lotApi.updateStatus(statusTarget.lotId, newStatus, performedBy);
      setStatusTarget(null);
      flash(`✅ Đã chuyển trạng thái → ${newStatus}`);
      loadLots();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Cập nhật trạng thái thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>📦 Tồn kho (Inventory Lots)</h1>
          <p>Quản lý lô hàng, trạng thái và lịch sử giao dịch</p>
        </div>
        <button
          id="btn-receive-lot"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowReceive(true); }}
        >
          📥 Nhập kho
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        {/* Stats */}
        <div className="stats-row">
          <div className="stat-card">
            <span className="stat-label">Tổng lô</span>
            <span className="stat-value">{lots.length}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label" style={{ color: 'var(--warning)' }}>Quarantine</span>
            <span className="stat-value" style={{ color: 'var(--warning)' }}>{statsMap.Quarantine ?? 0}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label" style={{ color: 'var(--success)' }}>Accepted</span>
            <span className="stat-value" style={{ color: 'var(--success)' }}>{statsMap.Accepted ?? 0}</span>
          </div>
          <div className="stat-card">
            <span className="stat-label" style={{ color: 'var(--danger)' }}>Rejected</span>
            <span className="stat-value" style={{ color: 'var(--danger)' }}>{statsMap.Rejected ?? 0}</span>
          </div>
        </div>

        <div className="card">
          {/* Filters */}
          <div className="filter-bar">
            <select
              className="form-control"
              value={materialFilter}
              onChange={(e) => setMaterialFilter(e.target.value)}
              id="filter-material"
              style={{ maxWidth: 260 }}
            >
              <option value="">🧪 Tất cả vật tư</option>
              {materials.map((m) => (
                <option key={m.materialId} value={m.materialId}>
                  {m.partNumber} — {m.materialName}
                </option>
              ))}
            </select>
            <div className="filter-chips">
              <button className={`chip ${!statusFilter ? 'active' : ''}`} onClick={() => setStatusFilter('')}>Tất cả</button>
              {LOT_STATUSES.map((s) => (
                <button key={s} className={`chip ${statusFilter === s ? 'active' : ''}`}
                  onClick={() => setStatusFilter(s === statusFilter ? '' : s)}>
                  {s}
                </button>
              ))}
            </div>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : lots.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📦</div>
              <p>Chưa có lô hàng nào. Nhấn <strong>Nhập kho</strong> để bắt đầu.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Vật tư</th>
                    <th>Lot ID</th>
                    <th>Mã lô NSX</th>
                    <th>Số lượng</th>
                    <th>Trạng thái</th>
                    <th>Ngày hết hạn</th>
                    <th>Vị trí kho</th>
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {lots.map((lot) => (
                    <tr key={lot.lotId}>
                      <td>
                        <div className="td-primary">{lot.materialName}</div>
                        <div className="text-muted">{lot.partNumber}</div>
                      </td>
                      <td className="td-mono">{lot.lotId || '—'}</td>
                      <td className="td-mono">{lot.manufacturerLot || '—'}</td>
                      <td className="td-primary">
                        {lot.quantity} <span className="text-muted">{lot.unitOfMeasure}</span>
                      </td>
                      <td><StatusBadge status={lot.status} /></td>
                      <td className="text-muted">
                        {lot.expirationDate
                          ? new Date(lot.expirationDate).toLocaleDateString('vi-VN')
                          : '—'}
                      </td>
                      <td>{lot.storageLocation || <span className="text-muted">—</span>}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                          <button
                            id={`btn-status-${lot.lotId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => { setFormError(''); setStatusTarget(lot); }}
                            disabled={lot.status === 'Depleted'}
                          >🔄 Status</button>
                          <button
                            id={`btn-tx-${lot.lotId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => setTxLotId(lot.lotId)}
                          >📋 Lịch sử</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {/* Receive Lot Modal */}
      {showReceive && (
        <Modal title="📥 Nhập kho – Tạo Inventory Lot mới" onClose={() => setShowReceive(false)} size="modal-lg">
          <ReceiveLotForm
            materials={materials}
            onSubmit={handleReceive}
            onClose={() => setShowReceive(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Status Update Modal */}
      {statusTarget && (
        <UpdateStatusModal
          lot={statusTarget}
          onSubmit={handleStatusUpdate}
          onClose={() => setStatusTarget(null)}
          loading={formLoading}
          error={formError}
        />
      )}

      {/* Transaction History Modal */}
      {txLotId && <TransactionHistoryModal lotId={txLotId} onClose={() => setTxLotId(null)} />}
    </>
  );
}
