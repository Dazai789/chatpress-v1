import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function Import() {
  const nav = useNavigate();
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState('');
  const [err, setErr] = useState('');

  const submit = async (e) => {
    e.preventDefault();
    if (!file) { setErr('Please select a .md file'); return; }
    setErr('');
    const formData = new FormData();
    formData.append('file', file);
    if (title.trim()) formData.append('title', title.trim());
    try {
      const token = localStorage.getItem('token');
      await axios.post('/api/artifacts/import/markdown', formData, {
        headers: { Authorization: `Bearer ${token}` }
      });
      nav('/artifacts');
    } catch (e) {
      setErr(e.response?.data?.message || 'Import failed');
    }
  };

  return (
    <div style={{ maxWidth: 600 }}>
      <h2 style={{ marginBottom: 24 }}>Import Markdown</h2>
      {err && <p style={{ color: '#b91c1c', background: '#fff5f5', padding: 10, borderRadius: 6 }}>{err}</p>}
      <form onSubmit={submit}>
        <div style={{ marginBottom: 16 }}>
          <label style={label}>Markdown File (.md)</label>
          <input type="file" accept=".md,text/markdown" onChange={e => setFile(e.target.files[0])}
            style={inputStyle} />
        </div>
        <div style={{ marginBottom: 24 }}>
          <label style={label}>Title (optional — uses filename if empty)</label>
          <input value={title} onChange={e => setTitle(e.target.value)}
            placeholder="Optional title" maxLength={200} style={inputStyle} />
        </div>
        <div style={{ display: 'flex', gap: 12 }}>
          <button type="submit" style={btnStyle}>Import</button>
          <button type="button" onClick={() => nav(-1)}
            style={{ ...btnStyle, background: '#fff', color: '#333', border: '1px solid #c9c6bd' }}>Cancel</button>
        </div>
      </form>
    </div>
  );
}

const label = { display: 'block', marginBottom: 6, fontWeight: 650, color: '#404040', fontSize: 14 };
const inputStyle = { display: 'block', width: '100%', padding: '10px 12px', border: '1px solid #c9c6bd', borderRadius: 6, fontSize: 14, boxSizing: 'border-box' };
const btnStyle = { padding: '10px 20px', background: '#0f766e', color: '#fff', border: 'none', borderRadius: 6, fontSize: 15, cursor: 'pointer' };
