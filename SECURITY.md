# Model Keamanan, Anti-Cheating & Keterbatasan Lockdown Android BYOD

Dokumen ini menjelaskan arsitektur keamanan sistem ujian online **CampusExam**, mitigasi ancaman, filosofi anti-cheating, serta penjelasan terbuka mengenai keterbatasan teknis perangkat pribadi Android (Bring Your Own Device).

---

## 1. Filosofi Anti-Cheating: Evidence & Monitoring System

Sistem anti-cheating dirancang sebagai **sistem bukti dan pemantauan (Evidence & Monitoring System)**, bukan sebagai *sistem penuduh otomatis (Automatic Accusation System)*.

### Prinsip Utama:
1. **Tidak Ada Hukuman Otomatis Sepihak**: Satu event `APP_BACKGROUND` atau `NETWORK_DISCONNECTED` tidak serta merta membuat mahasiswa didiskualifikasi. Kejadian bisa dipicu oleh panggilan telepon darurat, drop sinyal seluler, atau gangguan sistem OS.
2. **Audit Trail Lengkap**: Setiap insiden dicatat secara presisi dengan:
   - Jenis event
   - Durasi (dalam detik/milidetik)
   - Timestamp sinkronisasi server
   - Skor bobot pelanggaran
   - State ujian saat kejadian
3. **Wewenang Akhir pada Dosen/Pengawas**: Pengawas memiliki dashboard untuk memeriksa riwayat insiden dan menentukan apakah tindakan tersebut disengaja atau insidental.

---

## 2. Matriks Skoring Pelanggaran

| Jenis Event | Bobot Skor | Deskripsi & Deteksi Teknis |
|---|:---:|---|
| `APP_BACKGROUND` | +20 | Mahasiswa meminimalkan aplikasi atau menekan tombol Home/Overview. |
| `WINDOW_FOCUS_LOST` | +30 | Mahasiswa menarik notification shade, split screen, atau ada floating bubble. |
| `SCREEN_CAPTURE_ATTEMPT` | +30 | Terdeteksi aksi screenshot (walau diblokir oleh `FLAG_SECURE`). |
| `SCREEN_RECORDING_DETECTED` | +30 | Terdeteksi perekaman layar aktif melalui display manager listener. |
| `TIME_MANIPULATION` | +20 | Jam lokal perangkat diubah untuk mencoba memperpanjang timer ujian. |
| `MULTIPLE_LOGIN` | +30 | Akun login dari perangkat berbeda saat sesi ujian sedang berjalan. |
| `NETWORK_DISCONNECTED` | +5 | Koneksi jaringan terputus (diizinkan offline cache temporer). |
| `LOCK_TASK_EXIT` | +30 | Keluar dari lock-task mode jika diaktifkan. |

### Kategori Akumulasi Skor:
- **0 - 20 (NORMAL / HIJAU)**: Kejadian wajar, tidak ada indikasi kecurangan.
- **21 - 50 (PERLU DIPERIKSA / KUNING)**: Terdapat 1-2 kali perpindahan atau gangguan koneksi.
- **51 - 80 (MENCURIGAKAN / ORANYE)**: Berulang kali keluar aplikasi atau kehilangan fokus.
- **81+ (PELANGGARAN BERAT / MERAH)**: Pelanggaran intensif berkelanjutan. Pengawas dapat menghentikan sesi ujian.

---

## 3. Keterbatasan Teknis Android BYOD (Bring Your Own Device)

Sangat penting untuk memahami batas kapabilitas Android pada perangkat HP pribadi milik mahasiswa:

| Klaim Tidak Realistis | Realitas Teknis Android BYOD | Solusi & Fallback CampusExam |
|---|---|---|
| *"Mengunci 100% HP pribadi seperti kiosk MDM"* | Pada perangkat non-MDM (bukan Device Owner / Profile Owner), Android tidak mengizinkan aplikasi pihak ketiga mengunci sistem sepenuhnya tanpa intervensi pengguna. `startLockTask()` pada perangkat non-managed akan menampilkan prompt dialog konfirmasi sistem kepada pengguna. | Gunakan `startLockTask()` dengan try-catch graceful fallback. Aktifkan fullscreen immersive sticky mode + deteksi event `onWindowFocusChanged` dan `onPause`. |
| *"Mencegah mahasiswa membuka aplikasi lain"* | Sistem Android standar mengizinkan tombol Home atau Gesture Bar selalu aktif jika perangkat tidak di-root / non-MDM. | Jika mahasiswa beralih ke aplikasi lain, catat seketika durasi `APP_BACKGROUND` dan tambah poin pelanggaran +20. Tampilkan peringatan saat kembali ke aplikasi. |
| *"Mencegah penggunaan HP kedua"* | Kamera atau software di HP tidak dapat melihat jika mahasiswa membaca catatan fisik di meja atau menggunakan HP kedua di sebelahnya. | Kombinasi timer ketat per soal, randomisasi urutan soal dan opsi, serta review log aktivitas. |
| *"Memblokir screenshot 100% di semua custom ROM"* | Sebagian custom ROM atau screen capture hardware eksternal via USB Type-C DisplayPort dapat membypass batasan software tertentu. | Pasang `FLAG_SECURE` pada window Android yang membuat layar hitam pada screenshot & preview multitasking. |

---

## 4. Keamanan Server-Side (Zero-Trust Client)

1. **Timer Authoritative**: Timer berjalan di server (`start_time` dan `end_time` di database). Klien hanya menghitung selisih waktu (`remaining_time = server_end_time - current_server_time`). Perubahan jam HP mahasiswa tidak berpengaruh sama sekali.
2. **Kalkulasi Nilai di Server**: Klien tidak pernah mengirimkan skor. Klien hanya mengirimkan payload opsi jawaban terpilih (`answer_option_id` atau `essay_text`). Server yang melakukan pencocokan kunci jawaban dan perhitungan bobot nilai.
3. **Random Seed per Attempt**: Pengacakan nomor soal dan pilihan jawaban digenerate dengan seed unik yang tersimpan di `exam_attempts`. Mahasiswa yang duduk bersebelahan menerima urutan soal dan opsi yang berbeda, namun sistem dapat merekonstruksi urutan aslinya kapan saja untuk keperluan audit.
4. **Proteksi Anti-Tampering Jawaban**: Setiap jawaban ditandai dengan timestamp dan urutan revisi. Setelah ujian disubmit atau waktu habis, attempt terkunci permanen (`SUBMITTED`/`EXPIRED`).
