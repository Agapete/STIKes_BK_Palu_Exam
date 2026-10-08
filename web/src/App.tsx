import React, { useState } from 'react';
import LandingPage from './LandingPage';
import LoginPage from './LoginPage';

export default function App() {
  const [page, setPage] = useState<'landing' | 'login' | 'dashboard'>('landing');
  const [activeTab, setActiveTab] = useState<'monitoring' | 'violations' | 'bank' | 'googleform' | 'users' | 'profile'>('monitoring');
  const [gformUrl, setGformUrl] = useState('');
  const [previewLoaded, setPreviewLoaded] = useState(false);
  const [alertBanner, setAlertBanner] = useState<string | null>(null);
  
  const [userList, setUserList] = useState([
    { id: '1', name: 'Budi Pratama', nim: '202400101', role: 'Mahasiswa', status: 'Aktif' },
    { id: '2', name: 'Dewi Lestari', nim: '202400102', role: 'Mahasiswa', status: 'Aktif' },
    { id: '3', name: 'Dr. Hendra Gunawan', nim: '19800512', role: 'Dosen', status: 'Aktif' },
  ]);
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [newUser, setNewUser] = useState({ name: '', nim: '', role: 'Mahasiswa' });

  const [adminProfile, setAdminProfile] = useState({
    name: 'Admin Pusat',
    email: 'admin@stikesbkpalu.ac.id',
    phone: '081234567890'
  });

  const students: StudentMonitor[] = [
    {
      id: '1',
      name: 'Budi Pratama',
      nim: '202400101',
      answered: 6,
      total: 7,
      remainingMinutes: 48,
      isOnline: true,
      violationScore: 0,
      category: 'NORMAL'
    },
    {
      id: '2',
      name: 'Dewi Lestari',
      nim: '202400102',
      answered: 5,
      total: 7,
      remainingMinutes: 45,
      isOnline: true,
      violationScore: 20,
      category: 'NORMAL'
    },
    {
      id: '3',
      name: 'Rian Saputra',
      nim: '202400103',
      answered: 2,
      total: 7,
      remainingMinutes: 52,
      isOnline: true,
      violationScore: 60,
      category: 'MENCURIGAKAN'
    },
    {
      id: '4',
      name: 'Ahmad Ridwan',
      nim: '202400104',
      answered: 7,
      total: 7,
      remainingMinutes: 30,
      isOnline: false,
      violationScore: 35,
      category: 'PERLU_DIPERIKSA'
    }
  ];

  if (page === 'landing') {
    return <LandingPage onEnterDashboard={() => setPage('login')} />;
  }

  if (page === 'login') {
    return <LoginPage onLoginSuccess={() => setPage('dashboard')} onBack={() => setPage('landing')} />;
  }

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif', minHeight: '100vh', backgroundColor: '#F8FAFC', color: '#0F172A' }}>
      {/* Header Bar */}
      <header style={{ backgroundColor: '#1E293B', color: '#FFFFFF', padding: '16px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 'bold' }}>STIKES BK Palu Exam Web Portal</h1>
          <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#94A3B8' }}>Dashboard Dosen & Administrator • UTS Algoritma & Pemrograman</p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button onClick={() => setPage('landing')} style={{ fontSize: '13px', background: 'transparent', border: '1px solid #475569', color: '#94A3B8', padding: '6px 12px', borderRadius: '8px', cursor: 'pointer' }}>← Keluar</button>
          <button onClick={() => setActiveTab('profile')} style={{ fontSize: '13px', background: '#334155', color: '#FFFFFF', border: 'none', padding: '6px 12px', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>👤</span> {adminProfile.name}
          </button>
          <span style={{ fontSize: '12px', background: '#10B981', color: '#FFFFFF', padding: '4px 8px', borderRadius: '6px', fontWeight: 'bold' }}>LIVE EXAM</span>
        </div>
      </header>

      {/* Action Notification */}
      {alertBanner && (
        <div style={{ backgroundColor: '#ECFDF5', borderLeft: '4px solid #10B981', padding: '12px 24px', color: '#065F46', display: 'flex', justifyContent: 'space-between' }}>
          <span>{alertBanner}</span>
          <button onClick={() => setAlertBanner(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', fontWeight: 'bold' }}>×</button>
        </div>
      )}

      {/* Tabs */}
      <nav style={{ backgroundColor: '#FFFFFF', borderBottom: '1px solid #E2E8F0', padding: '0 24px', display: 'flex', gap: '24px' }}>
        <button
          onClick={() => setActiveTab('monitoring')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'monitoring' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'monitoring' ? 'bold' : 'normal', color: activeTab === 'monitoring' ? '#1D4ED8' : '#64748B' }}
        >
          Live Exam Monitoring
        </button>
        <button
          onClick={() => setActiveTab('violations')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'violations' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'violations' ? 'bold' : 'normal', color: activeTab === 'violations' ? '#1D4ED8' : '#64748B' }}
        >
          Pusat Pelanggaran Anti-Cheating
        </button>
        <button
          onClick={() => setActiveTab('bank')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'bank' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'bank' ? 'bold' : 'normal', color: activeTab === 'bank' ? '#1D4ED8' : '#64748B' }}
        >
          Bank Soal
        </button>
        <button
          onClick={() => setActiveTab('googleform')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'googleform' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'googleform' ? 'bold' : 'normal', color: activeTab === 'googleform' ? '#1D4ED8' : '#64748B' }}
        >
          Import Google Form
        </button>
        <button
          onClick={() => setActiveTab('users')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'users' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'users' ? 'bold' : 'normal', color: activeTab === 'users' ? '#1D4ED8' : '#64748B' }}
        >
          Manajemen Pengguna
        </button>
      </nav>

      {/* Main Content Area */}
      <main style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
        {activeTab === 'monitoring' && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px', marginBottom: '24px' }}>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Total Peserta Terdaftar</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px' }}>42 Mahasiswa</h3>
              </div>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Sedang Online Aktif</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px', color: '#10B981' }}>40 Mahasiswa</h3>
              </div>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Perlu Diperiksa / Curang</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px', color: '#EA580C' }}>2 Mahasiswa</h3>
              </div>
            </div>

            <div style={{ background: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                <thead style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0' }}>
                  <tr>
                    <th style={{ padding: '12px 16px' }}>Mahasiswa</th>
                    <th style={{ padding: '12px 16px' }}>NIM</th>
                    <th style={{ padding: '12px 16px' }}>Progres</th>
                    <th style={{ padding: '12px 16px' }}>Sisa Waktu</th>
                    <th style={{ padding: '12px 16px' }}>Koneksi</th>
                    <th style={{ padding: '12px 16px' }}>Skor Pelanggaran</th>
                    <th style={{ padding: '12px 16px' }}>Aksi Pengawas</th>
                  </tr>
                </thead>
                <tbody>
                  {students.map((st) => (
                    <tr key={st.id} style={{ borderBottom: '1px solid #F1F5F9' }}>
                      <td style={{ padding: '14px 16px', fontWeight: 'bold' }}>{st.name}</td>
                      <td style={{ padding: '14px 16px', color: '#64748B' }}>{st.nim}</td>
                      <td style={{ padding: '14px 16px' }}>{st.answered}/{st.total} Soal</td>
                      <td style={{ padding: '14px 16px' }}>{st.remainingMinutes} Menit</td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{ color: st.isOnline ? '#10B981' : '#EF4444', fontWeight: 'bold' }}>
                          ● {st.isOnline ? 'Online' : 'Offline'}
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <span style={{
                          padding: '4px 10px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold',
                          backgroundColor: st.violationScore === 0 ? '#ECFDF5' : st.violationScore < 50 ? '#FFFBEB' : '#FFF7ED',
                          color: st.violationScore === 0 ? '#10B981' : st.violationScore < 50 ? '#D97706' : '#EA580C'
                        }}>
                          {st.category} ({st.violationScore} Pts)
                        </span>
                      </td>
                      <td style={{ padding: '14px 16px' }}>
                        <button
                          onClick={() => setAlertBanner(`Peringatan terkirim ke HP ${st.name}`)}
                          style={{ padding: '6px 10px', marginRight: '6px', borderRadius: '6px', border: '1px solid #CBD5E1', background: '#FFFFFF', cursor: 'pointer' }}
                        >
                          Peringatkan
                        </button>
                        <button
                          onClick={() => setAlertBanner(`Token ujian ${st.name} dipulihkan`)}
                          style={{ padding: '6px 10px', borderRadius: '6px', border: 'none', background: '#1D4ED8', color: '#FFFFFF', cursor: 'pointer' }}
                        >
                          Pulihkan
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'googleform' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
            <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Import Soal Otomatis dari Google Form</h2>
            <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Masukkan URL Google Form kuis Anda untuk mem-parsing pertanyaan, opsi ganda, dan kunci jawaban secara otomatis.</p>

            <div style={{ display: 'flex', gap: '12px', marginBottom: '24px' }}>
              <input
                type="text"
                value={gformUrl}
                onChange={(e) => setGformUrl(e.target.value)}
                placeholder="https://docs.google.com/forms/d/e/1FAIpQLSc..."
                style={{ flex: 1, padding: '10px 14px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '14px' }}
              />
              <button
                onClick={() => setPreviewLoaded(true)}
                style={{ padding: '10px 20px', backgroundColor: '#1D4ED8', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
              >
                Parse & Preview Soal
              </button>
            </div>

            {previewLoaded && (
              <div style={{ borderTop: '1px solid #E2E8F0', paddingTop: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <h3 style={{ margin: 0, fontSize: '16px' }}>Preview Soal yang Terdeteksi (2 Soal)</h3>
                  <button
                    onClick={() => setAlertBanner('2 Soal berhasil diimpor ke Bank Soal internal')}
                    style={{ padding: '8px 16px', backgroundColor: '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
                  >
                    Simpan Semua ke Bank Soal
                  </button>
                </div>

                <div style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '8px', marginBottom: '12px' }}>
                  <p style={{ fontWeight: 'bold', margin: '0 0 8px' }}>1. [Pilihan Ganda] Manakah protokol layer transport yang menjamin data terkirim reliable dan urut?</p>
                  <p style={{ margin: '4px 0', color: '#10B981', fontWeight: 'bold' }}>✓ B. TCP (Transmission Control Protocol) - Kunci Jawaban</p>
                  <p style={{ margin: '4px 0', color: '#64748B' }}>○ A. UDP</p>
                  <p style={{ margin: '4px 0', color: '#64748B' }}>○ C. IP</p>
                </div>

                <div style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '8px' }}>
                  <p style={{ fontWeight: 'bold', margin: '0 0 8px' }}>2. [Essay] Jelaskan fungsi subnet mask pada pengalamatan IPv4!</p>
                  <p style={{ margin: '4px 0', color: '#64748B', fontStyle: 'italic' }}>Tipe: Uraian Paragraf (Bobot: 10 Poin)</p>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'violations' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
            <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Log Integritas & Anti-Cheating Engine</h2>
            <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Riwayat lengkap insiden keamanan dan pelanggaran selama ujian berlangsung.</p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <div style={{ padding: '14px', borderRadius: '8px', borderLeft: '4px solid #EA580C', background: '#FFF7ED', display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>Rian Saputra (202400103) - APP_BACKGROUND</p>
                  <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>Aplikasi berpindah ke background selama 22 detik. Mahasiswa terindikasi membuka aplikasi lain.</p>
                </div>
                <span style={{ fontWeight: 'bold', color: '#EA580C' }}>+20 Poin</span>
              </div>

              <div style={{ padding: '14px', borderRadius: '8px', borderLeft: '4px solid #EF4444', background: '#FEF2F2', display: 'flex', justifyContent: 'space-between' }}>
                <div>
                  <p style={{ margin: 0, fontWeight: 'bold' }}>Dewi Lestari (202400102) - SCREEN_CAPTURE_ATTEMPT</p>
                  <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>Percobaan screenshot terdeteksi dan diblokir oleh FLAG_SECURE.</p>
                </div>
                <span style={{ fontWeight: 'bold', color: '#EF4444' }}>+30 Poin</span>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'bank' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
            <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Bank Soal Dosen Terpadu</h2>
            <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Daftar soal pilihan ganda dan essay yang siap dikaitkan ke jadwal ujian.</p>
            <p style={{ color: '#1D4ED8', fontWeight: 'bold' }}>• 25 Soal Pilihan Ganda Algoritma</p>
            <p style={{ color: '#1D4ED8', fontWeight: 'bold' }}>• 10 Soal Essay Struktur Data</p>
            <p style={{ color: '#1D4ED8', fontWeight: 'bold' }}>• 15 Soal Import Google Form Layer Jaringan</p>
          </div>
        )}
        {activeTab === 'users' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0', position: 'relative' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
              <div>
                <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Manajemen Pengguna (User Management)</h2>
                <p style={{ margin: 0, color: '#64748B', fontSize: '14px' }}>Daftar lengkap mahasiswa, dosen, dan admin yang terdaftar.</p>
              </div>
              <button 
                onClick={() => setShowAddUserModal(true)}
                style={{ padding: '10px 16px', background: '#1D4ED8', color: '#FFFFFF', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}
              >
                + Tambah Pengguna
              </button>
            </div>
            
            {showAddUserModal && (
              <div style={{ padding: '16px', background: '#F8FAFC', border: '1px solid #E2E8F0', borderRadius: '8px', marginBottom: '20px' }}>
                <h3 style={{ margin: '0 0 12px', fontSize: '16px' }}>Form Tambah Pengguna Baru</h3>
                <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
                  <input 
                    placeholder="Nama Lengkap" 
                    value={newUser.name}
                    onChange={(e) => setNewUser({...newUser, name: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 200px' }}
                  />
                  <input 
                    placeholder="NIM / NIP / ID" 
                    value={newUser.nim}
                    onChange={(e) => setNewUser({...newUser, nim: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 150px' }}
                  />
                  <select 
                    value={newUser.role}
                    onChange={(e) => setNewUser({...newUser, role: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 150px', backgroundColor: 'white' }}
                  >
                    <option>Mahasiswa</option>
                    <option>Dosen</option>
                    <option>Admin</option>
                  </select>
                  <button 
                    onClick={() => {
                      if (!newUser.name || !newUser.nim) return setAlertBanner('Nama dan NIM wajib diisi!');
                      setUserList([...userList, { ...newUser, id: Date.now().toString(), status: 'Aktif' }]);
                      setNewUser({ name: '', nim: '', role: 'Mahasiswa' });
                      setShowAddUserModal(false);
                      setAlertBanner('Pengguna baru berhasil ditambahkan.');
                    }}
                    style={{ padding: '8px 16px', background: '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Simpan
                  </button>
                  <button 
                    onClick={() => setShowAddUserModal(false)}
                    style={{ padding: '8px 16px', background: '#E2E8F0', color: '#475569', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
                  >
                    Batal
                  </button>
                </div>
              </div>
            )}

            <div style={{ background: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                <thead style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0' }}>
                  <tr>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Nama & ID</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Role</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Status</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  {userList.map((user, idx) => (
                    <tr key={user.id} style={{ borderBottom: idx === userList.length - 1 ? 'none' : '1px solid #E2E8F0' }}>
                      <td style={{ padding: '16px' }}>
                        <div style={{ fontWeight: 'bold' }}>{user.name}</div>
                        <div style={{ fontSize: '12px', color: '#64748B' }}>{user.nim}</div>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <span style={{ fontSize: '12px', background: user.role === 'Dosen' ? '#FEF3C7' : user.role === 'Admin' ? '#FCE7F3' : '#DBEAFE', color: user.role === 'Dosen' ? '#B45309' : user.role === 'Admin' ? '#BE185D' : '#1E40AF', padding: '4px 8px', borderRadius: '12px', fontWeight: 'bold' }}>{user.role}</span>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <span style={{ fontSize: '12px', background: user.status === 'Aktif' ? '#D1FAE5' : '#FEE2E2', color: user.status === 'Aktif' ? '#065F46' : '#991B1B', padding: '4px 8px', borderRadius: '12px', fontWeight: 'bold' }}>{user.status}</span>
                      </td>
                      <td style={{ padding: '16px', display: 'flex', gap: '8px' }}>
                        <button 
                          style={{ padding: '6px 12px', background: user.status === 'Aktif' ? '#EF4444' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            const updated = userList.map(u => u.id === user.id ? { ...u, status: u.status === 'Aktif' ? 'Nonaktif' : 'Aktif' } : u);
                            setUserList(updated);
                            setAlertBanner(`Status ${user.name} diubah menjadi ${user.status === 'Aktif' ? 'Nonaktif' : 'Aktif'}`);
                          }}
                        >
                          {user.status === 'Aktif' ? 'Nonaktifkan' : 'Aktifkan'}
                        </button>
                        <button 
                          style={{ padding: '6px 12px', background: '#F1F5F9', color: '#475569', border: '1px solid #CBD5E1', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            setUserList(userList.filter(u => u.id !== user.id));
                            setAlertBanner(`Akun ${user.name} berhasil dihapus permanen.`);
                          }}
                        >
                          Hapus
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
        {activeTab === 'profile' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0', maxWidth: '600px', margin: '0 auto' }}>
            <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Pengaturan Profil Admin</h2>
            <p style={{ margin: '0 0 24px', color: '#64748B', fontSize: '14px' }}>Kelola informasi akun Anda di STIKes BK Palu.</p>
            
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#334155' }}>Nama Lengkap</label>
                <input 
                  type="text" 
                  value={adminProfile.name}
                  onChange={(e) => setAdminProfile({...adminProfile, name: e.target.value})}
                  style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', boxSizing: 'border-box' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#334155' }}>Email</label>
                <input 
                  type="email" 
                  value={adminProfile.email}
                  onChange={(e) => setAdminProfile({...adminProfile, email: e.target.value})}
                  style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', boxSizing: 'border-box' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '8px', fontSize: '14px', fontWeight: '600', color: '#334155' }}>Nomor Telepon</label>
                <input 
                  type="text" 
                  value={adminProfile.phone}
                  onChange={(e) => setAdminProfile({...adminProfile, phone: e.target.value})}
                  style={{ width: '100%', padding: '12px', borderRadius: '8px', border: '1px solid #CBD5E1', boxSizing: 'border-box' }}
                />
              </div>

              <div style={{ marginTop: '16px', display: 'flex', gap: '12px' }}>
                <button 
                  onClick={() => setAlertBanner('Profil berhasil diperbarui!')}
                  style={{ padding: '12px 24px', background: '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold' }}
                >
                  Simpan Perubahan
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
