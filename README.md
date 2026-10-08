# STIKES BK Palu Exam - Platform Ujian Online BYOD Android & Web Dashboard

Platform Ujian Online berbasis Android untuk mahasiswa menggunakan HP pribadi (BYOD / Bring Your Own Device) dengan sistem Anti-Cheating dan Lockdown Mode, serta Web Dashboard komprehensif untuk Admin dan Dosen.

---

## 1. Arsitektur Sistem

```
┌─────────────────────────────────┐       ┌─────────────────────────────────┐
│       Android Student App       │       │    Web Admin/Lecturer Portal    │
│  (Kotlin, Compose, Room, M3)    │       │  (React, TypeScript, Tailwind)  │
│  - Lockdown Mode & FLAG_SECURE  │       │  - Real-time Live Monitoring    │
│  - AntiCheatingEngine           │       │  - Question Bank & Google Form  │
│  - Room Offline Cache & Sync    │       │  - Violation Scoring & Action   │
└────────────────┬────────────────┘       └────────────────┬────────────────┘
                 │                                         │
                 │  HTTPS / REST / JSON                    │  HTTPS / WebSocket
                 ▼                                         ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                      Backend API Server (Node.js/NestJS)                  │
│  - JWT & Refresh Token Rotation       - Role-Based Access Control (RBAC)  │
│  - Server-Side Authoritative Timer    - Real-Time Anti-Tampering Engine   │
│  - Auto-Scoring & Audit Logger        - Google Form Parser & Importer     │
└─────────────────────────────────────┬─────────────────────────────────────┘
                                      │
                                      ▼
┌───────────────────────────────────────────────────────────────────────────┐
│                      Database Layer (PostgreSQL 16)                       │
│  - Users, Students, Lecturers, Courses, Question Banks, Questions         │
│  - Exams, Attempts, Answers, Violations, Devices, Audit Logs              │
└───────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Fitur Utama

### A. Android Student App (BYOD)
- **Lockdown Mode**: Mengaktifkan `FLAG_SECURE` (mencegah screenshot & screen recording), Fullscreen Immersive Sticky mode, serta integrasi Lock Task Mode (jika didukung perangkat).
- **AntiCheatingEngine**: Deteksi real-time perpindahan aplikasi (`APP_BACKGROUND`), kehilangan fokus window (`WINDOW_FOCUS_LOST` seperti swipe notifikasi atau split-screen), percobaan screenshot, status koneksi jaringan terputus/terhubung, serta deteksi manipulasi jam perangkat.
- **Server-Side Authoritative Timer**: Perhitungan waktu ujian disinkronkan dengan server timestamp. Mahasiswa yang mengubah jam HP lokal tidak dapat menambah waktu ujian. Otomatis autosave & submit ketika waktu habis.
- **Offline Resilience & Autosave**: Menggunakan Room Database terenkripsi lokal dengan queue sinkronisasi otomatis (`SyncQueue`). Jawaban tersimpan seketika di HP dan disinkronkan saat koneksi tersedia.
- **Device Security Health Check**: Memeriksa versi Android, status Root/SuperSU/Magisk, ADB Debugging, overlay permission, dan kapabilitas lock task dengan indikator warna (Hijau/Kuning/Merah).

### B. Web Dashboard Admin & Dosen
- **Live Exam Monitoring**: Dosen memantau progres ujian secara real-time (nomor soal, sisa waktu, status koneksi Online/Offline, status keamanan, skor pelanggaran).
- **Violation Management Center**: Deteksi dan klasifikasi otomatis pelanggaran:
  - *Normal* (0-20)
  - *Perlu Diperiksa* (21-50)
  - *Mencurigakan* (51-80)
  - *Pelanggaran Berat* (81+)
  Dosen dapat meninjau detail log kejadian dan mengambil tindakan (Beri Peringatan, Pulihkan, atau Diskualifikasi).
- **Question Bank & Google Form Importer**: Mendukung soal Pilihan Ganda (A-E) dan Essay. Fitur import cerdas langsung dari Google Form URL dengan parser preview sebelum commit ke bank soal.
- **Audit Logging**: Mencatat semua operasi kritis administratif dan akademik untuk integritas data.

---

## 3. Struktur Direktori Proyek

```
.
├── app/                          # Source code Android Native (Kotlin + Jetpack Compose)
│   ├── src/main/java/com/example/
│   │   ├── core/                 # AntiCheatingEngine, LockdownManager, DeviceCheck, Timer
│   │   ├── data/                 # Room Database, DAOs, Entities, Repository, Network API
│   │   ├── presentation/         # Composables UI (Splash, Login, Dashboard, Exam, Monitor)
│   │   └── ui/theme/             # Material Design 3 Academic Design System
│   └── build.gradle.kts
├── backend/                      # Backend API (Node.js / Express / NestJS Architecture)
│   ├── src/                      # Auth, Exams, Questions, Violations, GoogleForm, Monitoring
│   ├── prisma/ or db/            # PostgreSQL schema & migrations
│   └── package.json
├── web/                          # Admin & Lecturer Dashboard (React, Vite, Tailwind CSS)
│   ├── src/components/
│   ├── src/pages/
│   └── package.json
├── docker-compose.yml            # Container orchestration (Frontend, Backend, Postgres, Nginx)
├── nginx.conf                    # Reverse proxy & SSL termination
├── README.md                     # Overview & guide
├── INSTALLATION.md               # Setup step-by-step
├── API.md                        # REST API Specification
├── DATABASE.md                   # PostgreSQL schema & ERD
├── SECURITY.md                   # Security model & BYOD limitations
├── DEPLOYMENT.md                 # Production deployment
└── TROUBLESHOOTING.md            # Troubleshooting guide
```
