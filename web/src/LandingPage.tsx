import React, { useState, useEffect } from 'react';

interface LandingPageProps {
  onEnterDashboard: () => void;
}

export default function LandingPage({ onEnterDashboard }: LandingPageProps) {
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const features = [
    {
      icon: '🔒',
      title: 'Lockdown Mode',
      desc: 'Layar terkunci penuh saat ujian berlangsung. Mencegah screenshot, screen recording, dan perpindahan aplikasi.',
      color: '#EFF6FF',
      border: '#BFDBFE',
    },
    {
      icon: '🛡️',
      title: 'Anti-Cheating Engine',
      desc: 'Deteksi real-time setiap percobaan kecurangan: keluar aplikasi, split-screen, manipulasi waktu HP, hingga koneksi terputus.',
      color: '#FFF7ED',
      border: '#FED7AA',
    },
    {
      icon: '⏱️',
      title: 'Server-Side Timer',
      desc: 'Waktu ujian dihitung oleh server, bukan HP mahasiswa. Mengubah jam perangkat tidak bisa menambah durasi ujian.',
      color: '#F0FDF4',
      border: '#BBF7D0',
    },
    {
      icon: '📶',
      title: 'Offline Resilience',
      desc: 'Jawaban tersimpan otomatis di HP. Saat koneksi kembali, semua jawaban langsung tersinkronisasi ke server.',
      color: '#FDF4FF',
      border: '#E9D5FF',
    },
    {
      icon: '📊',
      title: 'Live Exam Monitoring',
      desc: 'Dosen dapat memantau progres, status koneksi, dan skor pelanggaran setiap mahasiswa secara langsung dan real-time.',
      color: '#ECFDF5',
      border: '#A7F3D0',
    },
    {
      icon: '📝',
      title: 'Import Google Form',
      desc: 'Import bank soal langsung dari Google Form URL. Preview otomatis sebelum soal disimpan ke sistem.',
      color: '#FEF2F2',
      border: '#FECACA',
    },
  ];

  const stats = [
    { value: '10.000+', label: 'Mahasiswa Aktif' },
    { value: '500+', label: 'Ujian Dijalankan' },
    { value: '99.9%', label: 'Uptime Server' },
    { value: '0', label: 'Kebocoran Soal' },
  ];

  const steps = [
    { num: '1', title: 'Dosen Buat Soal', desc: 'Import dari Google Form atau buat langsung di Bank Soal.' },
    { num: '2', title: 'Atur Jadwal Ujian', desc: 'Tentukan waktu, durasi, dan assign mahasiswa yang mengikuti ujian.' },
    { num: '3', title: 'Mahasiswa Instal Aplikasi', desc: 'Download APK STIKES BK Palu Exam di HP masing-masing (BYOD).' },
    { num: '4', title: 'Ujian Berlangsung Aman', desc: 'Lockdown aktif, anti-cheating berjalan, timer server mengontrol waktu.' },
  ];

  return (
    <div style={{ fontFamily: 'system-ui, -apple-system, sans-serif', color: '#0F172A', overflowX: 'hidden' }}>

      {/* Sticky Navbar */}
      <nav style={{
        position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100,
        background: scrolled ? 'rgba(255,255,255,0.95)' : 'transparent',
        backdropFilter: scrolled ? 'blur(12px)' : 'none',
        borderBottom: scrolled ? '1px solid #E2E8F0' : 'none',
        padding: '16px 40px',
        display: 'flex', justifyContent: 'space-between', alignItems: 'center',
        transition: 'all 0.3s ease',
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <img src="/logo_stikes.png" alt="Logo STIKes BK Palu" style={{ width: '36px', height: '36px', objectFit: 'contain' }} />
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '16px', color: scrolled ? '#0F172A' : '#FFFFFF' }}>STIKES BK Palu Exam</div>
            <div style={{ fontSize: '10px', color: scrolled ? '#64748B' : '#CBD5E1' }}>STIKes BK Palu</div>
          </div>
        </div>
        <div style={{ display: 'flex', gap: '24px', alignItems: 'center' }}>
          {['Fitur', 'Cara Kerja', 'Statistik'].map(item => (
            <a key={item} href={`#${item.toLowerCase().replace(' ', '-')}`} style={{ color: scrolled ? '#475569' : '#CBD5E1', textDecoration: 'none', fontSize: '14px', fontWeight: '500' }}>{item}</a>
          ))}
          <button
            onClick={onEnterDashboard}
            style={{ padding: '9px 20px', background: 'linear-gradient(135deg, #1D4ED8, #7C3AED)', color: '#FFFFFF', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' }}
          >
            Masuk Dashboard →
          </button>
        </div>
      </nav>

      {/* Hero Section */}
      <section style={{
        minHeight: '100vh',
        background: 'linear-gradient(135deg, #0F172A 0%, #1E1B4B 40%, #1E3A8A 100%)',
        display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center',
        textAlign: 'center', padding: '120px 24px 80px', position: 'relative', overflow: 'hidden',
      }}>
        {/* Background decorative circles */}
        <div style={{ position: 'absolute', top: '10%', left: '-5%', width: '400px', height: '400px', borderRadius: '50%', background: 'radial-gradient(circle, rgba(99,102,241,0.15) 0%, transparent 70%)' }} />
        <div style={{ position: 'absolute', bottom: '10%', right: '-5%', width: '500px', height: '500px', borderRadius: '50%', background: 'radial-gradient(circle, rgba(29,78,216,0.2) 0%, transparent 70%)' }} />

        <div style={{ position: 'relative', maxWidth: '800px' }}>
          <div style={{ display: 'inline-block', background: 'rgba(99,102,241,0.2)', border: '1px solid rgba(99,102,241,0.4)', borderRadius: '100px', padding: '6px 16px', marginBottom: '24px' }}>
            <span style={{ color: '#A5B4FC', fontSize: '13px', fontWeight: '600' }}>🔐 Platform Ujian Online BYOD — STIKes BK Palu</span>
          </div>

          <h1 style={{ fontSize: 'clamp(36px, 6vw, 68px)', fontWeight: '800', color: '#FFFFFF', lineHeight: '1.1', margin: '0 0 24px', letterSpacing: '-1px' }}>
            Ujian Online Lebih{' '}
            <span style={{ background: 'linear-gradient(90deg, #818CF8, #60A5FA)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Aman & Terpercaya
            </span>
          </h1>

          <p style={{ fontSize: 'clamp(16px, 2vw, 20px)', color: '#94A3B8', lineHeight: '1.7', marginBottom: '40px', maxWidth: '600px', margin: '0 auto 40px' }}>
            Platform ujian berbasis Android BYOD dengan Anti-Cheating Engine, Lockdown Mode, dan Web Dashboard monitoring real-time untuk dosen dan administrator.
          </p>

          <div style={{ display: 'flex', gap: '16px', justifyContent: 'center', flexWrap: 'wrap' }}>
            <button
              onClick={onEnterDashboard}
              style={{ padding: '14px 32px', background: 'linear-gradient(135deg, #1D4ED8, #7C3AED)', color: '#FFFFFF', border: 'none', borderRadius: '12px', cursor: 'pointer', fontWeight: 'bold', fontSize: '16px', boxShadow: '0 8px 30px rgba(99,102,241,0.4)' }}
            >
              Masuk ke Dashboard →
            </button>
            <button
              style={{ padding: '14px 32px', background: 'rgba(255,255,255,0.08)', color: '#FFFFFF', border: '1px solid rgba(255,255,255,0.2)', borderRadius: '12px', cursor: 'pointer', fontWeight: '600', fontSize: '16px', backdropFilter: 'blur(4px)' }}
            >
              📱 Download APK
            </button>
          </div>

          {/* Mock device preview */}
          <div style={{ marginTop: '60px', display: 'flex', justifyContent: 'center', gap: '16px', alignItems: 'flex-end' }}>
            <div style={{ width: '180px', background: 'rgba(255,255,255,0.05)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '16px', padding: '12px', backdropFilter: 'blur(8px)' }}>
              <div style={{ background: '#1E293B', borderRadius: '8px', padding: '10px', textAlign: 'left' }}>
                <div style={{ fontSize: '9px', color: '#10B981', fontWeight: 'bold', marginBottom: '6px' }}>🟢 UJIAN AKTIF — 48:22</div>
                <div style={{ height: '6px', background: '#334155', borderRadius: '3px', marginBottom: '6px' }}><div style={{ width: '70%', height: '100%', background: '#1D4ED8', borderRadius: '3px' }} /></div>
                <div style={{ fontSize: '8px', color: '#94A3B8' }}>Soal 7 dari 10</div>
              </div>
              <div style={{ marginTop: '8px', fontSize: '8px', color: '#EF4444', textAlign: 'center', fontWeight: 'bold' }}>🔒 LOCKDOWN MODE ON</div>
            </div>
            <div style={{ width: '280px', background: 'rgba(255,255,255,0.07)', border: '1px solid rgba(255,255,255,0.12)', borderRadius: '16px', padding: '14px', backdropFilter: 'blur(8px)' }}>
              <div style={{ fontSize: '9px', color: '#94A3B8', marginBottom: '8px', fontWeight: 'bold' }}>📊 LIVE MONITORING — 12 Peserta</div>
              {['Budi P.', 'Dewi L.', 'Rian S.'].map((name, i) => (
                <div key={name} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '5px 0', borderBottom: i < 2 ? '1px solid rgba(255,255,255,0.06)' : 'none' }}>
                  <span style={{ fontSize: '9px', color: '#E2E8F0' }}>{name}</span>
                  <span style={{ fontSize: '8px', color: i === 2 ? '#EF4444' : '#10B981', fontWeight: 'bold' }}>{i === 2 ? '⚠️ 60 poin' : '✅ Normal'}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Stats Section */}
      <section id="statistik" style={{ background: '#1E293B', padding: '60px 40px' }}>
        <div style={{ maxWidth: '900px', margin: '0 auto', display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '32px', textAlign: 'center' }}>
          {stats.map(s => (
            <div key={s.label}>
              <div style={{ fontSize: 'clamp(28px, 4vw, 40px)', fontWeight: '800', color: '#818CF8' }}>{s.value}</div>
              <div style={{ fontSize: '14px', color: '#94A3B8', marginTop: '4px' }}>{s.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features Section */}
      <section id="fitur" style={{ padding: '100px 40px', background: '#F8FAFC' }}>
        <div style={{ maxWidth: '1100px', margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: '60px' }}>
            <div style={{ display: 'inline-block', background: '#EFF6FF', border: '1px solid #BFDBFE', borderRadius: '100px', padding: '6px 16px', marginBottom: '16px' }}>
              <span style={{ color: '#1D4ED8', fontSize: '13px', fontWeight: '600' }}>✦ Fitur Unggulan</span>
            </div>
            <h2 style={{ fontSize: 'clamp(28px, 4vw, 44px)', fontWeight: '800', margin: '0 0 16px' }}>Teknologi Keamanan Ujian Terdepan</h2>
            <p style={{ color: '#64748B', fontSize: '18px', maxWidth: '600px', margin: '0 auto' }}>Dirancang khusus untuk mencegah kecurangan ujian secara menyeluruh, dari sisi perangkat hingga server.</p>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '24px' }}>
            {features.map(f => (
              <div key={f.title} style={{ background: '#FFFFFF', border: `1px solid ${f.border}`, borderRadius: '16px', padding: '28px', transition: 'transform 0.2s', cursor: 'default' }}
                onMouseEnter={e => (e.currentTarget.style.transform = 'translateY(-4px)')}
                onMouseLeave={e => (e.currentTarget.style.transform = 'translateY(0)')}
              >
                <div style={{ width: '52px', height: '52px', background: f.color, borderRadius: '14px', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '26px', marginBottom: '16px' }}>{f.icon}</div>
                <h3 style={{ margin: '0 0 10px', fontWeight: 'bold', fontSize: '17px' }}>{f.title}</h3>
                <p style={{ margin: 0, color: '#64748B', fontSize: '14px', lineHeight: '1.7' }}>{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section id="cara-kerja" style={{ padding: '100px 40px', background: '#FFFFFF' }}>
        <div style={{ maxWidth: '900px', margin: '0 auto' }}>
          <div style={{ textAlign: 'center', marginBottom: '60px' }}>
            <div style={{ display: 'inline-block', background: '#F0FDF4', border: '1px solid #BBF7D0', borderRadius: '100px', padding: '6px 16px', marginBottom: '16px' }}>
              <span style={{ color: '#15803D', fontSize: '13px', fontWeight: '600' }}>✦ Cara Kerja</span>
            </div>
            <h2 style={{ fontSize: 'clamp(28px, 4vw, 44px)', fontWeight: '800', margin: '0 0 16px' }}>4 Langkah, Ujian Berjalan Aman</h2>
          </div>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '24px' }}>
            {steps.map((step, i) => (
              <div key={step.num} style={{ textAlign: 'center', position: 'relative' }}>
                {i < steps.length - 1 && (
                  <div style={{ position: 'absolute', top: '24px', left: '60%', right: '-20%', height: '2px', background: 'linear-gradient(90deg, #1D4ED8, #7C3AED)', display: 'none' }} />
                )}
                <div style={{ width: '52px', height: '52px', background: 'linear-gradient(135deg, #1D4ED8, #7C3AED)', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#FFFFFF', fontWeight: '800', fontSize: '20px', margin: '0 auto 16px' }}>{step.num}</div>
                <h3 style={{ margin: '0 0 10px', fontWeight: 'bold', fontSize: '16px' }}>{step.title}</h3>
                <p style={{ margin: 0, color: '#64748B', fontSize: '13px', lineHeight: '1.6' }}>{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section style={{ padding: '100px 40px', background: 'linear-gradient(135deg, #0F172A, #1E1B4B)', textAlign: 'center' }}>
        <div style={{ maxWidth: '600px', margin: '0 auto' }}>
          <h2 style={{ fontSize: 'clamp(28px, 4vw, 44px)', fontWeight: '800', color: '#FFFFFF', marginBottom: '16px' }}>Siap Menjalankan Ujian yang Berintegritas?</h2>
          <p style={{ color: '#94A3B8', fontSize: '18px', marginBottom: '40px' }}>Masuk ke Dashboard untuk mulai membuat soal, memantau ujian, dan mengelola pengguna STIKes BK Palu.</p>
          <button
            onClick={onEnterDashboard}
            style={{ padding: '16px 40px', background: 'linear-gradient(135deg, #1D4ED8, #7C3AED)', color: '#FFFFFF', border: 'none', borderRadius: '14px', cursor: 'pointer', fontWeight: 'bold', fontSize: '18px', boxShadow: '0 12px 40px rgba(99,102,241,0.5)' }}
          >
            Masuk ke Dashboard →
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer style={{ background: '#0F172A', padding: '40px', textAlign: 'center', borderTop: '1px solid #1E293B' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px', marginBottom: '12px' }}>
          <img src="/logo_stikes.png" alt="Logo STIKes BK Palu" style={{ width: '28px', height: '28px', objectFit: 'contain' }} />
          <span style={{ fontWeight: 'bold', color: '#FFFFFF' }}>STIKES BK Palu Exam</span>
          <span style={{ color: '#475569' }}>•</span>
          <span style={{ color: '#94A3B8', fontSize: '13px' }}>STIKes BK Palu</span>
        </div>
        <p style={{ color: '#475569', fontSize: '13px', margin: 0 }}>© 2026 STIKES BK Palu Exam. Platform Ujian Online BYOD dengan Sistem Anti-Cheating.</p>
      </footer>
    </div>
  );
}
