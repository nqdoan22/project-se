import { useState, useEffect, useCallback } from 'react';
import { batchApi, lotApi, materialApi } from '../services/api';
import Modal from '../components/Modal';
import { AddComponentForm, ConfirmUsageModal, ModifyComponentForm, DeleteConfirmationModal } from './BatchComponentsActions';

const BATCH_STATUSES = ['Planned', 'InProgress', 'Complete', 'Rejected'];

function AddComponentToBatchForm({ batches, lots, onSubmit, onClose, loading, error }) {
  const [form, setForm] = useState({
    batchId: '',
    lotId: '',
    plannedQuantity: '',
    unitOfMeasure: '',
    addedBy: '',
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const acceptedLots = lots.filter((l) => l.status === 'Accepted');
  const plannedBatches = batches.filter((b) => b.status === 'Planned');

  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      onSubmit({ ...form, plannedQuantity: parseFloat(form.plannedQuantity) });
    }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group form-full">
            <label className="form-label required">Chọn lô sản xuất</label>
            <select
              className="form-control"
              value={form.batchId}
              onChange={(e) => set('batchId', e.target.value)}
              required
              id="add-comp-batchId"
            >
              <option value="">-- Chọn lô sản xuất --</option>
              {plannedBatches.map((b) => (
                <option key={b.batchId} value={b.batchId}>
                  {b.batchNumber} — {b.productName} ({b.status})
                </option>
              ))}
            </select>
            {plannedBatches.length === 0 && (
              <span className="form-error">Không có lô sản xuất nào ở trạng thái Planned</span>
            )}
          </div>
          <div className="form-group form-full">
            <label className="form-label required">Lô nguyên liệu (Accepted)</label>
            <select
              className="form-control"
              value={form.lotId}
              onChange={(e) => set('lotId', e.target.value)}
              required
              id="add-comp-lotId"
            >
              <option value="">-- Chọn lô nguyên liệu --</option>
              {acceptedLots.map((l) => (
                <option key={l.lotId} value={l.lotId}>
                  {l.partNumber} — {l.materialName} | SL: {l.quantity} {l.unitOfMeasure}
                </option>
              ))}
            </select>
            {acceptedLots.length === 0 && (
              <span className="form-error">Không có lô nào ở trạng thái Accepted</span>
            )}
          </div>
          <div className="form-group">
            <label className="form-label required">Số lượng kế hoạch</label>
            <input
              className="form-control"
              type="number"
              min="0.001"
              step="0.001"
              value={form.plannedQuantity}
              onChange={(e) => set('plannedQuantity', e.target.value)}
              placeholder="VD: 100"
              required
              id="add-comp-plannedQty"
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
              id="add-comp-uom"
            />
          </div>
          <div className="form-group form-full">
            <label className="form-label">Người thêm</label>
            <input
              className="form-control"
              value={form.addedBy}
              onChange={(e) => set('addedBy', e.target.value)}
              placeholder="VD: operator01"
              id="add-comp-addedBy"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading || plannedBatches.length === 0 || acceptedLots.length === 0} id="btn-add-component">
          {loading ? '⏳ Đang thêm...' : '➕ Thêm nguyên liệu'}
        </button>
      </div>
    </form>
  );
}

export default function BatchComponentsPage() {
  const [batches, setBatches] = useState([]);
  const [lots, setLots] = useState([]);
  const [materials, setMaterials] = useState([]);
  const [allComponents, setAllComponents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [materialFilter, setMaterialFilter] = useState('');
  const [batchFilter, setBatchFilter] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [confirmTarget, setConfirmTarget] = useState(null);
  const [modifyTarget, setModifyTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [actionError, setActionError] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadBatches = useCallback(async () => {
    try {
      const res = await batchApi.getAll({ size: 1000 });
      setBatches(res.data.content ?? []);
    } catch (e) {
      console.error('Failed to load batches');
    }
  }, []);

  const loadMaterials = useCallback(async () => {
    try {
      const res = await materialApi.getAll({ size: 1000 });
      setMaterials(res.data.content ?? []);
      console.log('Loaded materials:', res.data.content ?? []);
    } catch (e) {
      console.error('Failed to load materials');
    }
  }, []);

  const loadAllComponents = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const res = await batchApi.getAll({ size: 1000 });
      const batchesData = res.data.content ?? [];
      
      // Collect all components from all batches
      const components = [];
      for (const batch of batchesData) {
        const batchDetails = await batchApi.getById(batch.batchId);
        if (batchDetails.data.components) {
          batchDetails.data.components.forEach((comp) => {
            components.push({
              ...comp,
              batchId: batch.batchId,
              batchNumber: batch.batchNumber,
              productName: batch.productName,
              productId: batch.productId,
              batchStatus: batch.status,
            });
          });
        }
      }
      setAllComponents(components);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách nguyên liệu');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadBatches();
    loadMaterials();
    lotApi.getAll({ size: 1000 }).then((r) => setLots(r.data?.content ?? [])).catch(() => {});
    loadAllComponents();
  }, [loadBatches, loadMaterials, loadAllComponents]);

  const handleAddComponent = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await batchApi.addComponent(form.batchId, form);
      setShowCreate(false);
      flash('✅ Đã thêm nguyên liệu vào lô sản xuất');
      loadAllComponents();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Thêm nguyên liệu thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleConfirmUsage = async (actualQuantity, performedBy) => {
    setActionLoading(true);
    setActionError('');
    try {
      await batchApi.confirmComponent(confirmTarget.componentId, actualQuantity, performedBy);
      setConfirmTarget(null);
      flash('✅ Đã xác nhận số lượng sử dụng');
      loadAllComponents();
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
      loadAllComponents();
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
      loadAllComponents();
    } catch (e) {
      setActionError(e.response?.data?.message ?? 'Xóa nguyên liệu thất bại');
    } finally {
      setActionLoading(false);
    }
  };

  // Filter components
  const filteredComponents = allComponents.filter((c) => {
    const matchMaterial = !materialFilter || c.materialName.toLowerCase().includes(materialFilter.toLowerCase());
    const matchBatch = !batchFilter || c.batchId === batchFilter;
    return matchMaterial && matchBatch;
  });

  // Get unique materials and batches for filter options
//   const uniqueMaterials = [...new Set(allComponents.map((c) => c.materialName))].sort();
  const uniqueBatches = [...new Set(allComponents.map((c) => c.batchId))];

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>📦 Nguyên liệu lô sản xuất (Batch Components)</h1>
          <p>Quản lý nguyên liệu của tất cả lô sản xuất</p>
        </div>
        <button
          id="btn-add-component-batch"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowCreate(true); }}
        >
          ➕ Thêm nguyên liệu
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        <div className="card">
          <div className="filter-bar">
            <div className="filter-chips">
              <input
                type="text"
                placeholder="Tìm kiếm tên nguyên liệu..."
                value={materialFilter}
                onChange={(e) => setMaterialFilter(e.target.value)}
                className="form-control"
                style={{ maxWidth: '250px' }}
                list='materialList'
              />
              <datalist id="materialList">
                {materials.sort((a, b) => a.materialName < b.materialName).map((materialObject) => (
                  <option key={materialObject.materialId} value={materialObject.materialName} />
                ))}
              </datalist>
              <select
                value={batchFilter}
                onChange={(e) => setBatchFilter(e.target.value)}
                className="form-control"
                style={{ maxWidth: '250px' }}
              >
                <option value="">-- Tất cả lô --</option>
                {uniqueBatches.map((batchId) => {
                  const batch = allComponents.find((c) => c.batchId === batchId);
                  return (
                    <option key={batchId} value={batchId}>
                      {batch?.batchNumber} ({batch?.productName})
                    </option>
                  );
                })}
              </select>
            </div>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : allComponents.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">📦</div>
              <p>Chưa có nguyên liệu nào. Nhấn <strong>Thêm nguyên liệu</strong> để bắt đầu.</p>
            </div>
          ) : filteredComponents.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🔍</div>
              <p>Không tìm thấy nguyên liệu nào khớp với bộ lọc.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Lô sản xuất</th>
                    <th>Sản phẩm</th>
                    <th>Tên nguyên liệu</th>
                    <th>Part Number</th>
                    <th>Trạng thái lô</th>
                    <th>SL kế hoạch</th>
                    <th>SL thực tế</th>
                    <th>Người thêm</th>
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredComponents.map((c) => (
                    <tr key={c.componentId}>
                      <td className="td-mono">{c.batchNumber}</td>
                      <td>
                        <div className="td-primary">{c.productName}</div>
                        <div className="text-muted">{c.productId}</div>
                      </td>
                      <td className="td-primary">{c.materialName}</td>
                      <td className="td-mono">{c.partNumber || '—'}</td>
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
                            disabled={c.batchStatus !== 'Planned'}
                            onClick={() => { setActionError(''); setModifyTarget(c); }}
                          >✏️ Sửa</button>
                          <button
                            id={`btn-delete-comp-${c.componentId}`}
                            className="btn btn-outline btn-sm"
                            disabled={c.batchStatus !== 'Planned'}
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
      </div>

      {/* Add Component Modal */}
      {showCreate && (
        <Modal title="➕ Thêm nguyên liệu vào lô sản xuất" onClose={() => setShowCreate(false)} size="modal-lg">
          <AddComponentToBatchForm
            batches={batches}
            lots={lots}
            onSubmit={handleAddComponent}
            onClose={() => setShowCreate(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Confirm Usage Modal */}
      {confirmTarget && (
        <ConfirmUsageModal
          component={confirmTarget}
          onSubmit={handleConfirmUsage}
          onClose={() => setConfirmTarget(null)}
          loading={actionLoading}
          error={actionError}
        />
      )}

      {/* Modify Component Modal */}
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

      {/* Delete Confirmation Modal */}
      {deleteTarget && (
        <DeleteConfirmationModal
          component={deleteTarget}
          onConfirm={handleDeleteComponent}
          onCancel={() => setDeleteTarget(null)}
          loading={actionLoading}
          error={actionError}
        />
      )}
    </>
  );
}
