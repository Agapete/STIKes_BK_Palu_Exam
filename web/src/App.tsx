import React, { useState } from 'react';
import LandingPage from './LandingPage';
import LoginPage from './LoginPage';
import { API_BASE, authFetch, clearAuthToken } from './apiConfig';

export default function App() {
  const [page, setPage] = useState<'landing' | 'login' | 'dashboard'>('landing');
  const [currentUser, setCurrentUser] = useState<any>(null);
  const [activeTab, setActiveTab] = useState<'monitoring' | 'violations' | 'bank' | 'googleform' | 'users' | 'profile' | 'hasil' | 'exams'>('monitoring');
  const [examsList, setExamsList] = useState<any[]>([]);
  const [gformUrl, setGformUrl] = useState('');
  const [previewLoaded, setPreviewLoaded] = useState(false);
  const [alertBanner, setAlertBanner] = useState<string | null>(null);
  const [gformPreview, setGformPreview] = useState<any[]>([]);
  const [gformTitle, setGformTitle] = useState('');
  const [gformClass, setGformClass] = useState('');
  const [gformCourse, setGformCourse] = useState('');
  const [gformLecturer, setGformLecturer] = useState('');
  const [isLoadingGform, setIsLoadingGform] = useState(false);
  const [isSavingGform, setIsSavingGform] = useState(false);
  
  const [userList, setUserList] = useState<any[]>([]);
  const [bankList, setBankList] = useState<any[]>([]);
  
  React.useEffect(() => {
    if (page === 'dashboard' && activeTab === 'users') {
      authFetch(`${API_BASE}/api/users`)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success') {
            setUserList(data.data.map((u: any) => ({
              id: u.id,
              name: u.fullName,
              nim: u.username,
              role: u.roleId === 'LECTURER' ? 'Dosen' : (u.roleId === 'STUDENT' ? 'Mahasiswa' : 'Admin'),
              status: u.isActive ? 'Aktif' : 'Nonaktif',
              classification: u.classification,
              className: u.className || '',
              studyProgramName: u.studyProgramName || '',
              department: u.department || ''
            })));
          }
        })
        .catch(err => console.error(err));
    }
    
    if (page === 'dashboard' && activeTab === 'bank') {
      authFetch(`${API_BASE}/api/question-banks`)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success') {
            setBankList(data.data);
          }
        })
        .catch(err => console.error(err));
    }
  }, [page, activeTab]);
  const [showAddUserModal, setShowAddUserModal] = useState(false);
  const [newUser, setNewUser] = useState({ name: '', nim: '', role: 'Mahasiswa', password: '', className: 'KLP-A', studyProgram: 'D3 Keperawatan', department: '' });

  const [showEditUserModal, setShowEditUserModal] = useState(false);
  const [editingUser, setEditingUser] = useState<any>(null);
  const [isUpdatingUser, setIsUpdatingUser] = useState(false);

  const [showAddExamModal, setShowAddExamModal] = useState(false);
  const [showEditExamModal, setShowEditExamModal] = useState(false);
  const [editingExam, setEditingExam] = useState<any>(null);
  const [isUpdatingExam, setIsUpdatingExam] = useState(false);

  const [coursesList, setCoursesList] = useState<any[]>([]);
  const [lecturersList, setLecturersList] = useState<any[]>([]);
  const [isSavingExam, setIsSavingExam] = useState(false);
  const [newExam, setNewExam] = useState({ title: '', courseId: '', lecturerId: '', bankId: '', durationMinutes: '60', startTime: '', endTime: '', passingGrade: '60', className: '' });

  const [adminProfile, setAdminProfile] = useState({
    name: 'Admin Pusat',
    email: 'admin@stikesbkpalu.ac.id',
    phone: '081234567890'
  });

  const [students, setStudents] = useState<any[]>([]);
  const [violations, setViolations] = useState<any[]>([]);
  const [resultsList, setResultsList] = useState<any[]>([]);

  const handleExportCSV = () => {
    if (resultsList.length === 0) return setAlertBanner('Tidak ada data nilai untuk di-export.');
    const headers = ['Nama Mahasiswa', 'NIM/ID', 'Kelas', 'Matakuliah', 'Ujian', 'Skor Dasar', 'Skor Penalti', 'Nilai Akhir', 'Status'];
    const rows = resultsList.map(r => [
      `"${r.studentName}"`, `"${r.studentNim}"`, `"${r.className || '-'}"`, `"${r.courseName}"`, `"${r.examTitle}"`, r.baseScore, r.penalty, r.finalScore, r.gradeStatus
    ]);
    const csvContent = "data:text/csv;charset=utf-8," + [headers.join(','), ...rows.map(e => e.join(','))].join("\n");
    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "Rekap_Nilai_CampusExam.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  React.useEffect(() => {
    let interval: any;
    if (page === 'dashboard' && activeTab === 'monitoring') {
      const fetchMonitoring = () => {
        authFetch(`${API_BASE}/api/monitoring/live`)
          .then(res => res.json())
          .then(data => {
            if (data.status === 'success') setStudents(data.data);
          })
          .catch(err => console.error(err));
      };
      fetchMonitoring();
      interval = setInterval(fetchMonitoring, 3000);
    }
    return () => clearInterval(interval);
  }, [page, activeTab]);

  React.useEffect(() => {
    let interval: any;
    if (page === 'dashboard' && activeTab === 'violations') {
      const fetchViolations = () => {
        authFetch(`${API_BASE}/api/monitoring/violations`)
          .then(res => res.json())
          .then(data => {
            if (data.status === 'success') setViolations(data.data);
          })
          .catch(err => console.error(err));
      };
      fetchViolations();
      interval = setInterval(fetchViolations, 3000);
    }
    return () => clearInterval(interval);
  }, [page, activeTab]);

  React.useEffect(() => {
    if (page === 'dashboard' && activeTab === 'hasil') {
      authFetch(`${API_BASE}/api/results`)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success') setResultsList(data.data);
        })
        .catch(err => console.error(err));
    }
  }, [page, activeTab]);

  React.useEffect(() => {
    if (page === 'dashboard' && activeTab === 'exams') {
      authFetch(`${API_BASE}/api/exams`)
        .then(res => res.json())
        .then(data => {
          if (data.status === 'success') setExamsList(data.data);
        })
        .catch(err => console.error(err));
      
      authFetch(`${API_BASE}/api/question-banks`)
        .then(res => res.json())
        .then(data => { if (data.status === 'success') setBankList(data.data); })
        .catch(console.error);

      authFetch(`${API_BASE}/api/courses`)
        .then(res => res.json())
        .then(data => { if (data.status === 'success') setCoursesList(data.data); })
        .catch(console.error);

      authFetch(`${API_BASE}/api/lecturers`)
        .then(res => res.json())
        .then(data => { if (data.status === 'success') setLecturersList(data.data); })
        .catch(console.error);
    }
  }, [page, activeTab]);

  const openAddExamModal = () => {
    const now = new Date();
    const pad = (n: number) => n < 10 ? '0' + n : n;
    const nowStr = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
    const nextWeek = new Date(now.getTime() + 7 * 86400000);
    const nextWeekStr = `${nextWeek.getFullYear()}-${pad(nextWeek.getMonth() + 1)}-${pad(nextWeek.getDate())}T${pad(nextWeek.getHours())}:${pad(nextWeek.getMinutes())}`;

    setNewExam({
      title: '',
      courseId: coursesList.length > 0 ? coursesList[0].id : '',
      lecturerId: lecturersList.length > 0 ? lecturersList[0].id : '',
      bankId: bankList.length > 0 ? bankList[0].id : '',
      durationMinutes: '60',
      startTime: nowStr,
      endTime: nextWeekStr,
      passingGrade: '60',
      className: ''
    });
    setShowAddExamModal(true);
  };

  if (page === 'landing') {
    return <LandingPage onEnterDashboard={() => setPage('login')} />;
  }

  if (page === 'login') {
    return <LoginPage onLoginSuccess={(user) => { setCurrentUser(user); setPage('dashboard'); }} onBack={() => setPage('landing')} />;
  }

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif', minHeight: '100vh', backgroundColor: '#F8FAFC', color: '#0F172A' }}>
      {/* Header Bar */}
      <header style={{ backgroundColor: '#1E293B', color: '#FFFFFF', padding: '16px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ margin: 0, fontSize: '20px', fontWeight: 'bold' }}>STIKES BK Palu Exam Web Portal</h1>
          <p style={{ margin: '4px 0 0', fontSize: '12px', color: '#94A3B8' }}>Dashboard {currentUser?.role === 'ADMIN' ? 'Administrator' : 'Dosen'} • UTS Algoritma & Pemrograman</p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          <button onClick={() => { clearAuthToken(); setPage('landing'); setCurrentUser(null); }} style={{ fontSize: '13px', background: 'transparent', border: '1px solid #475569', color: '#94A3B8', padding: '6px 12px', borderRadius: '8px', cursor: 'pointer' }}>← Keluar</button>
          <button onClick={() => setActiveTab('profile')} style={{ fontSize: '13px', background: '#334155', color: '#FFFFFF', border: 'none', padding: '6px 12px', borderRadius: '8px', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span>👤</span> {currentUser?.name || adminProfile.name}
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
          onClick={() => setActiveTab('hasil')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'hasil' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'hasil' ? 'bold' : 'normal', color: activeTab === 'hasil' ? '#1D4ED8' : '#64748B' }}
        >
          Hasil & Rekap Nilai
        </button>
        
        <button
          onClick={() => setActiveTab('exams')}
          style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'exams' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'exams' ? 'bold' : 'normal', color: activeTab === 'exams' ? '#1D4ED8' : '#64748B' }}
        >
          Jadwal Ujian
        </button>
        
        {currentUser?.role === 'ADMIN' && (
          <button
            onClick={() => setActiveTab('users')}
            style={{ padding: '14px 4px', border: 'none', background: 'none', cursor: 'pointer', borderBottom: activeTab === 'users' ? '3px solid #1D4ED8' : 'none', fontWeight: activeTab === 'users' ? 'bold' : 'normal', color: activeTab === 'users' ? '#1D4ED8' : '#64748B' }}
          >
            Manajemen Pengguna
          </button>
        )}
      </nav>

      {/* Main Content Area */}
      <main style={{ padding: '24px', maxWidth: '1200px', margin: '0 auto' }}>
        {activeTab === 'monitoring' && (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '16px', marginBottom: '24px' }}>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Total Peserta Ujian</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px' }}>{students.length} Mahasiswa</h3>
              </div>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Sedang Online Aktif</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px', color: '#10B981' }}>{students.filter(s => s.isOnline).length} Mahasiswa</h3>
              </div>
              <div style={{ background: '#FFFFFF', padding: '16px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
                <p style={{ margin: 0, fontSize: '13px', color: '#64748B' }}>Terindikasi Curang (Skor &gt; 0)</p>
                <h3 style={{ margin: '8px 0 0', fontSize: '24px', color: '#EA580C' }}>{students.filter(s => s.violationScore > 0).length} Mahasiswa</h3>
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
                onClick={() => {
                  if (!gformUrl) return;
                  setIsLoadingGform(true);
                  authFetch(`${API_BASE}/api/google-forms/preview`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ url: gformUrl })
                  })
                  .then(res => res.json())
                  .then(data => {
                    setIsLoadingGform(false);
                    if (data.status === 'success') {
                      setGformTitle(data.title);
                      setGformPreview(data.questions);
                      setPreviewLoaded(true);
                    } else {
                      setAlertBanner('Error: ' + data.message);
                    }
                  })
                  .catch(err => {
                    setIsLoadingGform(false);
                    setAlertBanner('Gagal mengekstrak form. Pastikan tautan Publik.');
                  });
                }}
                disabled={isLoadingGform}
                style={{ padding: '10px 20px', backgroundColor: isLoadingGform ? '#94A3B8' : '#1D4ED8', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
              >
                {isLoadingGform ? 'Mengekstrak...' : 'Parse & Preview Soal'}
              </button>
            </div>

            {previewLoaded && (
              <div style={{ borderTop: '1px solid #E2E8F0', paddingTop: '20px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', flex: 1, marginRight: '16px' }}>
                    <h3 style={{ margin: 0, fontSize: '16px' }}>Preview: {gformTitle || 'Google Form'} ({gformPreview.length} Soal)</h3>
                    <div style={{ display: 'flex', gap: '8px' }}>
                      <input placeholder="Nama Kelas (Contoh: S1 Kep Tingkat 2)" value={gformClass} onChange={e => setGformClass(e.target.value)} style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '13px' }} />
                      <input placeholder="Mata Kuliah (Contoh: KMB 1)" value={gformCourse} onChange={e => setGformCourse(e.target.value)} style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '13px' }} />
                      <input placeholder="Dosen Pengampu" value={gformLecturer} onChange={e => setGformLecturer(e.target.value)} style={{ padding: '6px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', fontSize: '13px' }} />
                    </div>
                  </div>
                  <button
                    onClick={() => {
                      if (!gformClass || !gformCourse || !gformLecturer) return setAlertBanner('Harap lengkapi Kelas, Matakuliah, dan Dosen Pengampu');
                      setIsSavingGform(true);
                      authFetch(`${API_BASE}/api/google-forms/save`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ title: gformTitle, questions: gformPreview, className: gformClass, courseName: gformCourse, lecturerName: gformLecturer })
                      })
                      .then(res => res.json())
                      .then(data => {
                        setIsSavingGform(false);
                        if (data.status === 'success') {
                          setAlertBanner(data.message);
                          setGformUrl('');
                          setGformPreview([]);
                          setGformClass('');
                          setGformCourse('');
                          setGformLecturer('');
                          setPreviewLoaded(false);
                        } else {
                          setAlertBanner('Gagal: ' + data.message);
                        }
                      })
                      .catch(err => {
                        setIsSavingGform(false);
                        setAlertBanner('Terjadi kesalahan saat menyimpan ke database.');
                      });
                    }}
                    disabled={isSavingGform}
                    style={{ padding: '8px 16px', backgroundColor: isSavingGform ? '#94A3B8' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: isSavingGform ? 'not-allowed' : 'pointer' }}
                  >
                    {isSavingGform ? 'Menyimpan...' : 'Simpan Semua'}
                  </button>
                </div>

                <div style={{ maxHeight: '400px', overflowY: 'auto', paddingRight: '8px' }}>
                  {gformPreview.map((q, i) => (
                    <div key={i} style={{ padding: '16px', border: '1px solid #E2E8F0', borderRadius: '8px', marginBottom: '12px' }}>
                      <p style={{ fontWeight: 'bold', margin: '0 0 8px' }}>{i + 1}. {q.text}</p>
                      {q.type === 'MULTIPLE_CHOICE' ? (
                        q.options.map((opt: any, j: number) => (
                          <p key={j} style={{ margin: '4px 0', color: '#64748B' }}>○ {opt}</p>
                        ))
                      ) : (
                        <p style={{ margin: '4px 0', color: '#64748B', fontStyle: 'italic' }}>Tipe: Uraian Paragraf / Jawaban Singkat</p>
                      )}
                    </div>
                  ))}
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
              {violations.length === 0 ? (
                <p style={{ color: '#64748B', fontStyle: 'italic' }}>Belum ada pelanggaran yang terekam.</p>
              ) : (
                violations.map((v: any) => (
                  <div key={v.id} style={{ padding: '14px', borderRadius: '8px', borderLeft: `4px solid ${v.penalty > 20 ? '#EF4444' : '#EA580C'}`, background: v.penalty > 20 ? '#FEF2F2' : '#FFF7ED', display: 'flex', justifyContent: 'space-between' }}>
                    <div>
                      <p style={{ margin: 0, fontWeight: 'bold' }}>{v.studentName} ({v.studentNim}) - {v.type}</p>
                      <p style={{ margin: '4px 0 0', fontSize: '13px', color: '#64748B' }}>{v.description}</p>
                      <p style={{ margin: '4px 0 0', fontSize: '11px', color: '#94A3B8' }}>{new Date(v.timestamp).toLocaleString('id-ID')}</p>
                    </div>
                    <span style={{ fontWeight: 'bold', color: v.penalty > 20 ? '#EF4444' : '#EA580C' }}>+{v.penalty} Poin</span>
                  </div>
                ))
              )}
            </div>
          </div>
        )}

        {activeTab === 'bank' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
            <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Bank Soal Dosen Terpadu</h2>
            <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Daftar soal pilihan ganda dan essay yang siap dikaitkan ke jadwal ujian.</p>
            
            {bankList.length === 0 ? (
              <p style={{ color: '#64748B', fontStyle: 'italic' }}>Belum ada bank soal. Silakan import dari Google Form atau buat manual.</p>
            ) : (
              <ul style={{ listStyle: 'none', padding: 0 }}>
                {bankList.map((bank: any) => (
                  <li key={bank.id} style={{ padding: '12px', border: '1px solid #E2E8F0', borderRadius: '8px', marginBottom: '8px', display: 'flex', justifyContent: 'space-between' }}>
                    <div>
                      <p style={{ margin: '0 0 4px', fontWeight: 'bold', color: '#1E293B' }}>{bank.title}</p>
                      <p style={{ margin: 0, fontSize: '12px', color: '#64748B' }}>
                        Matakuliah: <strong>{bank.course?.name || 'Umum'}</strong> • 
                        Kelas: <strong>{bank.category || 'Umum'}</strong> • 
                        Dosen: <strong>{bank.lecturer?.user?.fullName || 'Anonim'}</strong> • 
                        Tgl: {new Date(bank.createdAt).toLocaleDateString('id-ID')}
                      </p>
                    </div>
                    <div style={{ background: '#EFF6FF', color: '#1D4ED8', padding: '6px 12px', borderRadius: '16px', fontWeight: 'bold', fontSize: '13px', display: 'flex', alignItems: 'center' }}>
                      {bank._count?.questions || 0} Soal
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
        {activeTab === 'hasil' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
              <div>
                <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Rekap Nilai Seluruh Mahasiswa</h2>
                <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Nilai ini sudah dikurangi secara otomatis dengan Poin Pelanggaran (Anti-Cheat).</p>
              </div>
              <button onClick={handleExportCSV} style={{ padding: '8px 16px', backgroundColor: '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}>
                ⬇️ Export ke Excel (CSV)
              </button>
            </div>
            
            <div style={{ background: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                <thead style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0' }}>
                  <tr>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Mahasiswa</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Ujian & Matakuliah</th>
                    <th style={{ padding: '16px', fontWeight: 'bold', textAlign: 'center' }}>Skor Dasar</th>
                    <th style={{ padding: '16px', fontWeight: 'bold', textAlign: 'center' }}>Penalti (Anti-Cheat)</th>
                    <th style={{ padding: '16px', fontWeight: 'bold', textAlign: 'center' }}>Nilai Akhir</th>
                  </tr>
                </thead>
                <tbody>
                  {resultsList.length === 0 ? (
                    <tr><td colSpan={5} style={{ padding: '24px', textAlign: 'center', color: '#64748B' }}>Belum ada data nilai.</td></tr>
                  ) : (
                    resultsList.map((mhs, idx, arr) => (
                      <tr key={mhs.id} style={{ borderBottom: idx === arr.length - 1 ? 'none' : '1px solid #E2E8F0' }}>
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontWeight: 'bold' }}>{mhs.studentName}</div>
                          <div style={{ fontSize: '12px', color: '#64748B' }}>{mhs.studentNim} • {mhs.className}</div>
                        </td>
                        <td style={{ padding: '16px' }}>
                          <div style={{ fontWeight: 'bold', color: '#1E293B' }}>{mhs.examTitle}</div>
                          <div style={{ fontSize: '12px', color: '#64748B' }}>{mhs.courseName} • Status: {mhs.status}</div>
                        </td>
                        <td style={{ padding: '16px', textAlign: 'center', color: '#10B981', fontWeight: 'bold' }}>{mhs.baseScore}</td>
                        <td style={{ padding: '16px', textAlign: 'center', color: '#EA580C', fontWeight: 'bold' }}>-{mhs.penalty} Poin</td>
                        <td style={{ padding: '16px', textAlign: 'center' }}>
                          <span style={{ padding: '4px 12px', borderRadius: '12px', fontWeight: 'bold', background: mhs.gradeStatus === 'LULUS' ? '#D1FAE5' : '#FEE2E2', color: mhs.gradeStatus === 'LULUS' ? '#065F46' : '#991B1B' }}>
                            {mhs.finalScore}
                          </span>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
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
                    placeholder="NIM / ID" 
                    value={newUser.nim}
                    onChange={(e) => setNewUser({...newUser, nim: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 150px' }}
                  />
                  <select 
                    value={newUser.role}
                    onChange={(e) => setNewUser({...newUser, role: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 120px', backgroundColor: 'white' }}
                  >
                    <option>Mahasiswa</option>
                    <option>Dosen</option>
                    <option>Admin</option>
                  </select>
                  
                  {newUser.role === 'Mahasiswa' && (
                    <>
                      <input 
                        placeholder="Program Studi (misal: S1 Keperawatan)" 
                        value={newUser.studyProgram}
                        onChange={(e) => setNewUser({...newUser, studyProgram: e.target.value})}
                        style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 180px' }}
                      />
                      <input 
                        placeholder="Kelas (contoh: KLP-A)" 
                        value={newUser.className}
                        onChange={(e) => setNewUser({...newUser, className: e.target.value})}
                        style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 120px' }}
                      />
                    </>
                  )}
                  {newUser.role === 'Dosen' && (
                    <input 
                      placeholder="Mata Kuliah / Departemen" 
                      value={newUser.department}
                      onChange={(e) => setNewUser({...newUser, department: e.target.value})}
                      style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 200px' }}
                    />
                  )}

                  <input 
                    placeholder="Sandi (Kosong = stikesbk123)" 
                    type="text"
                    value={newUser.password || ''}
                    onChange={(e) => setNewUser({...newUser, password: e.target.value})}
                    style={{ padding: '8px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', flex: '1 1 180px' }}
                  />
                  <button 
                    onClick={() => {
                      if (!newUser.name || !newUser.nim) return setAlertBanner('Nama dan NIM/ID wajib diisi!');
                      authFetch(`${API_BASE}/api/users`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ 
                          username: newUser.nim, 
                          fullName: newUser.name, 
                          roleId: newUser.role, 
                          password: newUser.password || 'stikesbk123', 
                          className: newUser.className, 
                          studyProgram: newUser.studyProgram,
                          department: newUser.department 
                        })
                      }).then(() => {
                        setNewUser({ name: '', nim: '', role: 'Mahasiswa', password: '', className: '', studyProgram: 'D3 Keperawatan', department: '' });
                        setShowAddUserModal(false);
                        setAlertBanner('Pengguna baru berhasil ditambahkan ke database.');
                        // Fetch fresh user list
                        authFetch(`${API_BASE}/api/users`).then(r=>r.json()).then(data=>{
                          if (data.status === 'success') {
                            setUserList(data.data.map((u: any) => ({
                              id: u.id,
                              name: u.fullName,
                              nim: u.username,
                              role: u.roleId === 'LECTURER' ? 'Dosen' : (u.roleId === 'STUDENT' ? 'Mahasiswa' : 'Admin'),
                              status: u.isActive ? 'Aktif' : 'Nonaktif',
                              password: u.passwordHash,
                              classification: u.classification
                            })));
                          }
                        });
                      });
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
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Program Studi & Kelas</th>
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
                      <td style={{ padding: '16px', color: '#475569', fontSize: '13px' }}>
                        {user.classification || '-'}
                      </td>
                      <td style={{ padding: '16px' }}>
                        <span style={{ fontSize: '12px', background: user.role === 'Dosen' ? '#FEF3C7' : user.role === 'Admin' ? '#FCE7F3' : '#DBEAFE', color: user.role === 'Dosen' ? '#B45309' : user.role === 'Admin' ? '#BE185D' : '#1E40AF', padding: '4px 8px', borderRadius: '12px', fontWeight: 'bold' }}>{user.role}</span>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <span style={{ fontSize: '12px', background: user.status === 'Aktif' ? '#D1FAE5' : '#FEE2E2', color: user.status === 'Aktif' ? '#065F46' : '#991B1B', padding: '4px 8px', borderRadius: '12px', fontWeight: 'bold' }}>{user.status}</span>
                      </td>
                      <td style={{ padding: '16px', display: 'flex', gap: '8px' }}>
                        <button 
                          style={{ padding: '6px 12px', background: '#2563EB', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            setEditingUser({
                              id: user.id,
                              name: user.name,
                              nim: user.nim,
                              role: user.role,
                              className: user.className || '',
                              studyProgram: user.studyProgramName || 'D3 Keperawatan',
                              department: user.department || ''
                            });
                            setShowEditUserModal(true);
                          }}
                        >
                          Edit
                        </button>
                        <button 
                          style={{ padding: '6px 12px', background: user.status === 'Aktif' ? '#EF4444' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            const newStatus = user.status === 'Aktif' ? false : true;
                            authFetch(`${API_BASE}/api/users/${user.id}/status`, {
                              method: 'PATCH',
                              headers: { 'Content-Type': 'application/json' },
                              body: JSON.stringify({ isActive: newStatus })
                            }).then(() => {
                              setUserList(userList.map(u => u.id === user.id ? { ...u, status: newStatus ? 'Aktif' : 'Nonaktif' } : u));
                              setAlertBanner(`Status ${user.name} diubah menjadi ${newStatus ? 'Aktif' : 'Nonaktif'} di database.`);
                            });
                          }}
                        >
                          {user.status === 'Aktif' ? 'Nonaktifkan' : 'Aktifkan'}
                        </button>
                        <button 
                          style={{ padding: '6px 12px', background: '#F1F5F9', color: '#475569', border: '1px solid #CBD5E1', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            if (window.confirm(`Yakin ingin menghapus akun ${user.name}?`)) {
                              authFetch(`${API_BASE}/api/users/${user.id}`, { method: 'DELETE' }).then(() => {
                                setUserList(userList.filter(u => u.id !== user.id));
                                setAlertBanner(`Akun ${user.name} berhasil dihapus permanen dari database.`);
                              });
                            }
                          }}
                        >
                          Hapus
                        </button>
                        <button 
                          style={{ padding: '6px 12px', background: '#E0F2FE', color: '#0369A1', border: '1px solid #BAE6FD', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            authFetch(`${API_BASE}/api/users/${user.id}/reset-password`, { method: 'POST' }).then(() => {
                              setUserList(userList.map(u => u.id === user.id ? { ...u, password: 'stikes123' } : u));
                              setAlertBanner(`Password untuk ${user.name} berhasil direset ke sandi default (stikes123) di database.`);
                            });
                          }}
                        >
                          Reset Sandi
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {showEditUserModal && editingUser && (
              <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '16px' }}>
                <div style={{ background: '#FFFFFF', borderRadius: '12px', padding: '24px', width: '100%', maxWidth: '440px', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E2E8F0', paddingBottom: '12px' }}>
                    <h3 style={{ margin: 0, fontSize: '18px', color: '#0F172A', fontWeight: 'bold' }}>Edit Data Pengguna & Kelas</h3>
                    <button onClick={() => setShowEditUserModal(false)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#64748B' }}>✕</button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Nama Lengkap</label>
                      <input 
                        value={editingUser.name} 
                        onChange={e => setEditingUser({...editingUser, name: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                      />
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>NIM / ID Pengguna</label>
                      <input 
                        value={editingUser.nim} 
                        onChange={e => setEditingUser({...editingUser, nim: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                      />
                    </div>

                    {editingUser.role === 'Mahasiswa' && (
                      <>
                        <div>
                          <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Program Studi</label>
                          <select 
                            value={editingUser.studyProgram} 
                            onChange={e => setEditingUser({...editingUser, studyProgram: e.target.value})} 
                            style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                          >
                            <option value="D3 Keperawatan">D3 Keperawatan</option>
                            <option value="S1 Administrasi Kesehatan">S1 Administrasi Kesehatan</option>
                          </select>
                        </div>

                        <div>
                          <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Kelas Mahasiswa</label>
                          <input 
                            placeholder="Contoh: KLP-A, KLP-B, atau Tingkat 2A"
                            value={editingUser.className} 
                            onChange={e => setEditingUser({...editingUser, className: e.target.value})} 
                            style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                          />
                          <div style={{ marginTop: '6px', display: 'flex', gap: '6px', alignItems: 'center', flexWrap: 'wrap' }}>
                            <span style={{ fontSize: '11px', color: '#64748B' }}>Pilihan cepat:</span>
                            {['KLP-A', 'KLP-B', 'KLP-C', 'Tingkat 1A', 'Tingkat 2A'].map(cl => (
                              <button 
                                key={cl} 
                                type="button" 
                                onClick={() => setEditingUser({...editingUser, className: cl})}
                                style={{ background: '#F1F5F9', border: '1px solid #CBD5E1', borderRadius: '4px', fontSize: '11px', padding: '2px 8px', cursor: 'pointer' }}
                              >
                                {cl}
                              </button>
                            ))}
                          </div>
                        </div>
                      </>
                    )}

                    {editingUser.role === 'Dosen' && (
                      <div>
                        <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Departemen / Bidang</label>
                        <input 
                          value={editingUser.department} 
                          onChange={e => setEditingUser({...editingUser, department: e.target.value})} 
                          style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                        />
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: '10px', marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #F1F5F9' }}>
                      <button 
                        disabled={isUpdatingUser}
                        onClick={async () => {
                          setIsUpdatingUser(true);
                          try {
                            const res = await authFetch(`${API_BASE}/api/users/${editingUser.id}`, {
                              method: 'PUT',
                              headers: { 'Content-Type': 'application/json' },
                              body: JSON.stringify({
                                fullName: editingUser.name,
                                username: editingUser.nim,
                                className: editingUser.className,
                                studyProgram: editingUser.studyProgram,
                                department: editingUser.department
                              })
                            });
                            const data = await res.json();
                            if (data.status === 'success') {
                              setShowEditUserModal(false);
                              setAlertBanner(`Data pengguna ${editingUser.name} & kelas berhasil diperbarui.`);
                              const uRes = await authFetch(`${API_BASE}/api/users`);
                              const uData = await uRes.json();
                              if (uData.status === 'success') {
                                setUserList(uData.data.map((u: any) => ({
                                  id: u.id,
                                  name: u.fullName,
                                  nim: u.username,
                                  role: u.roleId === 'LECTURER' ? 'Dosen' : (u.roleId === 'STUDENT' ? 'Mahasiswa' : 'Admin'),
                                  status: u.isActive ? 'Aktif' : 'Nonaktif',
                                  password: u.passwordHash,
                                  classification: u.classification,
                                  className: u.className || '',
                                  studyProgramName: u.studyProgramName || '',
                                  department: u.department || ''
                                })));
                              }
                            } else {
                              alert('Gagal memperbarui data: ' + (data.message || 'Terjadi kesalahan'));
                            }
                          } catch (err: any) {
                            alert('Gagal menghubungi server: ' + err.message);
                          } finally {
                            setIsUpdatingUser(false);
                          }
                        }}
                        style={{ flex: 1, padding: '10px', background: isUpdatingUser ? '#94A3B8' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: isUpdatingUser ? 'not-allowed' : 'pointer', fontWeight: 'bold' }}
                      >
                        {isUpdatingUser ? 'Menyimpan...' : 'Simpan Perubahan'}
                      </button>
                      <button 
                        disabled={isUpdatingUser}
                        onClick={() => setShowEditUserModal(false)} 
                        style={{ flex: 1, padding: '10px', background: '#F1F5F9', color: '#475569', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        Batal
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {activeTab === 'exams' && (
          <div style={{ background: '#FFFFFF', padding: '24px', borderRadius: '12px', border: '1px solid #E2E8F0', position: 'relative' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
              <div>
                <h2 style={{ margin: '0 0 8px', fontSize: '18px' }}>Jadwal & Setup Ujian</h2>
                <p style={{ margin: '0 0 20px', color: '#64748B', fontSize: '14px' }}>Atur jadwal ujian, batas waktu, KKM, dan kaitkan dengan Bank Soal.</p>
              </div>
              <button 
                onClick={openAddExamModal}
                style={{ padding: '8px 16px', backgroundColor: '#1D4ED8', color: '#FFFFFF', border: 'none', borderRadius: '8px', fontWeight: 'bold', cursor: 'pointer' }}
              >
                + Buat Jadwal Ujian
              </button>
            </div>

            {showAddExamModal && (
              <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '16px' }}>
                <div style={{ background: '#FFFFFF', borderRadius: '12px', padding: '24px', width: '100%', maxWidth: '480px', maxHeight: '90vh', overflowY: 'auto', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E2E8F0', paddingBottom: '12px' }}>
                    <h3 style={{ margin: 0, fontSize: '18px', color: '#0F172A', fontWeight: 'bold' }}>Buat Jadwal Ujian Baru</h3>
                    <button onClick={() => setShowAddExamModal(false)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#64748B' }}>✕</button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Judul Ujian *</label>
                      <input 
                        placeholder="Contoh: UTS Keperawatan Medikal Bedah" 
                        value={newExam.title} 
                        onChange={e => setNewExam({...newExam, title: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                      />
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Pilih Bank Soal (Opsional)</label>
                      <select 
                        value={newExam.bankId} 
                        onChange={e => {
                          const bId = e.target.value;
                          const selBank = bankList.find(b => b.id === bId);
                          setNewExam({
                            ...newExam,
                            bankId: bId,
                            courseId: selBank?.courseId || newExam.courseId,
                            lecturerId: selBank?.lecturerId || newExam.lecturerId,
                            title: newExam.title || (selBank ? selBank.title : '')
                          });
                        }} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px', background: '#F8FAFC' }}
                      >
                        <option value="">-- Tanpa Bank Soal (Gunakan Seluruh Soal Tersedia) --</option>
                        {bankList.map((b: any) => (
                          <option key={b.id} value={b.id}>{b.title} ({b._count?.questions || b.questions?.length || 0} Soal • {b.course?.name || 'Mata Kuliah'})</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Mata Kuliah</label>
                      <select 
                        value={newExam.courseId} 
                        onChange={e => setNewExam({...newExam, courseId: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                      >
                        <option value="">-- Pilih Mata Kuliah --</option>
                        {coursesList.map((c: any) => (
                          <option key={c.id} value={c.id}>{c.code ? `${c.code} - ` : ''}{c.name}</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Dosen Pengampu</label>
                      <select 
                        value={newExam.lecturerId} 
                        onChange={e => setNewExam({...newExam, lecturerId: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                      >
                        <option value="">-- Pilih Dosen Pengampu --</option>
                        {lecturersList.map((l: any) => (
                          <option key={l.id} value={l.id}>{l.user?.fullName || 'Dosen'} ({l.department || 'Keperawatan'})</option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Target Peserta (Kelas)</label>
                      <select 
                        value={newExam.className} 
                        onChange={e => setNewExam({...newExam, className: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                      >
                        <option value="">Semua Mahasiswa (Seluruh Kelas)</option>
                        <option value="KLP-A">Kelas KLP-A</option>
                        <option value="KLP-B">Kelas KLP-B</option>
                        <option value="KLP-C">Kelas KLP-C</option>
                      </select>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Waktu Buka Ujian</label>
                        <input 
                          type="datetime-local" 
                          value={newExam.startTime} 
                          onChange={e => setNewExam({...newExam, startTime: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Waktu Tutup Ujian</label>
                        <input 
                          type="datetime-local" 
                          value={newExam.endTime} 
                          onChange={e => setNewExam({...newExam, endTime: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Durasi (Menit)</label>
                        <input 
                          type="number" 
                          min="1"
                          placeholder="60" 
                          value={newExam.durationMinutes} 
                          onChange={e => setNewExam({...newExam, durationMinutes: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Batas Lulus (KKM)</label>
                        <input 
                          type="number" 
                          min="0"
                          max="100"
                          placeholder="60" 
                          value={newExam.passingGrade} 
                          onChange={e => setNewExam({...newExam, passingGrade: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                    </div>
                    
                    <div style={{ display: 'flex', gap: '10px', marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #F1F5F9' }}>
                      <button 
                        disabled={isSavingExam}
                        onClick={async () => {
                          if (!newExam.title || !newExam.title.trim()) {
                            alert('Mohon isi Judul Ujian terlebih dahulu!');
                            return;
                          }
                          setIsSavingExam(true);
                          try {
                            const res = await authFetch(`${API_BASE}/api/exams`, {
                              method: 'POST',
                              headers: { 'Content-Type': 'application/json' },
                              body: JSON.stringify(newExam)
                            });
                            const data = await res.json();
                            if (data.status === 'success') {
                              setShowAddExamModal(false);
                              setAlertBanner('Jadwal Ujian berhasil dibuat dan didistribusikan ke Mahasiswa.');
                              const listRes = await authFetch(`${API_BASE}/api/exams`);
                              const listData = await listRes.json();
                              if (listData.status === 'success') setExamsList(listData.data);
                            } else {
                              alert('Gagal menyimpan jadwal ujian: ' + (data.message || 'Terjadi kesalahan'));
                            }
                          } catch (err: any) {
                            alert('Gagal menghubungi server: ' + err.message);
                          } finally {
                            setIsSavingExam(false);
                          }
                        }}
                        style={{ flex: 1, padding: '10px', background: isSavingExam ? '#94A3B8' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: isSavingExam ? 'not-allowed' : 'pointer', fontWeight: 'bold' }}
                      >
                        {isSavingExam ? 'Menyimpan...' : 'Simpan Jadwal'}
                      </button>
                      <button 
                        disabled={isSavingExam}
                        onClick={() => setShowAddExamModal(false)} 
                        style={{ flex: 1, padding: '10px', background: '#F1F5F9', color: '#475569', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        Batal
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}

            <div style={{ background: '#FFFFFF', borderRadius: '12px', border: '1px solid #E2E8F0', overflow: 'hidden' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
                <thead style={{ backgroundColor: '#F8FAFC', borderBottom: '1px solid #E2E8F0' }}>
                  <tr>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Judul Ujian</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Matakuliah & Dosen</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Jadwal & Durasi</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Peserta</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Status</th>
                    <th style={{ padding: '16px', fontWeight: 'bold' }}>Aksi</th>
                  </tr>
                </thead>
                <tbody>
                  {examsList.map((exam, idx) => (
                    <tr key={exam.id} style={{ borderBottom: idx === examsList.length - 1 ? 'none' : '1px solid #E2E8F0' }}>
                      <td style={{ padding: '16px', fontWeight: 'bold', color: '#1E293B' }}>{exam.title}</td>
                      <td style={{ padding: '16px' }}>
                        <div>{exam.course?.name || '-'}</div>
                        <div style={{ fontSize: '12px', color: '#64748B' }}>{exam.lecturer?.user?.fullName || '-'}</div>
                      </td>
                      <td style={{ padding: '16px' }}>
                        <div>{new Date(exam.startTime).toLocaleString('id-ID')}</div>
                        <div style={{ fontSize: '12px', color: '#64748B' }}>{exam.durationMinutes} Menit • {exam._count?.examQuestions || 0} Soal</div>
                      </td>
                      <td style={{ padding: '16px' }}>{exam._count?.assignments || 0} Mahasiswa</td>
                      <td style={{ padding: '16px' }}>
                        <span style={{ padding: '4px 10px', background: exam.status === 'ACTIVE' ? '#D1FAE5' : '#F1F5F9', color: exam.status === 'ACTIVE' ? '#065F46' : '#475569', borderRadius: '12px', fontWeight: 'bold', fontSize: '12px' }}>
                          {exam.status}
                        </span>
                      </td>
                      <td style={{ padding: '16px', display: 'flex', gap: '8px' }}>
                        <button 
                          style={{ padding: '6px 12px', background: '#2563EB', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            const nowStr = new Date(new Date(exam.startTime).getTime() - new Date(exam.startTime).getTimezoneOffset() * 60000).toISOString().slice(0, 16);
                            const endStr = new Date(new Date(exam.endTime).getTime() - new Date(exam.endTime).getTimezoneOffset() * 60000).toISOString().slice(0, 16);
                            setEditingExam({
                              id: exam.id,
                              title: exam.title,
                              className: '',
                              status: exam.status,
                              bankId: '',
                              durationMinutes: exam.durationMinutes.toString(),
                              passingGrade: (exam.passingGrade || 60).toString(),
                              startTime: nowStr,
                              endTime: endStr
                            });
                            setShowEditExamModal(true);
                          }}
                        >
                          Edit
                        </button>
                        <button 
                          style={{ padding: '6px 12px', background: '#FEE2E2', color: '#B91C1C', border: '1px solid #FECACA', borderRadius: '6px', cursor: 'pointer', fontSize: '12px', fontWeight: 'bold' }} 
                          onClick={() => {
                            if (window.confirm(`Yakin ingin menghapus jadwal ujian "${exam.title}"?`)) {
                              authFetch(`${API_BASE}/api/exams/${exam.id}`, { method: 'DELETE' }).then(() => {
                                setExamsList(examsList.filter(e => e.id !== exam.id));
                                setAlertBanner(`Jadwal ujian "${exam.title}" berhasil dihapus.`);
                              });
                            }
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

            {showEditExamModal && editingExam && (
              <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0, 0, 0, 0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000, padding: '16px' }}>
                <div style={{ background: '#FFFFFF', borderRadius: '12px', padding: '24px', width: '100%', maxWidth: '460px', boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E2E8F0', paddingBottom: '12px' }}>
                    <h3 style={{ margin: 0, fontSize: '18px', color: '#0F172A', fontWeight: 'bold' }}>Edit Jadwal Ujian & Target Kelas</h3>
                    <button onClick={() => setShowEditExamModal(false)} style={{ background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#64748B' }}>✕</button>
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Judul Ujian</label>
                      <input 
                        value={editingExam.title} 
                        onChange={e => setEditingExam({...editingExam, title: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }} 
                      />
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Kaitkan / Perbarui Bank Soal</label>
                      <select 
                        value={editingExam.bankId || ''} 
                        onChange={e => setEditingExam({...editingExam, bankId: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px', background: '#F8FAFC' }}
                      >
                        <option value="">-- Tetap Gunakan Soal Saat Ini --</option>
                        <option value="ALL">-- Kaitkan Semua Soal di Sistem --</option>
                        {bankList.map((b: any) => (
                          <option key={b.id} value={b.id}>{b.title} ({b._count?.questions || b.questions?.length || 0} Soal • {b.course?.name || 'Mata Kuliah'})</option>
                        ))}
                      </select>
                      <div style={{ fontSize: '11px', color: '#64748B', marginTop: '4px' }}>Pilih bank jika ingin memperbarui seluruh daftar butir soal ujian ini.</div>
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Target Peserta (Kelas)</label>
                      <select 
                        value={editingExam.className} 
                        onChange={e => setEditingExam({...editingExam, className: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                      >
                        <option value="">Semua Mahasiswa (Seluruh Kelas)</option>
                        <option value="KLP-A">Kelas KLP-A</option>
                        <option value="KLP-B">Kelas KLP-B</option>
                        <option value="KLP-C">Kelas KLP-C</option>
                      </select>
                    </div>

                    <div>
                      <label style={{ fontSize: '13px', fontWeight: '600', color: '#334155', display: 'block', marginBottom: '4px' }}>Status Ujian</label>
                      <select 
                        value={editingExam.status} 
                        onChange={e => setEditingExam({...editingExam, status: e.target.value})} 
                        style={{ width: '100%', padding: '10px 12px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '14px' }}
                      >
                        <option value="ACTIVE">ACTIVE (Sedang Berlangsung)</option>
                        <option value="SCHEDULED">SCHEDULED (Terjadwal)</option>
                        <option value="ENDED">ENDED (Selesai/Ditutup)</option>
                      </select>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Waktu Buka Ujian</label>
                        <input 
                          type="datetime-local" 
                          value={editingExam.startTime} 
                          onChange={e => setEditingExam({...editingExam, startTime: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Waktu Tutup Ujian</label>
                        <input 
                          type="datetime-local" 
                          value={editingExam.endTime} 
                          onChange={e => setEditingExam({...editingExam, endTime: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>Durasi (Menit)</label>
                        <input 
                          type="number" 
                          min="1"
                          value={editingExam.durationMinutes} 
                          onChange={e => setEditingExam({...editingExam, durationMinutes: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                      <div>
                        <label style={{ fontSize: '12px', fontWeight: '600', color: '#64748B', display: 'block', marginBottom: '4px' }}>KKM (Batas Lulus)</label>
                        <input 
                          type="number" 
                          min="0"
                          max="100"
                          value={editingExam.passingGrade} 
                          onChange={e => setEditingExam({...editingExam, passingGrade: e.target.value})} 
                          style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #CBD5E1', boxSizing: 'border-box', fontSize: '13px' }} 
                        />
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '10px', marginTop: '12px', paddingTop: '12px', borderTop: '1px solid #F1F5F9' }}>
                      <button 
                        disabled={isUpdatingExam}
                        onClick={async () => {
                          setIsUpdatingExam(true);
                          try {
                            const res = await authFetch(`${API_BASE}/api/exams/${editingExam.id}`, {
                              method: 'PUT',
                              headers: { 'Content-Type': 'application/json' },
                              body: JSON.stringify(editingExam)
                            });
                            const data = await res.json();
                            if (data.status === 'success') {
                              setShowEditExamModal(false);
                              setAlertBanner(`Jadwal ujian "${editingExam.title}" berhasil diperbarui.`);
                              const eRes = await authFetch(`${API_BASE}/api/exams`);
                              const eData = await eRes.json();
                              if (eData.status === 'success') setExamsList(eData.data);
                            } else {
                              alert('Gagal memperbarui ujian: ' + (data.message || 'Terjadi kesalahan'));
                            }
                          } catch (err: any) {
                            alert('Gagal menghubungi server: ' + err.message);
                          } finally {
                            setIsUpdatingExam(false);
                          }
                        }}
                        style={{ flex: 1, padding: '10px', background: isUpdatingExam ? '#94A3B8' : '#10B981', color: '#FFFFFF', border: 'none', borderRadius: '6px', cursor: isUpdatingExam ? 'not-allowed' : 'pointer', fontWeight: 'bold' }}
                      >
                        {isUpdatingExam ? 'Menyimpan...' : 'Simpan Perubahan'}
                      </button>
                      <button 
                        disabled={isUpdatingExam}
                        onClick={() => setShowEditExamModal(false)} 
                        style={{ flex: 1, padding: '10px', background: '#F1F5F9', color: '#475569', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold' }}
                      >
                        Batal
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            )}
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
