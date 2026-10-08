# Skema Database PostgreSQL CampusExam

Sistem menggunakan database relasional PostgreSQL 16 dengan UUIDv4 sebagai primary key dan constraint foreign key terindeks.

---

## 1. Entity Relationship Overview (ERD)

```
[roles] 1 ──── ∞ [users] 1 ──── 1 [students] 1 ──── ∞ [exam_assignments]
                   │                                             │
                   └────── 1 [lecturers] 1 ──── ∞ [exams] 1 ──── ┘
                                                      │
                                                      ├──── ∞ [exam_questions]
                                                      │              │
                                                      │              ▼
                                                      │      [questions] 1 ──── ∞ [question_options]
                                                      │              ▲
                                                      │              │
                                                      │      [question_banks]
                                                      │
                                                      ├──── ∞ [exam_attempts] 1 ──── ∞ [answers]
                                                      │              │
                                                      │              ├──── ∞ [violations]
                                                      │              └──── 1 [exam_results]
                                                      ▼
                                              [audit_logs]
```

---

## 2. DDL PostgreSQL Schema

```sql
-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Roles
CREATE TABLE roles (
    id VARCHAR(30) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT
);

INSERT INTO roles (id, name) VALUES ('ADMIN', 'Administrator'), ('LECTURER', 'Dosen Pengampu'), ('STUDENT', 'Mahasiswa');

-- 2. Users
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    role_id VARCHAR(30) NOT NULL REFERENCES roles(id),
    username VARCHAR(100) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 3. Study Programs & Courses
CREATE TABLE study_programs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(150) NOT NULL,
    faculty VARCHAR(150) NOT NULL
);

CREATE TABLE courses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    study_program_id UUID NOT NULL REFERENCES study_programs(id),
    code VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    semester INT NOT NULL
);

-- 4. Students & Lecturers
CREATE TABLE students (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nim VARCHAR(50) UNIQUE NOT NULL,
    study_program_id UUID NOT NULL REFERENCES study_programs(id),
    class_name VARCHAR(50) NOT NULL,
    generation_year INT NOT NULL
);

CREATE TABLE lecturers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    nip VARCHAR(50) UNIQUE NOT NULL,
    department VARCHAR(150) NOT NULL
);

-- 5. Devices
CREATE TABLE devices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id UUID NOT NULL REFERENCES students(id),
    installation_id VARCHAR(255) NOT NULL,
    device_model VARCHAR(150),
    android_version VARCHAR(50),
    app_version VARCHAR(50),
    is_trusted BOOLEAN DEFAULT TRUE,
    first_seen TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    last_seen TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(student_id, installation_id)
);

-- 6. Question Banks & Questions
CREATE TABLE question_banks (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    lecturer_id UUID NOT NULL REFERENCES lecturers(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    title VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_bank_id UUID REFERENCES question_banks(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL CHECK (type IN ('MULTIPLE_CHOICE', 'ESSAY')),
    question_text TEXT NOT NULL,
    difficulty VARCHAR(20) DEFAULT 'MEDIUM' CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    score_weight NUMERIC(5,2) DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE question_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    label VARCHAR(10) NOT NULL, -- A, B, C, D, E
    option_text TEXT NOT NULL,
    is_correct BOOLEAN DEFAULT FALSE
);

-- 7. Exams & Assignments
CREATE TABLE exams (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    course_id UUID NOT NULL REFERENCES courses(id),
    lecturer_id UUID NOT NULL REFERENCES lecturers(id),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INT NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    passing_grade NUMERIC(5,2) DEFAULT 60.0,
    random_questions BOOLEAN DEFAULT TRUE,
    random_options BOOLEAN DEFAULT TRUE,
    allow_back_navigation BOOLEAN DEFAULT TRUE,
    show_result BOOLEAN DEFAULT TRUE,
    anti_cheating_enabled BOOLEAN DEFAULT TRUE,
    status VARCHAR(30) DEFAULT 'SCHEDULED' CHECK (status IN ('DRAFT', 'SCHEDULED', 'ACTIVE', 'ENDED', 'ARCHIVED')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE exam_questions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    order_index INT NOT NULL,
    UNIQUE(exam_id, question_id)
);

CREATE TABLE exam_assignments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exam_id UUID NOT NULL REFERENCES exams(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    max_attempts INT DEFAULT 1,
    UNIQUE(exam_id, student_id)
);

-- 8. Attempts, Answers & Violations
CREATE TABLE exam_attempts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    exam_id UUID NOT NULL REFERENCES exams(id),
    student_id UUID NOT NULL REFERENCES students(id),
    device_id UUID REFERENCES devices(id),
    state VARCHAR(30) DEFAULT 'NOT_STARTED' CHECK (state IN ('NOT_STARTED', 'IN_PROGRESS', 'SUBMITTED', 'AUTO_SUBMITTED', 'EXPIRED', 'CANCELLED')),
    started_at TIMESTAMP WITH TIME ZONE,
    submitted_at TIMESTAMP WITH TIME ZONE,
    random_seed BIGINT NOT NULL,
    violation_score INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE answers (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    attempt_id UUID NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id),
    selected_option_id UUID REFERENCES question_options(id),
    essay_text TEXT,
    is_flagged BOOLEAN DEFAULT FALSE,
    manual_score NUMERIC(5,2),
    saved_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    UNIQUE(attempt_id, question_id)
);

CREATE TABLE violations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    attempt_id UUID NOT NULL REFERENCES exam_attempts(id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    score_increment INT NOT NULL,
    duration_ms BIGINT DEFAULT 0,
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    actor_id UUID REFERENCES users(id),
    actor_role VARCHAR(30) NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_type VARCHAR(50),
    target_id VARCHAR(100),
    ip_address VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Indexes for High Performance
CREATE INDEX idx_exams_status_time ON exams(status, start_time, end_time);
CREATE INDEX idx_exam_attempts_student ON exam_attempts(student_id, exam_id);
CREATE INDEX idx_answers_attempt ON answers(attempt_id);
CREATE INDEX idx_violations_attempt ON violations(attempt_id, created_at DESC);
CREATE INDEX idx_audit_logs_actor ON audit_logs(actor_id, created_at DESC);
```
