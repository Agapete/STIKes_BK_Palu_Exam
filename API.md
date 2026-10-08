# Spesifikasi REST API CampusExam

Base URL: `https://api.campusexam.edu/api`

Semua request yang membutuhkan autentikasi harus menyertakan header:
`Authorization: Bearer <access_token>`

---

## 1. Authentication (`/auth`)

### POST `/auth/login`
Autentikasi mahasiswa, dosen, atau admin.
- **Request Body:**
```json
{
  "username": "202400101",
  "password": "PasswordMhs123!",
  "device_info": {
    "installation_id": "9f1c7d6e-...",
    "device_model": "Pixel 8",
    "android_version": "14",
    "app_version": "1.0.0"
  }
}
```
- **Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "access_token": "eyJhbGciOi...",
    "refresh_token": "def502...",
    "expires_in": 3600,
    "user": {
      "id": "usr_998811",
      "role": "STUDENT",
      "name": "Budi Pratama",
      "identifier": "202400101",
      "study_program": "Teknik Informatika"
    }
  }
}
```

### POST `/auth/refresh`
Memperbarui access token dengan refresh token rotation.
- **Request Body:** `{ "refresh_token": "..." }`
- **Response (200 OK):** `{ "access_token": "...", "refresh_token": "..." }`

### POST `/auth/logout`
Membatalkan refresh token dan mengakhiri sesi.

---

## 2. Student Endpoints (`/student`)

### GET `/student/profile`
Mengambil profil mahasiswa dan status registrasi perangkat.

### GET `/student/exams`
Daftar ujian yang ditugaskan kepada mahasiswa (berdasarkan program studi & kelas).
- **Query Params:** `?status=active|upcoming|completed`
- **Response (200 OK):** Array objek ujian dengan jadwal, durasi, dan status pengerjaan.

### GET `/student/exams/:id`
Detail ujian sebelum memulai (instruksi, durasi, total soal, status attempt).

### POST `/student/exams/:id/start`
Memulai sesi ujian (membuat record `exam_attempt` baru dengan state `IN_PROGRESS`).
- **Response:**
```json
{
  "attempt_id": "att_445566",
  "server_start_time": 1725700000,
  "server_end_time": 1725705400,
  "server_current_time": 1725700002,
  "questions": [
    {
      "id": "q_101",
      "number": 1,
      "type": "MULTIPLE_CHOICE",
      "text": "Manakah komponen utama CPU yang berfungsi melakukan perhitungan logika?",
      "options": [
        { "id": "opt_1", "label": "A", "text": "ALU (Arithmetic Logic Unit)" },
        { "id": "opt_2", "label": "B", "text": "Control Unit" },
        { "id": "opt_3", "label": "C", "text": "Register Bank" },
        { "id": "opt_4", "label": "D", "text": "Cache Memory" }
      ],
      "saved_answer": null
    }
  ]
}
```

### POST `/student/exams/:id/answer`
Autosave satu jawaban.
- **Request Body:**
```json
{
  "attempt_id": "att_445566",
  "question_id": "q_101",
  "selected_option_id": "opt_1",
  "essay_text": null,
  "is_flagged": false,
  "client_timestamp": 1725700120
}
```

### POST `/student/exams/:id/sync`
Sinkronisasi batch antrean jawaban lokal (saat koneksi offline kembali terhubung).
- **Request Body:** `{ "attempt_id": "att_445566", "answers": [...] }`

### POST `/student/exams/:id/violations`
Kirim event pelanggaran anti-cheating dari perangkat.
- **Request Body:**
```json
{
  "attempt_id": "att_445566",
  "event_type": "APP_BACKGROUND",
  "duration_ms": 3500,
  "score": 20,
  "metadata": { "reason": "User minimized app" }
}
```

### POST `/student/exams/:id/submit`
Mengumpulkan ujian secara final. Mengunci attempt ke status `SUBMITTED`.

### GET `/student/exams/:id/result`
Mengambil hasil ujian (jika konfigurasi ujian mengizinkan `show_result = true`).

---

## 3. Lecturer & Monitoring Endpoints (`/lecturer`, `/exams`)

### GET `/exams/:id/live`
Mengambil status seluruh mahasiswa yang sedang mengerjakan ujian secara langsung:
```json
[
  {
    "student_id": "std_101",
    "name": "Andi Wijaya",
    "nim": "20240101",
    "progress": "25/50",
    "remaining_seconds": 1935,
    "connection_status": "ONLINE",
    "security_status": "NORMAL",
    "violation_score": 0,
    "last_heartbeat": 1725702100
  }
]
```

### GET `/exams/:id/violations`
Mengambil daftar riwayat seluruh event pelanggaran untuk review dosen.

### POST `/google-forms/preview`
Menerima Google Form URL atau Form ID, mem-parsing struktur form, dan mengembalikan preview soal, pilihan, dan kunci jawaban.

### POST `/google-forms/import`
Menyimpan soal-soal terpilih dari Google Form ke dalam bank soal internal.
