import { useState, useEffect, useCallback } from 'react';
import { materialApi } from '../services/api';
import Modal from '../components/Modal';

// Backend enum values (must match Material.MaterialType in Java)
const MATERIAL_TYPES = [
  'API',
  'EXCIPIENT',
  'DIETARY_SUPPLEMENT',
  'CONTAINER',
  'CLOSURE',
  'PROCESS_CHEMICAL',
  'TESTING_MATERIAL'
];

// Display names for enum values (user-friendly)
const MATERIAL_TYPE_DISPLAY = {
  'API': 'API',
  'EXCIPIENT': 'Excipient',
  'DIETARY_SUPPLEMENT': 'Dietary Supplement',
  'CONTAINER': 'Container',
  'CLOSURE': 'Closure',
  'PROCESS_CHEMICAL': 'Process Chemical',
  'TESTING_MATERIAL': 'Testing Material'
};

const getDisplayName = (type) => MATERIAL_TYPE_DISPLAY[type] || type;

function MaterialForm({ initial, onSubmit, onClose, loading, error }) {
  const [form, setForm] = useState({
    partNumber: '',
    materialName: '',
    materialType: 'API',
    storageConditions: '',
    specificationDocument: '',
    ...initial,
  });

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(form);
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label required">Part Number</label>
            <input
              className="form-control"
              value={form.partNumber}
              onChange={(e) => set('partNumber', e.target.value)}
              placeholder="VD: MAT-001"
              required
              id="partNumber"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Loại vật tư</label>
            <select
              className="form-control"
              value={form.materialType}
              onChange={(e) => set('materialType', e.target.value)}
              required
              id="materialType"
            >
              <option value="">Select a type</option>
              {MATERIAL_TYPES.map((t) => (
                <option key={t} value={t}>{getDisplayName(t)}</option>
              ))}
            </select>
          </div>
          <div className="form-group form-full">
            <label className="form-label required">Tên vật tư</label>
            <input
              className="form-control"
              value={form.materialName}
              onChange={(e) => set('materialName', e.target.value)}
              placeholder="Tên đầy đủ của vật tư"
              required
              id="materialName"
            />
          </div>
          <div className="form-group form-full">
            <label className="form-label">Điều kiện bảo quản</label>
            <input
              className="form-control"
              value={form.storageConditions}
              onChange={(e) => set('storageConditions', e.target.value)}
              placeholder="VD: Bảo quản 2–8°C, tránh ánh sáng"
              id="storageConditions"
            />
          </div>
          <div className="form-group form-full">
            <label className="form-label">Tài liệu đặc tính (URL/Path)</label>
            <input
              className="form-control"
              value={form.specificationDocument}
              onChange={(e) => set('specificationDocument', e.target.value)}
              placeholder="Link hoặc đường dẫn tài liệu"
              id="specificationDocument"
            />
          </div>
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="submitMaterial">
          {loading ? '⏳ Đang lưu...' : (initial ? '💾 Cập nhật' : '➕ Thêm mới')}
        </button>
      </div>
    </form>
  );
}

export default function MaterialsPage() {
  const [materials, setMaterials] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  // filters
  const [keyword, setKeyword] = useState('');
  const [typeFilter, setTypeFilter] = useState('');

  // modal states
  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadMaterials = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const params = {};
      if (keyword) params.keyword = keyword;
      if (typeFilter) params.type = typeFilter;
      const res = await materialApi.getAll(params);
      setMaterials(res.data.content ?? []);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách vật tư');
    } finally {
      setLoading(false);
    }
  }, [keyword, typeFilter]);

  useEffect(() => { loadMaterials(); }, [loadMaterials]);

  const handleCreate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await materialApi.create(form);
      setShowCreate(false);
      flash('✅ Đã thêm vật tư mới');
      loadMaterials();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Tạo thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await materialApi.update(editTarget.materialId, form);
      setEditTarget(null);
      flash('✅ Cập nhật thành công');
      loadMaterials();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Cập nhật thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDelete = async () => {
    setFormLoading(true);
    try {
      await materialApi.delete(deleteTarget.materialId);
      setDeleteTarget(null);
      flash('🗑 Đã xoá vật tư');
      loadMaterials();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Xoá thất bại');
      setDeleteTarget(null);
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>🧪 Vật tư (Materials)</h1>
          <p>Quản lý danh mục nguyên vật liệu và sản phẩm</p>
        </div>
        <button
          id="btn-add-material"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowCreate(true); }}
        >
          ＋ Thêm vật tư
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        <div className="card">
          {/* Filter bar */}
          <div className="filter-bar">
            <input
              className="form-control"
              placeholder="🔍 Tìm theo tên vật tư..."
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              id="filter-keyword"
            />
            <div className="filter-chips">
              <button
                className={`chip ${!typeFilter ? 'active' : ''}`}
                onClick={() => setTypeFilter('')}
              >Tất cả</button>
              {MATERIAL_TYPES.map((t) => (
                <button
                  key={t}
                  className={`chip ${typeFilter === t ? 'active' : ''}`}
                  onClick={() => setTypeFilter(t === typeFilter ? '' : t)}
                >{getDisplayName(t)}</button>
              ))}
            </div>
          </div>

          {/* Table */}
          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : materials.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">🧪</div>
              <p>Chưa có vật tư nào. Nhấn <strong>Thêm vật tư</strong> để bắt đầu.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Part Number</th>
                    <th>Tên vật tư</th>
                    <th>Loại</th>
                    <th>Điều kiện bảo quản</th>
                    <th>Ngày tạo</th>
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {materials.map((m) => (
                    <tr key={m.materialId}>
                      <td className="td-mono">{m.partNumber}</td>
                      <td className="td-primary">{m.materialName}</td>
                      <td><span className="type-badge">{getDisplayName(m.materialType)}</span></td>
                      <td>{m.storageConditions || <span className="text-muted">—</span>}</td>
                      <td className="text-muted">
                        {m.createdDate ? new Date(m.createdDate).toLocaleDateString('vi-VN') : '—'}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                          <button
                            id={`btn-edit-${m.materialId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => { setFormError(''); setEditTarget(m); }}
                          >✏ Sửa</button>
                          <button
                            id={`btn-delete-${m.materialId}`}
                            className="btn btn-danger btn-sm"
                            onClick={() => setDeleteTarget(m)}
                          >🗑</button>
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

      {/* Create Modal */}
      {showCreate && (
        <Modal title="➕ Thêm vật tư mới" onClose={() => setShowCreate(false)}>
          <MaterialForm
            onSubmit={handleCreate}
            onClose={() => setShowCreate(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Edit Modal */}
      {editTarget && (
        <Modal title={`✏ Sửa: ${editTarget.partNumber}`} onClose={() => setEditTarget(null)}>
          <MaterialForm
            initial={editTarget}
            onSubmit={handleUpdate}
            onClose={() => setEditTarget(null)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Delete Confirm Modal */}
      {deleteTarget && (
        <Modal
          title="⚠ Xác nhận xoá"
          onClose={() => setDeleteTarget(null)}
          footer={
            <>
              <button className="btn btn-outline" onClick={() => setDeleteTarget(null)}>Huỷ</button>
              <button
                id="btn-confirm-delete"
                className="btn btn-danger"
                onClick={handleDelete}
                disabled={formLoading}
              >
                {formLoading ? 'Đang xoá...' : '🗑 Xoá'}
              </button>
            </>
          }
        >
          <p>Bạn có chắc muốn xoá vật tư <strong>{deleteTarget.materialName}</strong> ({deleteTarget.partNumber})?</p>
          <p className="text-muted">Thao tác này không thể hoàn tác. Nếu vật tư đã có lô hàng nhập kho, việc xoá sẽ bị từ chối.</p>
        </Modal>
      )}
    </>
  );
}
