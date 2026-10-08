import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';

const app = express();
const port = process.env.PORT || 3000;

app.use(helmet());
app.use(cors());
app.use(express.json());

// In-Memory state for standalone execution / seed replication
interface QuestionOption {
  id: string;
  label: string;
  text: string;
  isCorrect: boolean;
}

interface Question {
  id: string;
  number: number;
  type: 'MULTIPLE_CHOICE' | 'ESSAY';
  text: string;
  options: QuestionOption[];
  maxScore: number;
  category: string;
}

interface LiveStudent {
  studentId: string;
  studentName: string;
  nim: string;
  answeredCount: number;
  totalQuestions: number;
  remainingSeconds: number;
  isOnline: boolean;
  securityLevel: 'GREEN' | 'YELLOW' | 'RED';
  violationScore: number;
  category: 'NORMAL' | 'PERLU_DIPERIKSA' | 'MENCURIGAKAN' | 'PELANGGARAN_BERAT';
  lastActive: number;
}

const liveStudents: LiveStudent[] = [
  {
    studentId: 'std_01',
    studentName: 'Budi Pratama',
    nim: '202400101',
    answeredCount: 5,
    totalQuestions: 7,
    remainingSeconds: 2890,
    isOnline: true,
    securityLevel: 'GREEN',
    violationScore: 0,
    category: 'NORMAL',
    lastActive: Date.now()
  },
  {
    studentId: 'std_02',
    studentName: 'Dewi Lestari',
    nim: '202400102',
    answeredCount: 6,
    totalQuestions: 7,
    remainingSeconds: 2600,
    isOnline: true,
    securityLevel: 'GREEN',
    violationScore: 20,
    category: 'NORMAL',
    lastActive: Date.now() - 3000
  },
  {
    studentId: 'std_03',
    studentName: 'Rian Saputra',
    nim: '202400103',
    answeredCount: 2,
    totalQuestions: 7,
    remainingSeconds: 3050,
    isOnline: true,
    securityLevel: 'YELLOW',
    violationScore: 60,
    category: 'MENCURIGAKAN',
    lastActive: Date.now() - 12000
  }
];

const questionBank: Question[] = [
  {
    id: 'qb_1',
    number: 1,
    type: 'MULTIPLE_CHOICE',
    text: 'Kompleksitas waktu rata-rata untuk algoritma QuickSort dalam notasi Big-O adalah:',
    options: [
      { id: 'opt_1', label: 'A', text: 'O(n)', isCorrect: false },
      { id: 'opt_2', label: 'B', text: 'O(n log n)', isCorrect: true },
      { id: 'opt_3', label: 'C', text: 'O(n^2)', isCorrect: false },
      { id: 'opt_4', label: 'D', text: 'O(log n)', isCorrect: false }
    ],
    maxScore: 10,
    category: 'Algoritma'
  },
  {
    id: 'qb_2',
    number: 2,
    type: 'MULTIPLE_CHOICE',
    text: 'Struktur data manakah yang mengimplementasikan prinsip FIFO?',
    options: [
      { id: 'opt_2a', label: 'A', text: 'Stack', isCorrect: false },
      { id: 'opt_2b', label: 'B', text: 'Queue', isCorrect: true },
      { id: 'opt_2c', label: 'C', text: 'Binary Tree', isCorrect: false },
      { id: 'opt_2d', label: 'D', text: 'Graph', isCorrect: false }
    ],
    maxScore: 10,
    category: 'Struktur Data'
  }
];

// 1. Healthcheck
app.get('/health', (req: Request, res: Response) => {
  res.json({ status: 'UP', service: 'campusexam-api', timestamp: Date.now() });
});

// 2. Authentication
app.post('/api/auth/login', (req: Request, res: Response) => {
  const { username, password } = req.body;
  
  if (username === 'admin' || username === 'admin_puskom') {
    return res.json({
      status: 'success',
      token: 'jwt_admin_token_mock',
      user: { id: 'usr_adm_01', username, role: 'ADMIN', name: 'Super Administrator' }
    });
  }

  if (username.startsWith('198')) {
    return res.json({
      status: 'success',
      token: 'jwt_lecturer_token_mock',
      user: { id: 'usr_lec_01', username, role: 'LECTURER', name: 'Dr. Ir. Hendra Gunawan, M.T.' }
    });
  }

  return res.json({
    status: 'success',
    token: 'jwt_student_token_mock',
    user: { id: 'usr_std_01', username: username || '202400101', role: 'STUDENT', name: 'Budi Pratama' }
  });
});

// 3. Server Authoritative Timer & Exam Session
app.post('/api/student/exams/:id/start', (req: Request, res: Response) => {
  const serverNow = Date.now();
  const durationMinutes = 60;
  const serverEndTime = serverNow + durationMinutes * 60 * 1000;

  res.json({
    attemptId: 'att_' + Date.now(),
    serverStartTime: serverNow,
    serverEndTime: serverEndTime,
    serverCurrentTime: serverNow,
    questions: questionBank
  });
});

// 4. Autosave Answer
app.post('/api/student/exams/:id/answer', (req: Request, res: Response) => {
  const { questionId, selectedOptionId, essayText } = req.body;
  res.json({
    status: 'success',
    message: 'Answer saved on server',
    serverTimestamp: Date.now()
  });
});

// 5. Anti-Cheating Violations Logger
app.post('/api/student/exams/:id/violations', (req: Request, res: Response) => {
  const { eventType, durationMs, score } = req.body;
  console.log(`[AntiCheating] Violation recorded: ${eventType} (${score} pts) duration: ${durationMs}ms`);
  res.json({ status: 'success', recorded: true });
});

// 6. Live Monitoring for Lecturer
app.get('/api/exams/:id/live', (req: Request, res: Response) => {
  res.json({
    examId: req.params.id,
    students: liveStudents,
    timestamp: Date.now()
  });
});

// 7. Google Form Parser & Importer
app.post('/api/google-forms/preview', (req: Request, res: Response) => {
  const { formUrl } = req.body;
  
  const parsed = [
    {
      id: 'gf_q1_' + Date.now(),
      number: 1,
      type: 'MULTIPLE_CHOICE',
      text: '[Google Form Import] Protokol layer transport apa yang menyediakan layanan connection-oriented dan reliable?',
      options: [
        { id: 'opt_a', label: 'A', text: 'UDP', isCorrect: false },
        { id: 'opt_b', label: 'B', text: 'TCP', isCorrect: true },
        { id: 'opt_c', label: 'C', text: 'IP', isCorrect: false },
        { id: 'opt_d', label: 'D', text: 'ICMP', isCorrect: false }
      ],
      maxScore: 10,
      category: 'Jaringan'
    },
    {
      id: 'gf_q2_' + Date.now(),
      number: 2,
      type: 'ESSAY',
      text: '[Google Form Import] Jelaskan proses 3-Way Handshake pada pembentukan koneksi TCP!',
      options: [],
      maxScore: 20,
      category: 'Jaringan'
    }
  ];

  res.json({ status: 'success', questions: parsed });
});

app.post('/api/google-forms/import', (req: Request, res: Response) => {
  const { questions } = req.body;
  if (Array.isArray(questions)) {
    questionBank.push(...questions);
  }
  res.json({ status: 'success', importedCount: questions?.length || 0 });
});

// Start API Server
app.listen(port, () => {
  console.log(`CampusExam API Server running on port ${port}`);
});
