# Panduan Troubleshooting CampusExam

Daftar penanganan kendala umum saat pengoperasian sistem ujian online.

---

### 1. Mahasiswa Mengalami Putus Internet Saat Ujian
- **Gejala**: Muncul banner oranye "Mode Offline - Jawaban Tersimpan Lokal" pada header aplikasi Android.
- **Tindakan**: Mahasiswa tidak perlu panik dan tetap dapat memilih atau mengetik jawaban. Setiap perubahan disimpan di Room Database terenkripsi lokal dan dimasukkan ke antrean sinkronisasi (`SyncQueue`). Saat sinyal kembali, sinkronisasi otomatis berjalan tanpa kehilangan data.

### 2. Layar Hitam Saat Mengambil Screenshot
- **Penyebab**: Perilaku normal dari `FLAG_SECURE` yang diaktifkan oleh Lockdown Engine.
- **Tindakan**: Sistem mencegah perekaman dan tangkapan layar untuk melindungi kerahasiaan naskah soal.

### 3. Muncul Peringatan "Aplikasi Terbuka di Background"
- **Penyebab**: Mahasiswa menekan tombol Home, berpindah aplikasi, atau menarik panel notifikasi terlalu lama.
- **Tindakan**: AntiCheatingEngine mencatat durasi kehilangan fokus window dan menambahkan poin penalti. Mahasiswa diminta menekan tombol "Kembali ke Ujian" untuk melanjutkan.

### 4. Database Connection Pool Exhaustion
- **Gejala**: Backend merespon HTTP 500 saat ribuan mahasiswa submit bersamaan.
- **Solusi**: Konfigurasikan PgBouncer di depan PostgreSQL dan tingkatkan parameter `max_connections` pada PostgreSQL config.
