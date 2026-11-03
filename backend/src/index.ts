import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';

// Handle BigInt JSON serialization error
(BigInt.prototype as any).toJSON = function () {
  return this.toString();
};

const prisma = new PrismaClient();
const app = express();
const port = process.env.PORT || 3000;

// JWT Secret — use environment variable in production
const JWT_SECRET = process.env.JWT_SECRET || 'campusexam_jwt_secret_stikes_bk_palu_2024';
const BCRYPT_SALT_ROUNDS = 10;

app.use(helmet({
  hsts: false,
  contentSecurityPolicy: false
}));
app.use(cors());
app.use(express.json());

// ==========================================
// SECURITY MIDDLEWARE: JWT Authentication
// ==========================================
interface AuthRequest extends Request {
  user?: { id: string; role: string; username: string };
}

function authenticateToken(req: AuthRequest, res: Response, next: NextFunction) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Bearer TOKEN

  if (!token) {
    return res.status(401).json({ status: 'error', message: 'Token autentikasi diperlukan.' });
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET) as { id: string; role: string; username: string };
    req.user = decoded;
    next();
  } catch (err) {
    return res.status(403).json({ status: 'error', message: 'Token tidak valid atau sudah kadaluarsa.' });
  }
}

function requireRole(allowedRoles: string[]) {
  return (req: AuthRequest, res: Response, next: NextFunction) => {
    if (!req.user || !allowedRoles.includes(req.user.role)) {
      return res.status(403).json({ status: 'error', message: 'Anda tidak memiliki akses untuk fitur ini.' });
    }
    next();
  };
}

// ==========================================
// SEEDER: Initialize mock data if DB is empty
// ==========================================
async function seedDatabase() {
  const roleCount = await prisma.role.count();
  if (roleCount === 0) {
    console.log('Seeding initial data to SQLite...');
    
    // 1. Roles
    await prisma.role.create({ data: { id: 'ADMIN', name: 'Administrator' } });
    await prisma.role.create({ data: { id: 'LECTURER', name: 'Dosen Pengampu' } });
    await prisma.role.create({ data: { id: 'STUDENT', name: 'Mahasiswa' } });

    // 2. Users (Admin, Lecturer, Student) — passwords hashed with bcrypt
    const adminHash = await bcrypt.hash('admin123', BCRYPT_SALT_ROUNDS);
    const lecturerHash = await bcrypt.hash('stikes123', BCRYPT_SALT_ROUNDS);
    const studentHash = await bcrypt.hash('stikesbk123', BCRYPT_SALT_ROUNDS);

    await prisma.user.create({
      data: {
        roleId: 'ADMIN',
        username: 'admin',
        email: 'admin@stikes.ac.id',
        passwordHash: adminHash,
        fullName: 'Admin Pusat',
      }
    });

    const sp = await prisma.studyProgram.create({
      data: { code: 'D3KEP', name: 'D3 Keperawatan', faculty: 'Keperawatan' }
    });
    await prisma.studyProgram.create({
      data: { code: 'S1ADMKES', name: 'S1 Administrasi Kesehatan', faculty: 'Ilmu Kesehatan' }
    });

    const lecUser = await prisma.user.create({
      data: {
        roleId: 'LECTURER',
        username: '19800512',
        email: 'hendra@stikes.ac.id',
        passwordHash: lecturerHash,
        fullName: 'Ns. Hendra Gunawan, M.Kep.',
        lecturer: { create: { nip: '19800512', department: 'Keperawatan' } }
      },
      include: { lecturer: true }
    });

    const stdUser = await prisma.user.create({
      data: {
        roleId: 'STUDENT',
        username: '202400101',
        email: 'budi@stikes.ac.id',
        passwordHash: studentHash,
        fullName: 'Budi Pratama',
        student: { create: { nim: '202400101', studyProgramId: sp.id, className: 'KLP-A', generationYear: 2024 } }
      },
      include: { student: true }
    });

    // 3. Course & Exam
    const course = await prisma.course.create({
      data: { studyProgramId: sp.id, code: 'KEP101', name: 'Keperawatan Dasar & Medikal', semester: 1 }
    });

    const exam = await prisma.exam.create({
      data: {
        courseId: course.id,
        lecturerId: lecUser.lecturer!.id,
        title: 'Ujian Tengah Semester',
        description: 'UTS Keperawatan Dasar',
        durationMinutes: 60,
        startTime: new Date(Date.now() - 3600000), // Started 1 hour ago
        endTime: new Date(Date.now() + 86400000 * 7), // Ends in 7 days
        status: 'ACTIVE'
      }
    });

    // 4. Assign exam to student
    await prisma.examAssignment.create({
      data: { examId: exam.id, studentId: stdUser.student!.id }
    });

    console.log('Seeding complete! Database is ready to use.');
  } else {
    // Migration for existing database: ensure D3 Keperawatan & S1 Administrasi Kesehatan exist
    let spD3 = await prisma.studyProgram.findFirst({ where: { name: 'D3 Keperawatan' } });
    if (!spD3) {
      spD3 = await prisma.studyProgram.create({
        data: { code: 'D3KEP', name: 'D3 Keperawatan', faculty: 'Keperawatan' }
      });
    }

    let spS1 = await prisma.studyProgram.findFirst({ where: { name: 'S1 Administrasi Kesehatan' } });
    if (!spS1) {
      spS1 = await prisma.studyProgram.create({
        data: { code: 'S1ADMKES', name: 'S1 Administrasi Kesehatan', faculty: 'Ilmu Kesehatan' }
      });
    }

    // Replace Teknik Informatika references
    const spTI = await prisma.studyProgram.findFirst({ where: { name: 'Teknik Informatika' } });
    if (spTI) {
      await prisma.student.updateMany({
        where: { studyProgramId: spTI.id },
        data: { studyProgramId: spD3.id, className: 'KLP-A' }
      });
      await prisma.course.updateMany({
        where: { studyProgramId: spTI.id },
        data: { studyProgramId: spD3.id }
      });
      await prisma.studyProgram.delete({ where: { id: spTI.id } }).catch(() => {});
    }

    // Rename any lingering IT courses to Health courses
    await prisma.course.updateMany({
      where: { name: { contains: 'Algoritma' } },
      data: { name: 'Keperawatan Medikal Bedah (KMB)', code: 'KEP101', studyProgramId: spD3.id }
    });

    // Update IT lecturer profile to Health/Nursing
    const itLec = await prisma.lecturer.findFirst({
      where: { department: 'Informatika' },
      include: { user: true }
    });
    if (itLec) {
      await prisma.lecturer.update({
        where: { id: itLec.id },
        data: { department: 'Keperawatan' }
      });
      await prisma.user.update({
        where: { id: itLec.userId },
        data: { fullName: 'Ns. Hendra Gunawan, M.Kep.' }
      });
    }

    // Ensure default lecturer exists
    let defaultLec = await prisma.lecturer.findFirst({ include: { user: true } });
    if (!defaultLec) {
      let lecUser = await prisma.user.findFirst({ where: { roleId: 'LECTURER' } });
      if (!lecUser) {
        const hashedPw = await bcrypt.hash('stikes123', BCRYPT_SALT_ROUNDS);
        lecUser = await prisma.user.create({
          data: {
            roleId: 'LECTURER',
            username: '19800512',
            email: 'hendra@stikes.ac.id',
            passwordHash: hashedPw,
            fullName: 'Ns. Hendra Gunawan, M.Kep.',
          }
        });
      }
      defaultLec = await prisma.lecturer.create({
        data: { userId: lecUser.id, nip: '19800512', department: 'Keperawatan' },
        include: { user: true }
      });
    }

    // Ensure default courses exist
    let courseD3 = await prisma.course.findFirst({ where: { studyProgramId: spD3.id } });
    if (!courseD3) {
      courseD3 = await prisma.course.create({
        data: {
          studyProgramId: spD3.id,
          code: 'KEP101',
          name: 'Keperawatan Medikal Bedah (KMB)',
          semester: 3
        }
      });
    }

    let courseS1 = await prisma.course.findFirst({ where: { studyProgramId: spS1.id } });
    if (!courseS1) {
      courseS1 = await prisma.course.create({
        data: {
          studyProgramId: spS1.id,
          code: 'ADM201',
          name: 'Administrasi & Manajemen Rumah Sakit',
          semester: 3
        }
      });
    }

    // Ensure at least one QuestionBank exists
    const bankCount = await prisma.questionBank.count();
    if (bankCount === 0 && defaultLec && courseD3) {
      const sampleBank = await prisma.questionBank.create({
        data: {
          title: 'Bank Soal Keperawatan Medikal Bedah',
          lecturerId: defaultLec.id,
          courseId: courseD3.id,
          category: 'KMB'
        }
      });
      await prisma.question.create({
        data: {
          questionBankId: sampleBank.id,
          type: 'MULTIPLE_CHOICE',
          questionText: 'Tindakan keperawatan prioritas pertama pada pasien yang mengalami sesak napas akut adalah...',
          options: {
            create: [
              { label: 'A', optionText: 'Memberikan posisi semi Fowler dan terapi oksigen', isCorrect: true },
              { label: 'B', optionText: 'Memberikan kompres hangat pada dahi', isCorrect: false },
              { label: 'C', optionText: 'Menganjurkan pasien untuk banyak minum air es', isCorrect: false },
              { label: 'D', optionText: 'Melakukan fisioterapi dada secara agresif', isCorrect: false }
            ]
          }
        }
      });
      await prisma.question.create({
        data: {
          questionBankId: sampleBank.id,
          type: 'MULTIPLE_CHOICE',
          questionText: 'Pencegahan infeksi nosokomial di ruang rawat inap paling efektif dilakukan dengan cara...',
          options: {
            create: [
              { label: 'A', optionText: 'Cuci tangan 6 langkah (hand hygiene) sesuai standar WHO', isCorrect: true },
              { label: 'B', optionText: 'Membuka seluruh jendela ruangan', isCorrect: false },
              { label: 'C', optionText: 'Membatasi asupan nutrisi pasien', isCorrect: false },
              { label: 'D', optionText: 'Menggunakan sarung tangan tanpa menggantinya', isCorrect: false }
            ]
          }
        }
      });
    }

    // SECURITY: Migrate plaintext passwords to bcrypt hashes
    const allUsers = await prisma.user.findMany();
    for (const u of allUsers) {
      // bcrypt hashes start with '$2b$' or '$2a$' — if not, it's plaintext
      if (!u.passwordHash.startsWith('$2b$') && !u.passwordHash.startsWith('$2a$')) {
        const hashed = await bcrypt.hash(u.passwordHash, BCRYPT_SALT_ROUNDS);
        await prisma.user.update({ where: { id: u.id }, data: { passwordHash: hashed } });
        console.log(`[Security] Migrated password for user: ${u.username}`);
      }
    }
  }
}

// ==========================================
// API ENDPOINTS
// ==========================================

// 1. Healthcheck
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'UP', service: 'campusexam-api', database: 'connected', timestamp: Date.now() });
});

// 2. Authentication (Real DB Check + bcrypt + JWT)
app.post('/api/auth/login', async (req: Request, res: Response) => {
  const { username, password } = req.body;
  try {
    const user = await prisma.user.findUnique({
      where: { username },
      include: { 
        role: true,
        student: { include: { studyProgram: true } }
      }
    });

    if (!user) {
      return res.status(401).json({ status: 'error', message: 'Username atau password salah!' });
    }

    // Secure password comparison using bcrypt
    const passwordValid = await bcrypt.compare(password, user.passwordHash);
    if (!passwordValid) {
      return res.status(401).json({ status: 'error', message: 'Username atau password salah!' });
    }

    // Generate real signed JWT token
    const token = jwt.sign(
      { id: user.id, role: user.roleId, username: user.username },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    res.json({
      status: 'success',
      token,
      user: { 
        id: user.id, 
        username: user.username, 
        role: user.roleId, 
        name: user.fullName,
        studyProgram: user.student?.studyProgram?.name || 'D3 Keperawatan',
        className: user.student?.className || 'KLP-A'
      }
    });
  } catch (error) {
    res.status(500).json({ status: 'error', message: 'Internal server error' });
  }
});

app.post('/api/auth/register', async (req: Request, res: Response) => {
  try {
    const { username, fullName, className, studyProgram } = req.body;
    
    // Check if user already exists
    const existing = await prisma.user.findUnique({ where: { username } });
    if (existing) {
      return res.status(400).json({ status: 'error', message: 'NIM tersebut sudah terdaftar.' });
    }

    // Create user with hashed password
    const hashedPassword = await bcrypt.hash('stikesbk123', BCRYPT_SALT_ROUNDS);
    const user = await prisma.user.create({
      data: {
        username,
        fullName,
        roleId: 'STUDENT',
        passwordHash: hashedPassword, // default password (hashed)
        email: `${username}@stikes.ac.id`,
        isActive: true
      }
    });

    // Find or dynamically create StudyProgram
    let sp = await prisma.studyProgram.findFirst({ where: { name: studyProgram } });
    if (!sp && studyProgram) {
      sp = await prisma.studyProgram.create({
        data: {
          code: studyProgram.substring(0, 4).toUpperCase() + Date.now().toString().slice(-4),
          name: studyProgram,
          faculty: 'Kesehatan'
        }
      });
    } else if (!sp) {
      sp = await prisma.studyProgram.findFirst();
    }

    // Create Student
    if (sp) {
      const newStudent = await prisma.student.create({
        data: {
          userId: user.id,
          nim: username,
          studyProgramId: sp.id,
          className: className || 'Umum',
          generationYear: new Date().getFullYear()
        }
      });

      // Auto-assign any active exams to this new student
      const activeExams = await prisma.exam.findMany({ where: { status: 'ACTIVE' } });
      for (const ex of activeExams) {
        await prisma.examAssignment.create({
          data: { examId: ex.id, studentId: newStudent.id }
        }).catch(() => {});
      }
    }

    res.json({
      status: 'success',
      message: 'Pendaftaran berhasil. Silakan login.'
    });
  } catch (error) {
    res.status(500).json({ status: 'error', message: 'Internal server error' });
  }
});

// ==========================================
// STUDENT ANDROID APP ENDPOINTS (JWT Protected)
// ==========================================

const getStudentFromUserId = async (userId?: string) => {
  if (!userId) {
    return null; // No fallback — require authenticated user
  }
  const byUser = await prisma.student.findUnique({ where: { userId } }).catch(() => null);
  if (byUser) return byUser;
  return await prisma.student.findUnique({ where: { id: userId } }).catch(() => null);
};

// 2.1 Get Available Exams for Student
app.get('/api/student/exams', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const userId = req.user!.id;
    const student = await getStudentFromUserId(userId);
    if (!student) return res.status(401).json({ status: 'error', message: 'Not a student' });

    let assignments = await prisma.examAssignment.findMany({
      where: { studentId: student.id },
      include: {
        exam: {
          include: { 
            course: true, 
            lecturer: { include: { user: true } },
            _count: { select: { examQuestions: true } }
          }
        }
      }
    });

    // Fallback: If no assignment found for this student, auto-assign active exams
    if (assignments.length === 0) {
      const allActiveExams = await prisma.exam.findMany({
        where: { status: 'ACTIVE' },
        include: { 
          course: true, 
          lecturer: { include: { user: true } },
          _count: { select: { examQuestions: true } }
        }
      });
      for (const ex of allActiveExams) {
        await prisma.examAssignment.create({
          data: { examId: ex.id, studentId: student.id }
        }).catch(() => {});
      }
      assignments = await prisma.examAssignment.findMany({
        where: { studentId: student.id },
        include: {
          exam: {
            include: { 
              course: true, 
              lecturer: { include: { user: true } },
              _count: { select: { examQuestions: true } }
            }
          }
        }
      });
    }

    const studentExams = assignments.map(a => ({
      id: a.exam.id,
      title: a.exam.title,
      course: a.exam.course.name,
      lecturer: a.exam.lecturer.user.fullName,
      durationMinutes: a.exam.durationMinutes,
      startTime: a.exam.startTime,
      endTime: a.exam.endTime,
      status: a.exam.status,
      questionCount: a.exam._count?.examQuestions || 0
    }));

    res.json({ status: 'success', data: studentExams });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 2.2 Start Exam Attempt
app.post('/api/student/exam/:examId/start', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const { examId } = req.params;
    const userId = req.user!.id;
    const student = await getStudentFromUserId(userId);
    if (!student) return res.status(401).json({ status: 'error', message: 'Not a student' });

    let attempt = await prisma.examAttempt.findFirst({
      where: { examId, studentId: student.id }
    });

    if (!attempt) {
      attempt = await prisma.examAttempt.create({
        data: {
          examId,
          studentId: student.id,
          state: 'IN_PROGRESS',
          startedAt: new Date(),
          randomSeed: 0
        }
      });
    }

    const examQuestions = await prisma.examQuestion.findMany({
      where: { examId },
      include: { question: { include: { options: true } } }
    });

    let questions = examQuestions.map(eq => eq.question);
    if (questions.length === 0) {
      const exam = await prisma.exam.findUnique({ where: { id: examId } });
      if (exam) {
        const banks = await prisma.questionBank.findMany({
          where: { courseId: exam.courseId },
          include: { questions: { include: { options: true } } }
        });
        questions = banks.flatMap(b => b.questions);
      }
    }

    const formattedQuestions = questions.map(q => ({
      id: q.id,
      text: q.questionText,
      options: q.options.map(o => ({ id: o.id, label: o.label, text: o.optionText }))
    }));

    res.json({ status: 'success', attemptId: attempt.id, questions: formattedQuestions });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 2.3 Submit Answer
app.post('/api/student/exam/:attemptId/answer', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const { attemptId } = req.params;
    const { questionId, optionId } = req.body;
    
    const existing = await prisma.answer.findFirst({
      where: { attemptId, questionId }
    });

    if (existing) {
      await prisma.answer.update({
        where: { id: existing.id },
        data: { selectedOptionId: optionId }
      });
    } else {
      await prisma.answer.create({
        data: { attemptId, questionId, selectedOptionId: optionId }
      });
    }

    res.json({ status: 'success' });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 2.4 Report Violation
app.post('/api/student/exam/:attemptId/violation', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const { attemptId } = req.params;
    const { eventType, severity, scoreIncrement, metadata } = req.body;
    
    await prisma.violation.create({
      data: {
        attemptId,
        eventType,
        severity,
        scoreIncrement,
        metadata
      }
    });

    res.json({ status: 'success' });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 2.5 Server-Side Exam Submission & Grading (CRITICAL-4 Fix)
app.post('/api/student/exam/:attemptId/submit', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const { attemptId } = req.params;
    const userId = req.user!.id;

    // Verify the attempt belongs to this student
    const attempt = await prisma.examAttempt.findUnique({
      where: { id: attemptId },
      include: {
        student: true,
        exam: true,
        answers: { include: { selectedOption: true } },
        violations: true
      }
    });

    if (!attempt) {
      return res.status(404).json({ status: 'error', message: 'Sesi ujian tidak ditemukan.' });
    }
    if (attempt.student.userId !== userId) {
      return res.status(403).json({ status: 'error', message: 'Anda tidak berhak mengakses sesi ujian ini.' });
    }

    // Server-side authoritative grading
    const correctAnswers = attempt.answers.filter(a => a.selectedOption?.isCorrect).length;
    const totalQuestions = attempt.answers.length > 0 ? attempt.answers.length : 1;
    let baseScore = (correctAnswers / totalQuestions) * 100;

    // Deduct violation penalties
    const totalPenalty = attempt.violations.reduce((sum, v) => sum + v.scoreIncrement, 0);
    let finalScore = Math.max(0, baseScore - totalPenalty);

    // Update attempt state
    await prisma.examAttempt.update({
      where: { id: attemptId },
      data: {
        state: 'COMPLETED',
        submittedAt: new Date(),
        violationScore: totalPenalty
      }
    });

    const grade = finalScore >= 85 ? 'A' : finalScore >= 75 ? 'B' : finalScore >= 65 ? 'C' : finalScore >= 50 ? 'D' : 'E';

    res.json({
      status: 'success',
      data: {
        score: Math.round(baseScore),
        penalty: totalPenalty,
        finalScore: Math.round(finalScore),
        grade,
        correctAnswers,
        totalQuestions,
        passed: finalScore >= attempt.exam.passingGrade
      }
    });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: 'Gagal menyelesaikan ujian.' });
  }
});
// 2.6. User Management (Admin Feature — Protected)
app.get('/api/users', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const users = await prisma.user.findMany({
      include: {
        student: { include: { studyProgram: true } },
        lecturer: true
      },
      orderBy: { createdAt: 'desc' }
    });
    
    const formatted = users.map(u => ({
      id: u.id,
      roleId: u.roleId,
      username: u.username,
      fullName: u.fullName,
      // CRITICAL-1 FIX: passwordHash is NEVER sent to the client
      isActive: u.isActive,
      className: u.student?.className || '',
      studyProgramName: u.student?.studyProgram?.name || '',
      department: u.lecturer?.department || '',
      classification: u.roleId === 'STUDENT' ? (u.student ? `${u.student.studyProgram.name} - ${u.student.className}` : 'Belum Ada Kelas') : (u.roleId === 'LECTURER' ? (u.lecturer?.department || 'Umum') : 'Administrator')
    }));
    
    res.json({ status: 'success', data: formatted });
  } catch (error) {
    res.status(500).json({ status: 'error', message: 'Failed to fetch users' });
  }
});

app.post('/api/users', authenticateToken, requireRole(['ADMIN']), async (req: AuthRequest, res: Response) => {
  try {
    const { username, fullName, roleId, password, className, studyProgram, department } = req.body;
    
    const role = roleId === 'Admin' ? 'ADMIN' : (roleId === 'Dosen' ? 'LECTURER' : 'STUDENT');
    
    // Hash password with bcrypt (CRITICAL-3 fix)
    const hashedPassword = await bcrypt.hash(password || 'stikesbk123', BCRYPT_SALT_ROUNDS);

    const user = await prisma.user.create({
      data: {
        username,
        fullName,
        roleId: role,
        passwordHash: hashedPassword,
        email: `${username}@stikes.ac.id`,
        isActive: true
      }
    });

    // Create the associated record based on role
    if (role === 'STUDENT') {
      let sp = await prisma.studyProgram.findFirst({ where: { name: studyProgram } });
      if (!sp) {
        sp = await prisma.studyProgram.findFirst();
      }
      if (sp) {
        await prisma.student.create({
          data: {
            userId: user.id,
            nim: username,
            studyProgramId: sp.id,
            className: className || 'Umum',
            generationYear: new Date().getFullYear()
          }
        });
      }
    } else if (role === 'LECTURER') {
      await prisma.lecturer.create({
        data: {
          userId: user.id,
          nip: username,
          department: department || 'Fakultas Kesehatan'
        }
      });
    }

    // Do not return passwordHash in response (CRITICAL-1 fix)
    const { passwordHash: _, ...safeUser } = user;
    res.json({ status: 'success', data: safeUser });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

app.put('/api/users/:id', authenticateToken, requireRole(['ADMIN']), async (req: AuthRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { fullName, username, password, className, studyProgram, department } = req.body;

    const user = await prisma.user.findUnique({
      where: { id },
      include: { student: true, lecturer: true }
    });

    if (!user) {
      return res.status(404).json({ status: 'error', message: 'Pengguna tidak ditemukan' });
    }

    // Hash new password if provided (CRITICAL-3 fix)
    let updatedPasswordHash = user.passwordHash;
    if (password !== undefined && password.trim() !== '') {
      updatedPasswordHash = await bcrypt.hash(password, BCRYPT_SALT_ROUNDS);
    }

    // 1. Update User basic info
    await prisma.user.update({
      where: { id },
      data: {
        fullName: fullName !== undefined && fullName !== '' ? fullName : user.fullName,
        username: username !== undefined && username !== '' ? username : user.username,
        passwordHash: updatedPasswordHash
      }
    });

    // 2. If student, update class and study program
    if (user.roleId === 'STUDENT' && user.student) {
      let spId = user.student.studyProgramId;
      if (studyProgram) {
        const sp = await prisma.studyProgram.findFirst({ where: { name: studyProgram } });
        if (sp) spId = sp.id;
      }

      await prisma.student.update({
        where: { id: user.student.id },
        data: {
          nim: username !== undefined && username !== '' ? username : user.student.nim,
          className: className !== undefined ? className : user.student.className,
          studyProgramId: spId
        }
      });
    }

    // 3. If lecturer, update department
    if (user.roleId === 'LECTURER' && user.lecturer) {
      await prisma.lecturer.update({
        where: { id: user.lecturer.id },
        data: {
          nip: username !== undefined && username !== '' ? username : user.lecturer.nip,
          department: department !== undefined ? department : user.lecturer.department
        }
      });
    }

    console.log(`[User Updated] User ${id} (${user.fullName}) updated successfully.`);
    res.json({ status: 'success', message: 'Data pengguna dan kelas berhasil diperbarui.' });
  } catch (error: any) {
    console.error('[Update User Error]', error);
    res.status(500).json({ status: 'error', message: error.message || 'Gagal memperbarui data pengguna' });
  }
});

app.patch('/api/users/:id/status', authenticateToken, requireRole(['ADMIN']), async (req: AuthRequest, res: Response) => {
  try {
    const { isActive } = req.body;
    await prisma.user.update({
      where: { id: req.params.id },
      data: { isActive }
    });
    res.json({ status: 'success' });
  } catch (error) {
    res.status(500).json({ status: 'error' });
  }
});

app.delete('/api/users/:id', authenticateToken, requireRole(['ADMIN']), async (req: AuthRequest, res: Response) => {
  try {
    await prisma.user.delete({ where: { id: req.params.id } });
    res.json({ status: 'success' });
  } catch (error) {
    res.status(500).json({ status: 'error' });
  }
});

app.post('/api/users/:id/reset-password', authenticateToken, requireRole(['ADMIN']), async (req: AuthRequest, res: Response) => {
  try {
    // Hash default reset password with bcrypt (CRITICAL-3 fix)
    const resetHash = await bcrypt.hash('stikes123', BCRYPT_SALT_ROUNDS);
    await prisma.user.update({
      where: { id: req.params.id },
      data: { passwordHash: resetHash }
    });
    res.json({ status: 'success' });
  } catch (error) {
    res.status(500).json({ status: 'error' });
  }
});

// 6. Live Monitoring (Admin/Lecturer Dashboard — Protected)
app.get('/api/monitoring/live', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const students = await prisma.student.findMany({
      include: {
        user: true,
        attempts: {
          include: {
            answers: true,
            violations: true,
            exam: true
          },
          orderBy: { startedAt: 'desc' }
        }
      }
    });

    const formattedStudents = students.map((std: any) => {
      const activeAttempt = std.attempts.find((a: any) => a.state === 'IN_PROGRESS') || std.attempts[0];
      const isExamActive = activeAttempt?.state === 'IN_PROGRESS';
      
      const totalViolations = activeAttempt
        ? activeAttempt.violations.reduce((sum: number, log: any) => sum + log.scoreIncrement, 0)
        : 0;

      const remainingMinutes = activeAttempt
        ? Math.max(0, activeAttempt.exam.durationMinutes - Math.floor((Date.now() - (activeAttempt.startedAt?.getTime() || Date.now())) / 60000))
        : 60;

      return {
        id: std.id,
        attemptId: activeAttempt?.id || null,
        name: std.user.fullName,
        nim: std.user.username,
        answered: activeAttempt ? activeAttempt.answers.length : 0,
        total: 7,
        remainingMinutes: remainingMinutes,
        isOnline: isExamActive || (Date.now() - new Date(std.user.updatedAt || std.user.createdAt).getTime() < 600000),
        violationScore: totalViolations,
        category: totalViolations > 50 ? 'MENCURIGAKAN' : (totalViolations > 20 ? 'PERLU_DIPERIKSA' : 'NORMAL'),
        status: isExamActive ? 'Sedang Mengerjakan' : (activeAttempt?.state === 'COMPLETED' ? 'Selesai' : 'Belum Mulai')
      };
    });

    res.json({ status: 'success', data: formattedStudents });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 6.5 Get All Violations (Protected)
app.get('/api/monitoring/violations', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const logs = await prisma.violation.findMany({
      include: {
        attempt: {
          include: {
            student: {
              include: { user: true }
            }
          }
        }
      },
      orderBy: { createdAt: 'desc' }
    });

    const formattedLogs = logs.map((log: any) => ({
      id: log.id,
      studentName: log.attempt.student.user.fullName,
      studentNim: log.attempt.student.user.username,
      type: log.eventType,
      description: log.metadata || log.eventType,
      penalty: log.scoreIncrement,
      timestamp: log.createdAt
    }));

    res.json({ status: 'success', data: formattedLogs });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 7. Google Form Parser (Protected)
app.post('/api/google-forms/preview', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const { url } = req.body;
    if (!url || !url.includes('docs.google.com/forms')) {
      return res.status(400).json({ status: 'error', message: 'URL Google Form tidak valid' });
    }

    const response = await fetch(url);
    const html = await response.text();
    
    // Extract FB_PUBLIC_LOAD_DATA_
    const match = html.match(/var FB_PUBLIC_LOAD_DATA_ = (\[.*?\]);\s*<\/script>/);
    if (!match) {
      return res.status(400).json({ status: 'error', message: 'Gagal mengambil data form' });
    }

    const data = JSON.parse(match[1]);
    const items = data[1][1] || [];
    
    const parsedQuestions = [];
    
    for (const item of items) {
      const qText = item[1];
      const qType = item[3]; // 0 = Short answer, 1 = Paragraph, 2 = Multiple choice, 3 = Dropdown, 4 = Checkboxes
      
      if (qType === 2 || qType === 3 || qType === 4) {
        // Multiple choice
        const optionsData = item[4] && item[4][0] && item[4][0][1] ? item[4][0][1] : [];
        const options = optionsData.map((opt: any) => opt[0]);
        parsedQuestions.push({
          type: 'MULTIPLE_CHOICE',
          text: qText,
          options: options
        });
      } else if (qType === 0 || qType === 1) {
        // Essay
        parsedQuestions.push({
          type: 'ESSAY',
          text: qText,
          options: []
        });
      }
    }

    res.json({ status: 'success', title: data[1][8], questions: parsedQuestions });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 8. Save Google Form to Bank Soal (Protected)
app.post('/api/google-forms/save', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const { title, questions, className, courseName, lecturerName } = req.body;
    
    // Find or create Course
    let course = await prisma.course.findFirst({ where: { name: courseName || 'Umum' }});
    if (!course) {
      const sp = await prisma.studyProgram.findFirst();
      course = await prisma.course.create({ data: { 
        name: courseName || 'Umum', 
        code: (courseName || 'UMUM').substring(0,4).toUpperCase() + Date.now().toString().slice(-4),
        semester: 1,
        studyProgramId: sp?.id || ''
      } });
    }

    // Find or create Lecturer
    let lecturer = await prisma.lecturer.findFirst({ where: { user: { fullName: lecturerName || 'Dosen Umum' } } });
    if (!lecturer) {
      const defaultLecPasswordHash = await bcrypt.hash('stikes123', BCRYPT_SALT_ROUNDS);
      const user = await prisma.user.create({ data: { fullName: lecturerName || 'Dosen Umum', username: 'dosentemp' + Date.now().toString().slice(-4), passwordHash: defaultLecPasswordHash, roleId: 'LECTURER', email: 'dosen' + Date.now().toString().slice(-4) + '@stikes.ac.id', isActive: true } });
      lecturer = await prisma.lecturer.create({ data: { userId: user.id, nip: 'NIP' + Date.now().toString().slice(-4), department: 'Fakultas Kesehatan' } });
    }

    // 1. Create Question Bank
    const bank = await prisma.questionBank.create({
      data: {
        title: title || 'Imported Google Form',
        lecturerId: lecturer.id,
        courseId: course.id,
        category: className || 'Kelas Umum'
      }
    });

    // 2. Create Questions & Options
    for (const q of questions) {
      await prisma.question.create({
        data: {
          questionBankId: bank.id,
          type: q.type,
          questionText: q.text,
          options: {
            create: q.options.map((optText: string, index: number) => ({
              label: String.fromCharCode(65 + index), // A, B, C, D...
              optionText: optText,
              isCorrect: index === 0 // Assume first option is correct for demo if not specified
            }))
          }
        }
      });
    }

    res.json({ status: 'success', message: `${questions.length} soal berhasil disimpan ke database!` });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 9. Get Question Banks (Admin/Lecturer Feature — Protected)
app.get('/api/question-banks', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const banks = await prisma.questionBank.findMany({
      include: {
        course: true,
        lecturer: { include: { user: true } },
        _count: {
          select: { questions: true }
        }
      },
      orderBy: { createdAt: 'desc' }
    });
    res.json({ status: 'success', data: banks });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 10. Auto-Grading & Results (Protected)
app.get('/api/results', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const attempts = await prisma.examAttempt.findMany({
      include: {
        exam: { include: { course: true } },
        student: { include: { user: true, studyProgram: true } },
        answers: { include: { selectedOption: true } },
        violations: true
      },
      orderBy: { createdAt: 'desc' }
    });

    const results = attempts.map(attempt => {
      // Calculate correct answers
      const correctAnswers = attempt.answers.filter(a => a.selectedOption?.isCorrect).length;
      const totalQuestions = attempt.answers.length > 0 ? attempt.answers.length : attempt.exam.passingGrade > 0 ? 10 : 0; // fallback mock total
      
      // Calculate score (0-100 base)
      let baseScore = totalQuestions > 0 ? (correctAnswers / totalQuestions) * 100 : 0;
      
      // Deduct violations
      const totalPenalty = attempt.violations.reduce((sum, v) => sum + v.scoreIncrement, 0);
      let finalScore = baseScore - totalPenalty;
      if (finalScore < 0) finalScore = 0;

      return {
        id: attempt.id,
        studentName: attempt.student.user.fullName,
        studentNim: attempt.student.user.username,
        className: attempt.student.className,
        examTitle: attempt.exam.title,
        courseName: attempt.exam.course.name,
        status: attempt.state,
        baseScore: Math.round(baseScore),
        penalty: totalPenalty,
        finalScore: Math.round(finalScore),
        gradeStatus: finalScore >= attempt.exam.passingGrade ? 'LULUS' : 'TIDAK LULUS',
        submittedAt: attempt.submittedAt || attempt.createdAt
      };
    });

    res.json({ status: 'success', data: results });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 11. Courses & Lecturers API (Protected)
app.get('/api/courses', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const courses = await prisma.course.findMany({
      include: { studyProgram: true },
      orderBy: { name: 'asc' }
    });
    res.json({ status: 'success', data: courses });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

app.get('/api/lecturers', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const lecturers = await prisma.lecturer.findMany({
      include: { user: true },
      orderBy: { user: { fullName: 'asc' } }
    });
    res.json({ status: 'success', data: lecturers });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// 12. Exam Management (Lecturer/Admin — Protected)
app.get('/api/exams', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    const exams = await prisma.exam.findMany({
      include: {
        course: true,
        lecturer: { include: { user: true } },
        _count: { select: { assignments: true, examQuestions: true } }
      },
      orderBy: { createdAt: 'desc' }
    });
    res.json({ status: 'success', data: exams });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

app.post('/api/exams', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const { title, courseId, lecturerId, bankId, durationMinutes, startTime, endTime, passingGrade, className } = req.body;
    
    if (!title || !title.trim()) {
      return res.status(400).json({ status: 'error', message: 'Judul Ujian wajib diisi!' });
    }

    let finalCourseId = courseId;
    let finalLecturerId = lecturerId;

    // 1. If bankId provided, resolve course and lecturer from QuestionBank
    if (bankId) {
      const bank = await prisma.questionBank.findUnique({ where: { id: bankId } });
      if (bank) {
        if (!finalCourseId) finalCourseId = bank.courseId;
        if (!finalLecturerId) finalLecturerId = bank.lecturerId;
      }
    }

    // 2. Validate or fallback courseId
    if (finalCourseId) {
      const c = await prisma.course.findUnique({ where: { id: finalCourseId } });
      if (!c) finalCourseId = null;
    }
    if (!finalCourseId) {
      const defaultCourse = await prisma.course.findFirst();
      if (defaultCourse) {
        finalCourseId = defaultCourse.id;
      } else {
        const sp = await prisma.studyProgram.findFirst();
        const createdCourse = await prisma.course.create({
          data: {
            name: 'Keperawatan Medikal Bedah (KMB)',
            code: 'KEP101',
            semester: 1,
            studyProgramId: sp?.id || ''
          }
        });
        finalCourseId = createdCourse.id;
      }
    }

    // 3. Validate or fallback lecturerId
    if (finalLecturerId) {
      const l = await prisma.lecturer.findUnique({ where: { id: finalLecturerId } });
      if (!l) finalLecturerId = null;
    }
    if (!finalLecturerId) {
      const defaultLecturer = await prisma.lecturer.findFirst();
      if (defaultLecturer) {
        finalLecturerId = defaultLecturer.id;
      } else {
        const u = await prisma.user.findFirst({ where: { roleId: 'LECTURER' } });
        if (u) {
          const createdLec = await prisma.lecturer.create({
            data: { userId: u.id, nip: u.username, department: 'Keperawatan' }
          });
          finalLecturerId = createdLec.id;
        }
      }
    }

    // 4. Safe Dates & Numbers
    let parsedStart = startTime ? new Date(startTime) : new Date();
    if (isNaN(parsedStart.getTime())) parsedStart = new Date();

    let parsedEnd = endTime ? new Date(endTime) : new Date(Date.now() + 7 * 86400000);
    if (isNaN(parsedEnd.getTime())) parsedEnd = new Date(Date.now() + 7 * 86400000);

    const parsedDuration = parseInt(durationMinutes) || 60;
    const parsedPassing = !isNaN(parseFloat(passingGrade)) ? parseFloat(passingGrade) : 60.0;

    // 5. Create Exam
    const exam = await prisma.exam.create({
      data: {
        title: title.trim(),
        description: 'Ujian ini menggunakan sistem Anti-Cheating ketat.',
        courseId: finalCourseId!,
        lecturerId: finalLecturerId!,
        durationMinutes: parsedDuration,
        startTime: parsedStart,
        endTime: parsedEnd,
        passingGrade: parsedPassing,
        status: 'ACTIVE'
      }
    });

    // 6. Map Questions from Bank or All Available Questions
    let questionsToMap: any[] = [];
    if (bankId && bankId !== '' && bankId !== 'ALL') {
      questionsToMap = await prisma.question.findMany({ where: { questionBankId: bankId } });
    } else {
      // Map all available questions without limitation
      questionsToMap = await prisma.question.findMany();
    }

    for (let i = 0; i < questionsToMap.length; i++) {
      await prisma.examQuestion.create({
        data: { examId: exam.id, questionId: questionsToMap[i].id, orderIndex: i }
      }).catch(() => {});
    }

    // 7. Assign to Students
    if (className && className.trim() !== '' && className !== 'SEMUA') {
      const students = await prisma.student.findMany({ where: { className: className.trim() } });
      for (const s of students) {
        await prisma.examAssignment.create({
          data: { examId: exam.id, studentId: s.id }
        }).catch(() => {});
      }
    } else {
      const students = await prisma.student.findMany();
      for (const s of students) {
        await prisma.examAssignment.create({
          data: { examId: exam.id, studentId: s.id }
        }).catch(() => {});
      }
    }

    console.log(`[Exam Created] "${exam.title}" (${exam.id}) created with ${questionsToMap.length} questions and distributed to students.`);
    res.json({ status: 'success', message: 'Jadwal Ujian berhasil dibuat dan didistribusikan ke Mahasiswa.', data: exam });
  } catch (error: any) {
    console.error('[Exam Creation Error]', error);
    res.status(500).json({ status: 'error', message: error.message || 'Gagal menyimpan ujian' });
  }
});

app.put('/api/exams/:id', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const { id } = req.params;
    const { title, className, durationMinutes, passingGrade, status, startTime, endTime, bankId } = req.body;

    const existing = await prisma.exam.findUnique({ where: { id } });
    if (!existing) return res.status(404).json({ status: 'error', message: 'Jadwal ujian tidak ditemukan' });

    const updateData: any = {};
    if (title) updateData.title = title.trim();
    if (durationMinutes) updateData.durationMinutes = parseInt(durationMinutes);
    if (passingGrade !== undefined) updateData.passingGrade = parseFloat(passingGrade);
    if (status) updateData.status = status;
    if (startTime) updateData.startTime = new Date(startTime);
    if (endTime) updateData.endTime = new Date(endTime);

    const updated = await prisma.exam.update({
      where: { id },
      data: updateData
    });

    if (bankId && bankId.trim() !== '') {
      await prisma.examQuestion.deleteMany({ where: { examId: id } });
      let questionsToMap: any[] = [];
      if (bankId !== 'ALL') {
        questionsToMap = await prisma.question.findMany({ where: { questionBankId: bankId } });
      } else {
        questionsToMap = await prisma.question.findMany();
      }
      for (let i = 0; i < questionsToMap.length; i++) {
        await prisma.examQuestion.create({
          data: { examId: id, questionId: questionsToMap[i].id, orderIndex: i }
        }).catch(() => {});
      }
    }

    if (className !== undefined && className.trim() !== '' && className !== 'SEMUA') {
      const students = await prisma.student.findMany({ where: { className: className.trim() } });
      for (const s of students) {
        await prisma.examAssignment.upsert({
          where: { examId_studentId: { examId: id, studentId: s.id } },
          update: {},
          create: { examId: id, studentId: s.id }
        }).catch(() => {});
      }
    }

    res.json({ status: 'success', message: 'Jadwal ujian berhasil diperbarui.', data: updated });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

app.delete('/api/exams/:id', authenticateToken, requireRole(['ADMIN', 'LECTURER']), async (req: AuthRequest, res: Response) => {
  try {
    const { id } = req.params;
    await prisma.exam.delete({ where: { id } });
    res.json({ status: 'success', message: 'Jadwal ujian berhasil dihapus.' });
  } catch (error: any) {
    res.status(500).json({ status: 'error', message: error.message });
  }
});

// Start Server
seedDatabase().then(() => {
  app.listen(Number(port), '0.0.0.0', () => {
    console.log(`🚀 Server Backend CampusExam menyala di port ${port} pada 0.0.0.0 dengan Database SQLite!`);
  });
}).catch(e => {
  console.error("Gagal memulai server:", e);
});
