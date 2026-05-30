import { Outlet, Link, useNavigate } from 'react-router-dom';

export default function Layout() {
  const nav = useNavigate();
  const user = localStorage.getItem('username');

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    nav('/login');
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: 20 }}>
      <header style={{
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        borderBottom: '2px solid #e0e0e0', paddingBottom: 16, marginBottom: 24
      }}>
        <nav style={{ display: 'flex', gap: 20, alignItems: 'center' }}>
          <Link to="/artifacts" style={linkStyle}>Articles</Link>
          <Link to="/artifacts/new" style={linkStyle}>New</Link>
          <Link to="/artifacts/import" style={linkStyle}>Import</Link>
          <Link to="/logs" style={linkStyle}>Logs</Link>
        </nav>
        <div>
          <span style={{ color: '#666', marginRight: 12 }}>{user}</span>
          <button onClick={logout} style={btnStyle}>Logout</button>
        </div>
      </header>
      <Outlet />
    </div>
  );
}

const linkStyle = {
  textDecoration: 'none', color: '#0f766e', fontSize: 16, fontWeight: 600,
  padding: '6px 12px', borderRadius: 6,
};

const btnStyle = {
  padding: '6px 14px', border: '1px solid #c9c6bd', borderRadius: 6,
  background: '#fff', cursor: 'pointer', fontSize: 14
};
