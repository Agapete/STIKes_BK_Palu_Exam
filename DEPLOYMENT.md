# Panduan Deployment CampusExam

Dokumen ini menjelaskan prosedur deployment lingkungan produksi menggunakan Docker Compose, Nginx Reverse Proxy, PostgreSQL, dan Redis.

---

## 1. Arsitektur Deployment Docker

```
[Klien Android / Web Browser]
              │
              ▼ HTTPS (443)
┌───────────────────────────────────────┐
│        Nginx Reverse Proxy & SSL      │
└──────┬─────────────────────────┬──────┘
       │ /api                    │ /
       ▼                         ▼
┌──────────────┐          ┌──────────────┐
│ Backend API  │          │ Web Frontend │
│ (Node.js/    │          │ (Vite React  │
│  NestJS :3000│          │  Nginx :80)  │
└──────┬───────┘          └──────────────┘
       │
   ┌───┴──────────────┐
   ▼                  ▼
┌──────────────┐ ┌──────────────┐
│  PostgreSQL  │ │    Redis     │
│  Database    │ │ Session/Rate │
│    :5432     │ │    :6379     │
└──────────────┘ └──────────────┘
```

---

## 2. File Konfigurasi Docker Compose (`docker-compose.yml`)

Lihat file `docker-compose.yml` pada root repository untuk konfigurasi multi-container:
- Service `postgres`: Penyimpanan data relasional dengan healthcheck dan persistent volume.
- Service `redis`: Caching dan rate limiting untuk melindungi server dari flood request.
- Service `backend`: REST API dengan hot-reload/production build, automigrasi Prisma/Kysely.
- Service `web`: Admin dan Lecturer dashboard yang disajikan secara terkompilasi via Nginx.
- Service `reverse-proxy`: Menangani terminasi SSL Let's Encrypt dan routing path.

---

## 3. Strategi Backup Database

### Backup Harian (Daily Automated Cron):
```bash
#!/bin/bash
BACKUP_DIR="/backups/campusexam/daily"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
mkdir -p $BACKUP_DIR
docker exec -t campusexam-db pg_dump -U campusexam_user -d campusexam_db | gzip > "$BACKUP_DIR/db_$TIMESTAMP.sql.gz"
find $BACKUP_DIR -type f -mtime +14 -name "*.sql.gz" -exec rm {} \;
```

### Prosedur Restore:
```bash
gunzip < /backups/campusexam/daily/db_20260907.sql.gz | docker exec -i campusexam-db psql -U campusexam_user -d campusexam_db
```
