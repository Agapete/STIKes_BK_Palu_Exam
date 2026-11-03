import React, { useState } from 'react';
import { API_BASE, setAuthToken } from './apiConfig';

interface LoginPageProps {
  onLoginSuccess: (user: any) => void;
  onBack: () => void;
}

export default function LoginPage({ onLoginSuccess, onBack }: LoginPageProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch(`${API_BASE}/api/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
      });
      const data = await response.json();
      
      if (data.status === 'success') {
        if (data.token) {
          setAuthToken(data.token);
        }
        onLoginSuccess(data.user);
      } else {
        setError(data.message || 'Login gagal!');
      }
    } catch (err) {
      setError('Gagal menghubungi server. Pastikan Backend menyala.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'linear-gradient(135deg, #0F172A, #1E1B4B)', padding: '24px', boxSizing: 'border-box' }}>
      <div style={{ background: '#FFFFFF', padding: '40px', borderRadius: '16px', width: '100%', maxWidth: '400px', boxShadow: '0 20px 40px rgba(0,0,0,0.4)', boxSizing: 'border-box' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <img src="/logo_stikes.png" alt="Logo STIKes BK Palu" style={{ width: '80px', height: '80px', borderRadius: '12px', objectFit: 'contain', margin: '0 auto 16px' }} />
          <h2 style={{ margin: '0 0 8px', fontSize: '24px', fontWeight: 'bold', color: '#0F172A' }}>Login Admin</h2>
          <p style={{ margin: 0, color: '#64748B', fontSize: '14px' }}>Web Portal STIKES BK Palu Exam - STIKes BK Palu</p>
        </div>

        {error && (
          <div style={{ background: '#FEF2F2', color: '#DC2626', padding: '12px', borderRadius: '8px', fontSize: '13px', marginBottom: '20px', border: '1px solid #FECACA', textAlign: 'center' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#334155' }}>Username</label>
            <input 
              type="text" 
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Masukkan username admin"
              style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', boxSizing: 'border-box' }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#334155' }}>Password</label>
            <input 
              type="password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', boxSizing: 'border-box' }}
            />
          </div>

          <button 
            type="submit"
            style={{ marginTop: '8px', padding: '14px', background: 'linear-gradient(135deg, #1D4ED8, #7C3AED)', color: '#FFFFFF', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px' }}
          >
            Masuk
          </button>
        </form>

        <button 
          onClick={onBack}
          style={{ width: '100%', marginTop: '16px', padding: '12px', background: 'transparent', color: '#64748B', border: 'none', cursor: 'pointer', fontSize: '14px' }}
        >
          ← Kembali ke Halaman Depan
        </button>
      </div>
    </div>
  );
}
