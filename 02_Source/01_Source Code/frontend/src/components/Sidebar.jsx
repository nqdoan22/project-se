import { NavLink } from 'react-router-dom';
import keycloak from '../auth/keycloak';

const NAV_SECTIONS = [
  {
    label: 'Material Management',
    items: [
      { to: '/materials', icon: '🧪', label: 'Vật tư (Materials)' },
      { to: '/lots',      icon: '📦', label: 'Tồn kho (Lots)' },
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
      <div style={{ padding: '16px', borderTop: '1px solid var(--border)' }}>
        {keycloak.authenticated && (
          <>
            <span className="text-muted" style={{ fontSize: 13 }}>
              {keycloak.tokenParsed?.preferred_username}
            </span>
            <button className="btn btn-outline btn-sm" onClick={() => keycloak.logout()}>
              Logout
            </button>
          </>
        )}
      </div>
    </aside>
  );
}
