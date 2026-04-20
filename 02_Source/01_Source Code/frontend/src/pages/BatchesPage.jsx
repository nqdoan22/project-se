import { useState, useEffect, useCallback } from 'react';
import { batchApi, lotApi } from '../services/api';
import Modal from '../components/Modal';
import { AddComponentForm, ConfirmUsageModal, ModifyComponentForm, DeleteConfirmationModal } from './BatchComponentsActions';

const BATCH_STATUSES = ['Planned', 'InProgress', 'Complete', 'Rejected'];

function BatchStatusBadge({ status }) {
  const cls =
    status === 'Planned'    ? 'badge-planned'    :
    status === 'InProgress' ? 'badge-inprogress' :
    status === 'Complete'   ? 'badge-complete'   :
    status === 'Rejected'   ? 'badge-rejected'   : '';
  return (
    <span className={`badge ${cls}`}>
      <span className="badge-dot" />
      {status}
    </span>
  );
}

function CreateBatchForm({ onSubmit, onClose, loading, error }) {
  const today = new Date().toISOString().split('T')[0];
  const [form, setForm] = useState({
    productId: '',
    productName: '',
    batchNumber: '',
    batchSize: '',
    unitOfMeasure: 'kg',
    manufactureDate: today,
    expirationDate: '',
    status: 'Planned',
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit({ ...form, batchSize: parseFloat(form.batchSize) }); }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label required">Mã sản phẩm</label>
            <input
              className="form-control"
              value={form.productId}
              onChange={(e) => set('productId', e.target.value)}
              placeholder="VD: PROD-001"
              required
              id="batch-productId"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Tên sản phẩm</label>
            <input
              className="form-control"
              value={form.productName}
              onChange={(e) => set('productName', e.target.value)}
              placeholder="VD: Amoxicillin 500mg"
              required
              id="batch-productName"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Số lô sản xuất</label>
            <input
              className="form-control"
              value={form.batchNumber}
              onChange={(e) => set('batchNumber', e.target.value)}
              placeholder="VD: BATCH-2024-001"
              required
              id="batch-batchNumber"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Cỡ lô</label>
            <input
              className="form-control"
              type="number"
              min="0.001"
              step="0.001"
              value={form.batchSize}
              onChange={(e) => set('batchSize', e.target.value)}
              placeholder="VD: 1000"
              required
              id="batch-batchSize"
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
              id="batch-uom"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Trạng thái ban đầu</label>
            <select
              className="form-control"
              value={form.status}
              onChange={(e) => set('status', e.target.value)}
              id="batch-status"
            >
              {BATCH_STATUSES.map((s) => <option key={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Ngày sản xuất</label>
            <input
              className="form-control"
              type="date"
              value={form.manufactureDate}
              onChange={(e) => set('manufactureDate', e.target.value)}
              id="batch-manufactureDate"
            />
          </div>
          <div className="form-group">
            <label className="form-label">Ngày hết hạn</label>
            <input
              className="form-control"
              type="date"
              value={form.expirationDate}
              onChange={(e) => set('expirationDate', e.target.value)}
              id="batch-expirationDate"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="btn-submit-batch">
          {loading ? '⏳ Đang tạo...' : '➕ Tạo lô sản xuất'}
        </button>
      </div>
    </form>
  );
}

function UpdateStatusModal({ batch, onSubmit, onClose, loading, error }) {
  const [newStatus, setNewStatus] = useState(batch.status);

  return (
    <Modal
      title="🔄 Cập nhật trạng thái lô sản xuất"
      onClose={onClose}
      footer={
        <>
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
          <button
            id="btn-confirm-batch-status"
            className="btn btn-primary"
            disabled={loading || newStatus === batch.status}
            onClick={() => onSubmit(newStatus)}
          >
            {loading ? 'Đang cập nhật...' : '✔ Xác nhận'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error">⚠ {error}</div>}
      <p style={{ marginBottom: 12 }}>
        Lô: <strong>{batch.batchNumber}</strong> — <BatchStatusBadge status={batch.status} />
      </p>
      <div className="form-group">
        <label className="form-label required">Trạng thái mới</label>
        <select
          className="form-control"
          value={newStatus}
          onChange={(e) => setNewStatus(e.target.value)}
          id="batch-new-status"
        >
          {BATCH_STATUSES.map((s) => <option key={s}>{s}</option>)}
        </select>
      </div>
    </Modal>
  );
}

function BatchDetailModal({ batchId, lots, onClose }) {
  const [batch, setBatch] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [addCompTarget, setAddCompTarget] = useState(null);
  const [confirmTarget, setConfirmTarget] = useState(null);
  const [modifyTarget, setModifyTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [statusTarget, setStatusTarget] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadBatch = useCallback(async () => {
    setLoading(true);
    try {
      const res = await batchApi.getById(batchId);
      setBatch(res.data);
    } catch (e) {
      setError(e.response?.data?.message ?? 'Không thể tải chi tiết lô sản xuất');
    } finally {
      setLoading(false);
    }
  }, [batchId]);

  useEffect(() => { loadBatch(); }, [loadBatch]);

  const handleAddComponent = async (form) => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.addComponent(batchId, form);
      setAddCompTarget(null);
      flash('✅ Đã thêm nguyên liệu vào lô sản xuất');
      loadBatch();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Thêm nguyên liệu thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleConfirmUsage = async (actualQuantity, performedBy) => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.confirmComponent(confirmTarget.componentId, actualQuantity, performedBy);
      setConfirmTarget(null);
      flash('✅ Đã xác nhận số lượng sử dụng');
      loadBatch();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Xác nhận thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleModifyComponent = async (form) => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.updateComponent(modifyTarget.componentId, form);
      setModifyTarget(null);
      flash('✅ Đã cập nhật nguyên liệu');
      loadBatch();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Cập nhật nguyên liệu thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleDeleteComponent = async () => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.deleteComponent(deleteTarget.componentId);
      setDeleteTarget(null);
      flash('✅ Đã xóa nguyên liệu khỏi lô sản xuất');
      loadBatch();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Xóa nguyên liệu thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  const handleStatusUpdate = async (newStatus) => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.updateStatus(batchId, newStatus);
      setStatusTarget(null);
      flash(`✅ Đã cập nhật trạng thái → ${newStatus}`);
      loadBatch();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Cập nhật trạng thái thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  return (
    <Modal title="📋 Chi tiết lô sản xuất" onClose={onClose} size="modal-lg">
      {loading && <div className="loading-center"><div className="spinner" /></div>}
      {error && <div className="alert alert-error">{error}</div>}
      {successMsg && <div className="alert alert-success">{successMsg}</div>}
      {batch && (
        <>
          <div className="form-grid">
            <div className="form-group">
              <label className="form-label">Số lô sản xuất</label>
              <div className="td-mono" style={{ color: 'var(--primary)', fontFamily: 'monospace' }}>{batch.batchNumber}</div>
            </div>
            <div className="form-group">
              <label className="form-label">Sản phẩm</label>
              <div>{batch.productName} <span className="text-muted">({batch.productId})</span></div>
            </div>
            <div className="form-group">
              <label className="form-label">Cỡ lô</label>
              <div>{batch.batchSize} {batch.unitOfMeasure}</div>
            </div>
            <div className="form-group">
              <label className="form-label">Trạng thái</label>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <BatchStatusBadge status={batch.status} />
                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => setStatusTarget(batch)}
                >🔄 Đổi</button>
              </div>
            </div>
            <div className="form-group">
              <label className="form-label">Ngày sản xuất</label>
              <div>{batch.manufactureDate ? new Date(batch.manufactureDate).toLocaleDateString('vi-VN') : '—'}</div>
            </div>
            <div className="form-group">
              <label className="form-label">Ngày hết hạn</label>
              <div>{batch.expirationDate ? new Date(batch.expirationDate).toLocaleDateString('vi-VN') : '—'}</div>
            </div>
          </div>

          <div style={{ borderTop: '1px solid var(--border)', paddingTop: 16, marginTop: 4 }}>
            {batch.status !== 'Planned' && (
              <div className="alert alert-warning" style={{ marginBottom: 16 }}>
                ⚠️ Chỉ có thể thêm/sửa/xóa nguyên liệu khi lô còn ở trạng thái "Planned"
              </div>
            )}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
              <span className="detail-panel-title">
                Nguyên liệu ({batch.components?.filter(item => item.actualQuantity != null && item.actualQuantity > 0)?.length ?? 0}/{batch.components?.length ?? 0} đã xác nhận)
              </span>
              <button
                className="btn btn-outline btn-sm"
                disabled={batch.status !== 'Planned'}
                onClick={() => { setActionError(''); setAddCompTarget(batch); }}
              >➕ Thêm nguyên liệu</button>
            </div>

            {(!batch.components || batch.components.length === 0) ? (
              <div className="empty-state" style={{ padding: '24px 0' }}>
                <p>Chưa có nguyên liệu nào. Nhấn <strong>Thêm nguyên liệu</strong> để thêm.</p>
              </div>
            ) : (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Tên nguyên liệu</th>
                      <th>Part Number</th>
                      <th>Lot ID</th>
                      <th>Trạng thái lô</th>
                      <th>SL kế hoạch</th>
                      <th>SL thực tế</th>
                      <th>Người thêm</th>
                      <th style={{ textAlign: 'right' }}>Hành động</th>
                    </tr>
                  </thead>
                  <tbody>
                    {batch.components.map((c) => (
                      <tr key={c.componentId}>
                        <td className="td-primary">{c.materialName}</td>
                        <td className="td-mono">{c.partNumber || '—'}</td>
                        <td className="td-mono">{c.lotId || '—'}</td>
                        <td>{c.lotStatus || <span className="text-muted">—</span>}</td>
                        <td>{c.plannedQuantity} <span className="text-muted">{c.unitOfMeasure}</span></td>
                        <td>
                          {c.actualQuantity != null
                            ? <span style={{ color: 'var(--success)' }}>{c.actualQuantity} {c.unitOfMeasure}</span>
                            : <span className="text-muted">Chưa xác nhận</span>}
                        </td>
                        <td>{c.addedBy || <span className="text-muted">—</span>}</td>
                        <td>
                          <div style={{ display: 'flex', gap: 4, justifyContent: 'flex-end' }}>
                            {c.actualQuantity == null && (
                              <button
                                id={`btn-confirm-comp-${c.componentId}`}
                                className="btn btn-outline btn-sm"
                                onClick={() => { setActionError(''); setConfirmTarget(c); }}
                              >✔ Xác nhận</button>
                            )}
                            <button
                              id={`btn-edit-comp-${c.componentId}`}
                              className="btn btn-outline btn-sm"
                              disabled={batch.status !== 'Planned'}
                              onClick={() => { setActionError(''); setModifyTarget(c); }}
                            >✏️ Sửa</button>
                            <button
                              id={`btn-delete-comp-${c.componentId}`}
                              className="btn btn-outline btn-sm"
                              disabled={batch.status !== 'Planned'}
                              onClick={() => { setActionError(''); setDeleteTarget(c); }}
                              style={{ color: 'var(--error)' }}
                            >🗑️ Xóa</button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="modal-footer" style={{ padding: '16px 0 0', border: 'none' }}>
            <button className="btn btn-outline" onClick={onClose}>Đóng</button>
          </div>
        </>
      )}

      {addCompTarget && (
        <Modal title="➕ Thêm nguyên liệu vào lô" onClose={() => setAddCompTarget(null)} size="modal-lg">
          <AddComponentForm
            batchId={batchId}
            lots={lots}
            onSubmit={handleAddComponent}
            onClose={() => setAddCompTarget(null)}
            loading={actionLoading}
            error={actionError}
          />
        </Modal>
      )}

      {confirmTarget && (
        <ConfirmUsageModal
          component={confirmTarget}
          onSubmit={handleConfirmUsage}
          onClose={() => setConfirmTarget(null)}
          loading={actionLoading}
          error={actionError}
        />
      )}

      {statusTarget && (
        <UpdateStatusModal
          batch={statusTarget}
          onSubmit={handleStatusUpdate}
          onClose={() => setStatusTarget(null)}
          loading={actionLoading}
          error={actionError}
        />
      )}

      {modifyTarget && (
        <Modal title="✏️ Chỉnh sửa nguyên liệu" onClose={() => setModifyTarget(null)} size="modal-lg">
          <ModifyComponentForm
            component={modifyTarget}
            lots={lots}
            onSubmit={handleModifyComponent}
            onClose={() => setModifyTarget(null)}
            loading={actionLoading}
            error={actionError}
          />
        </Modal>
      )}

      {deleteTarget && (
        <DeleteConfirmationModal
          component={deleteTarget}
          onConfirm={handleDeleteComponent}
          onCancel={() => setDeleteTarget(null)}
          loading={actionLoading}
          error={actionError}
        />
      )}
    </Modal>
  );
}

export default function BatchesPage() {
  const [batches, setBatches] = useState([]);
  const [lots, setLots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [statusFilter, setStatusFilter] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [detailBatchId, setDetailBatchId] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  useEffect(() => {
    lotApi.getAll().then((r) => setLots(r.data?.content ?? [])).catch(() => {});
  }, []);

  const loadBatches = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const params = {};
      if (statusFilter) params.status = statusFilter;
      const res = await batchApi.getAll(params);
      setBatches(res.data.content ?? []);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách lô sản xuất');
    } finally {
      setLoading(false);
    }
  }, [statusFilter]);

  useEffect(() => { loadBatches(); }, [loadBatches]);

  const handleCreate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await batchApi.create(form);
      setShowCreate(false);
      flash('✅ Đã tạo lô sản xuất mới');
      loadBatches();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Tạo lô thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>⚗️ Lô sản xuất (Production Batches)</h1>
          <p>Quản lý lô sản xuất và nguyên liệu sử dụng</p>
        </div>
        <button
          id="btn-create-batch"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowCreate(true); }}
        >
          ➕ Tạo lô sản xuất
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        <div className="card">
          <div className="filter-bar">
            <div className="filter-chips">
              <button className={`chip ${!statusFilter ? 'active' : ''}`} onClick={() => setStatusFilter('')}>Tất cả</button>
              {BATCH_STATUSES.map((s) => (
                <button
                  key={s}
                  className={`chip ${statusFilter === s ? 'active' : ''}`}
                  onClick={() => setStatusFilter(s === statusFilter ? '' : s)}
                >{s}</button>
              ))}
            </div>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : batches.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">⚗️</div>
              <p>Chưa có lô sản xuất nào. Nhấn <strong>Tạo lô sản xuất</strong> để bắt đầu.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Số lô</th>
                    <th>Sản phẩm</th>
                    <th>Cỡ lô</th>
                    <th>Ngày sản xuất</th>
                    <th>Ngày hết hạn</th>
                    <th>Trạng thái</th>
                    {/* <th>Nguyên liệu</th> */}
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {batches.map((b) => (
                    <tr key={b.batchId}>
                      <td className="td-mono">{b.batchNumber}</td>
                      <td>
                        <div className="td-primary">{b.productName}</div>
                        <div className="text-muted">{b.productId}</div>
                      </td>
                      <td>{b.batchSize} <span className="text-muted">{b.unitOfMeasure}</span></td>
                      <td className="text-muted">
                        {b.manufactureDate ? new Date(b.manufactureDate).toLocaleDateString('vi-VN') : '—'}
                      </td>
                      <td className="text-muted">
                        {b.expirationDate ? new Date(b.expirationDate).toLocaleDateString('vi-VN') : '—'}
                      </td>
                      <td><BatchStatusBadge status={b.status} /></td>
                      {/* <td>
                        <span style={{ color: b.confirmedComponentCount === b.componentCount && b.componentCount > 0 ? 'var(--success)' : 'var(--text-secondary)' }}>
                          {b.confirmedComponentCount ?? 0}/{b.componentCount ?? 0}
                        </span>
                      </td> */}
                      <td>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                          <button
                            id={`btn-detail-${b.batchId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => setDetailBatchId(b.batchId)}
                          >📋 Chi tiết</button>
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

      {/* Create Batch Modal */}
      {showCreate && (
        <Modal title="➕ Tạo lô sản xuất mới" onClose={() => setShowCreate(false)} size="modal-lg">
          <CreateBatchForm
            onSubmit={handleCreate}
            onClose={() => setShowCreate(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Batch Detail Modal */}
      {detailBatchId && (
        <BatchDetailModal
          batchId={detailBatchId}
          lots={lots}
          onClose={() => setDetailBatchId(null)}
        />
      )}
    </>
  );
}
