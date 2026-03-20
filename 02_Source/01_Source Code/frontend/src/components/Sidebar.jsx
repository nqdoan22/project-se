import { NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const NAV_SECTIONS = [
  {
    label: 'Material Management',
    items: [
      { to: '/materials', icon: '🧪', label: 'Vật tư (Materials)' },
      { to: '/lots',      icon: '📦', label: 'Tồn kho (Lots)' },
      { to: '/transactions', icon: '📋', label: 'Giao dịch (Transactions)' },
    ],
  },
  {
    label: 'Quality Control',
    items: [
      { to: '/qctests', icon: '🔬', label: 'Kiểm nghiệm (QC Tests)' },
    ],
  },
  {
    label: 'Production',
    items: [
      { to: '/batches',   icon: '⚗️', label: 'Lô sản xuất (Batches)' },
      { to: '/components', icon: '📦', label: 'Nguyên liệu (Components)' },
    ],
  },
  {
    label: 'Labels & Reports',
    items: [
      { to: '/labels',    icon: '🏷️', label: 'Nhãn (Labels)' },
      { to: '/dashboard', icon: '📊', label: 'Báo cáo (Reports)' },
    ],
  },
  {
    label: 'Administration',
    items: [
      { to: '/users', icon: '👥', label: 'Người dùng (Users)' },
    ],
  },
];

export default function Sidebar() {
  const { authenticated, username, roles, logout } = useAuth();

  const displayRole = roles.find((r) => r !== 'default-roles-ims' && r !== 'offline_access' && r !== 'uma_authorization') || '';

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <h2>IMS</h2>
        <span>Inventory System</span>
      </div>
      <nav className="sidebar-nav">
        {NAV_SECTIONS.map((section) => (
          <div key={section.label}>
            <div className="nav-section-label">{section.label}</div>
            {section.items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
              >
                <span className="nav-icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </div>
        ))}
      </nav>
      <div className="sidebar-profile">
        {authenticated ? (
          <>
            <div className="sidebar-profile-info">
              <div className="sidebar-avatar">
                {username.charAt(0).toUpperCase()}
              </div>
              <div className="sidebar-profile-text">
                <span className="sidebar-username">{username}</span>
                {displayRole && <span className="sidebar-role">{displayRole}</span>}
              </div>
            </div>
            <button className="btn btn-outline btn-sm sidebar-logout" onClick={logout}>
              Logout
            </button>
          </>
        ) : (
          <span className="sidebar-role">Not connected</span>
        )}
      </div>
    </aside>
  );
}
