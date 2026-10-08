# Panduan Instalasi & Persiapan Lingkungan

Panduan langkah-demi-langkah menjalankan CampusExam di server kampus atau laptop pengembang.

---

## 1. Persyaratan Sistem
- **Docker Engine** >= 24.0 & **Docker Compose** >= 2.20
- **Node.js** >= 20 LTS (untuk pengembangan lokal)
- **JDK 17** & **Android Studio Hedgehog / Iguana / Jellyfish**
- **Port Tersedia**: 80, 443, 3000, 5432, 6379

---

## 2. Langkah Instalasi Cepat (Docker)

1. Salin file environment:
   ```bash
   cp .env.example .env
   ```
2. Sesuaikan secret key (JWT_SECRET, DATABASE_PASSWORD, GOOGLE_CLIENT_ID).
3. Jalankan container:
   ```bash
   docker compose up -d --build
   ```
4. Verifikasi status container:
   ```bash
   docker compose ps
   ```
5. Buka Web Dashboard di browser: `http://localhost`
   - Akun default Admin: `admin` / `AdminSecret2026!`
   - Akun default Dosen: `dosen01` / `DosenPass123!`

---

## 3. Menjalankan Aplikasi Android Mahasiswa

1. Buka root proyek ini di Android Studio.
2. Tunggu Gradle sync selesai.
3. Jalankan pada emulator atau hubungkan HP Android fisik (Android 8.0 Oreo s.d. Android 14/15).
4. Gunakan akun mahasiswa demo:
   - NIM: `2024001`
   - Password: `PasswordMhs123!`
