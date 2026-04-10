import { useState, useEffect, useCallback } from 'react';
import { userApi } from '../services/api';
import Modal from '../components/Modal';

const ROLES = ['Admin', 'InventoryManager', 'QualityControl', 'Production', 'Viewer'];

function RoleBadge({ role }) {
  const color =
    role === 'Admin'            ? 'var(--danger)'  :
    role === 'InventoryManager' ? 'var(--primary)' :
    role === 'QualityControl'   ? 'var(--warning)' :
    role === 'Production'       ? 'var(--success)' : 'var(--muted)';
  const bg =
    role === 'Admin'            ? 'var(--danger-bg)'   :
    role === 'InventoryManager' ? 'var(--primary-light)' :
    role === 'QualityControl'   ? 'var(--warning-bg)'  :
    role === 'Production'       ? 'var(--success-bg)'  : 'var(--muted-bg)';
  return (
    <span className="badge" style={{ background: bg, color }}>
      {role}
    </span>
  );
}

function ActiveBadge({ isActive }) {
  return (
    <span className={`badge ${isActive ? 'badge-active' : 'badge-inactive'}`}>
      <span className="badge-dot" />
      {isActive ? 'Đang hoạt động' : 'Đã vô hiệu hoá'}
    </span>
  );
}

function UserForm({ initial, onSubmit, onClose, loading, error }) {
  const [form, setForm] = useState({
    username: '',
    email: '',
    role: 'Viewer',
    isActive: true,
    password: '',
    ...initial,
  });
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const isEdit = !!initial;

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSubmit(form); }}>
      <div className="modal-body">
        {error && <div className="alert alert-error">⚠ {error}</div>}
        <div className="form-grid">
          <div className="form-group">
            <label className="form-label required">Tên đăng nhập</label>
            <input
              className="form-control"
              value={form.username}
              onChange={(e) => set('username', e.target.value)}
              placeholder="VD: jdoe"
              required
              id="user-username"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Email</label>
            <input
              className="form-control"
              type="email"
              value={form.email}
              onChange={(e) => set('email', e.target.value)}
              placeholder="VD: jdoe@company.com"
              required
              id="user-email"
            />
          </div>
          <div className="form-group">
            <label className="form-label required">Vai trò</label>
            <select
              className="form-control"
              value={form.role}
              onChange={(e) => set('role', e.target.value)}
              required
              id="user-role"
            >
              {ROLES.map((r) => <option key={r}>{r}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Trạng thái</label>
            <select
              className="form-control"
              value={form.isActive ? 'true' : 'false'}
              onChange={(e) => set('isActive', e.target.value === 'true')}
              id="user-isActive"
            >
              <option value="true">Đang hoạt động</option>
              <option value="false">Vô hiệu hoá</option>
            </select>
          </div>
          {!isEdit && (
            <div className="form-group form-full">
              <label className="form-label required">Mật khẩu khởi tạo</label>
              <input
                className="form-control"
                type="password"
                value={form.password}
                onChange={(e) => set('password', e.target.value)}
                placeholder="Nhập mật khẩu"
                required
                id="user-password"
              />
            </div>
          )}
        </div>
      </div>
      <div className="modal-footer">
        <button type="button" className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
        <button type="submit" className="btn btn-primary" disabled={loading} id="btn-submit-user">
          {loading ? '⏳ Đang lưu...' : (isEdit ? '💾 Cập nhật' : '➕ Tạo người dùng')}
        </button>
      </div>
    </form>
  );
}

function ChangePasswordModal({ user, onSubmit, onClose, loading, error }) {
  const [newPassword, setNewPassword] = useState('');

  return (
    <Modal
      title={`🔑 Đổi mật khẩu: ${user.username}`}
      onClose={onClose}
      footer={
        <>
          <button className="btn btn-outline" onClick={onClose} disabled={loading}>Huỷ</button>
          <button
            id="btn-confirm-password"
            className="btn btn-primary"
            disabled={loading || !newPassword}
            onClick={() => onSubmit(newPassword)}
          >
            {loading ? 'Đang đổi...' : '🔑 Đổi mật khẩu'}
          </button>
        </>
      }
    >
      {error && <div className="alert alert-error">⚠ {error}</div>}
      <p style={{ marginBottom: 12 }} className="text-muted">
        Đặt mật khẩu mới cho người dùng <strong>{user.username}</strong>.
        Admin đặt mật khẩu không cần xác thực mật khẩu cũ.
      </p>
      <div className="form-group">
        <label className="form-label required">Mật khẩu mới</label>
        <input
          className="form-control"
          type="password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          placeholder="Nhập mật khẩu mới"
          id="new-password"
        />
      </div>
    </Modal>
  );
}

export default function UsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [fetchError, setFetchError] = useState('');

  const [roleFilter, setRoleFilter] = useState('');
  const [activeFilter, setActiveFilter] = useState('');

  const [showCreate, setShowCreate] = useState(false);
  const [editTarget, setEditTarget] = useState(null);
  const [pwdTarget, setPwdTarget] = useState(null);
  const [deactivateTarget, setDeactivateTarget] = useState(null);
  const [formLoading, setFormLoading] = useState(false);
  const [formError, setFormError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const flash = (msg) => { setSuccessMsg(msg); setTimeout(() => setSuccessMsg(''), 3000); };

  const loadUsers = useCallback(async () => {
    setLoading(true);
    setFetchError('');
    try {
      const params = {};
      if (roleFilter) params.role = roleFilter;
      if (activeFilter !== '') params.isActive = activeFilter;
      const res = await userApi.getAll(params);
      setUsers(res.data.content ?? []);
    } catch (e) {
      setFetchError(e.response?.data?.message ?? 'Không thể tải danh sách người dùng');
    } finally {
      setLoading(false);
    }
  }, [roleFilter, activeFilter]);

  useEffect(() => { loadUsers(); }, [loadUsers]);

  const handleCreate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      await userApi.create(form);
      setShowCreate(false);
      flash('✅ Đã tạo người dùng mới');
      loadUsers();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Tạo người dùng thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleUpdate = async (form) => {
    setFormLoading(true);
    setFormError('');
    try {
      const { _password, ...data } = form;
      await userApi.update(editTarget.userId, data);
      setEditTarget(null);
      flash('✅ Cập nhật người dùng thành công');
      loadUsers();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Cập nhật thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleChangePassword = async (newPassword) => {
    setFormLoading(true);
    setFormError('');
    try {
      await userApi.changePassword(pwdTarget.userId, newPassword);
      setPwdTarget(null);
      flash('✅ Đã đổi mật khẩu thành công');
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Đổi mật khẩu thất bại');
    } finally {
      setFormLoading(false);
    }
  };

  const handleDeactivate = async () => {
    setFormLoading(true);
    try {
      await userApi.deactivate(deactivateTarget.userId);
      setDeactivateTarget(null);
      flash('🗑 Đã vô hiệu hoá người dùng');
      loadUsers();
    } catch (e) {
      setFormError(e.response?.data?.message ?? 'Vô hiệu hoá thất bại');
      setDeactivateTarget(null);
    } finally {
      setFormLoading(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <div className="page-header-left">
          <h1>👥 Người dùng (Users)</h1>
          <p>Quản lý tài khoản và phân quyền người dùng</p>
        </div>
        <button
          id="btn-create-user"
          className="btn btn-primary"
          onClick={() => { setFormError(''); setShowCreate(true); }}
        >
          ➕ Tạo người dùng
        </button>
      </div>

      <div className="page-body">
        {successMsg && <div className="alert alert-success" style={{ marginBottom: 16 }}>{successMsg}</div>}

        <div className="card">
          <div className="filter-bar">
            <div className="filter-chips">
              <button className={`chip ${!roleFilter ? 'active' : ''}`} onClick={() => setRoleFilter('')}>
                Tất cả vai trò
              </button>
              {ROLES.map((r) => (
                <button
                  key={r}
                  className={`chip ${roleFilter === r ? 'active' : ''}`}
                  onClick={() => setRoleFilter(r === roleFilter ? '' : r)}
                >{r}</button>
              ))}
            </div>
            <div className="filter-chips" style={{ marginLeft: 'auto' }}>
              <button
                className={`chip ${activeFilter === '' ? 'active' : ''}`}
                onClick={() => setActiveFilter('')}
              >Tất cả</button>
              <button
                className={`chip ${activeFilter === 'true' ? 'active' : ''}`}
                onClick={() => setActiveFilter(activeFilter === 'true' ? '' : 'true')}
              >Đang hoạt động</button>
              <button
                className={`chip ${activeFilter === 'false' ? 'active' : ''}`}
                onClick={() => setActiveFilter(activeFilter === 'false' ? '' : 'false')}
              >Vô hiệu hoá</button>
            </div>
          </div>

          {loading ? (
            <div className="loading-center"><div className="spinner" /></div>
          ) : fetchError ? (
            <div style={{ padding: 20 }}><div className="alert alert-error">{fetchError}</div></div>
          ) : users.length === 0 ? (
            <div className="empty-state">
              <div className="empty-icon">👥</div>
              <p>Không tìm thấy người dùng nào. Nhấn <strong>Tạo người dùng</strong> để bắt đầu.</p>
            </div>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Tên đăng nhập</th>
                    <th>Email</th>
                    <th>Vai trò</th>
                    <th>Trạng thái</th>
                    <th>Đăng nhập lần cuối</th>
                    <th>Ngày tạo</th>
                    <th style={{ textAlign: 'right' }}>Hành động</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.userId}>
                      <td className="td-primary">{u.username}</td>
                      <td className="td-mono">{u.email}</td>
                      <td><RoleBadge role={u.role} /></td>
                      <td><ActiveBadge isActive={u.isActive} /></td>
                      <td className="text-muted">
                        {u.lastLogin ? new Date(u.lastLogin).toLocaleString('vi-VN') : 'Chưa đăng nhập'}
                      </td>
                      <td className="text-muted">
                        {u.createdDate ? new Date(u.createdDate).toLocaleDateString('vi-VN') : '—'}
                      </td>
                      <td>
                        <div style={{ display: 'flex', gap: 6, justifyContent: 'flex-end' }}>
                          <button
                            id={`btn-edit-user-${u.userId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => { setFormError(''); setEditTarget(u); }}
                          >✏ Sửa</button>
                          <button
                            id={`btn-pwd-user-${u.userId}`}
                            className="btn btn-outline btn-sm"
                            onClick={() => { setFormError(''); setPwdTarget(u); }}
                          >🔑</button>
                          <button
                            id={`btn-deactivate-user-${u.userId}`}
                            className="btn btn-danger btn-sm"
                            onClick={() => setDeactivateTarget(u)}
                            disabled={!u.isActive}
                            title={u.isActive ? 'Vô hiệu hoá' : 'Đã vô hiệu hoá'}
                          >🚫</button>
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
        <Modal title="➕ Tạo người dùng mới" onClose={() => setShowCreate(false)}>
          <UserForm
            onSubmit={handleCreate}
            onClose={() => setShowCreate(false)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Edit Modal */}
      {editTarget && (
        <Modal title={`✏ Sửa người dùng: ${editTarget.username}`} onClose={() => setEditTarget(null)}>
          <UserForm
            initial={editTarget}
            onSubmit={handleUpdate}
            onClose={() => setEditTarget(null)}
            loading={formLoading}
            error={formError}
          />
        </Modal>
      )}

      {/* Change Password Modal */}
      {pwdTarget && (
        <ChangePasswordModal
          user={pwdTarget}
          onSubmit={handleChangePassword}
          onClose={() => setPwdTarget(null)}
          loading={formLoading}
          error={formError}
        />
      )}

      {/* Deactivate Confirm Modal */}
      {deactivateTarget && (
        <Modal
          title="⚠ Xác nhận vô hiệu hoá"
          onClose={() => setDeactivateTarget(null)}
          footer={
            <>
              <button className="btn btn-outline" onClick={() => setDeactivateTarget(null)}>Huỷ</button>
              <button
                id="btn-confirm-deactivate"
                className="btn btn-danger"
                onClick={handleDeactivate}
                disabled={formLoading}
              >
                {formLoading ? 'Đang xử lý...' : '🚫 Vô hiệu hoá'}
              </button>
            </>
          }
        >
          <p>Bạn có chắc muốn vô hiệu hoá tài khoản <strong>{deactivateTarget.username}</strong>?</p>
          <p className="text-muted">Người dùng sẽ không thể đăng nhập, nhưng dữ liệu vẫn được giữ lại.</p>
        </Modal>
      )}
    </>
  );
}
