export interface Session {
  authenticated: boolean;
  needsBootstrap: boolean;
  email: string | null;
}

export type SetupState =
  | 'KEY_MISSING'
  | 'UNTESTED'
  | 'TESTING'
  | 'VALIDATED'
  | 'QUOTA'
  | 'INVALID_CREDENTIALS'
  | 'CONNECTIVITY'
  | 'UNAVAILABLE';

export interface SetupStatus {
  keyConfigured: boolean;
  keySource: string;
  model: string;
  imageEditingAvailable: boolean;
  state: SetupState | string;
  lastTestAt: string | null;
  lastTestMessage: string | null;
}

export interface ApiErrorBody {
  code?: string;
  message?: string;
  fields?: Record<string, string>;
  pageCount?: number;
  hints?: string[];
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Profile {
  fullName: string;
  targetRole: string;
  currentProfession: string | null;
  city: string | null;
  state: string | null;
  professionalEmail: string | null;
  phone: string | null;
  linkedinUrl: string | null;
  githubUrl: string | null;
  portfolioUrl: string | null;
  otherProfessionalLinks: string | null;
  workMode: string | null;
  targetOpportunity: string | null;
  includeAge: boolean;
  age: number | null;
  careerChange: boolean;
  desiredProfession: string | null;
  careerChangeReason: string | null;
  photoApproved: boolean;
  hasPhoto: boolean;
  aiConsentAt: string | null;
}

export interface Experience {
  id: string;
  company: string;
  roleTitle: string;
  startYear: number;
  startMonth: number | null;
  endYear: number | null;
  endMonth: number | null;
  currentJob: boolean;
  projectLabel: string | null;
  responsibilities: string | null;
  tools: string | null;
  results: string | null;
  notes: string | null;
  sortOrder: number;
  detailPreference: string;
}

export type ExperienceWrite = Omit<Experience, 'id'>;

export interface Education {
  id: string;
  level: string;
  course: string | null;
  institution: string | null;
  status: string;
  startYear: number | null;
  endYear: number | null;
  sortOrder: number;
}

export type EducationWrite = Omit<Education, 'id'>;

export interface Certification {
  id: string;
  name: string;
  issuer: string | null;
  year: number | null;
  sortOrder: number;
}

export type CertificationWrite = Omit<Certification, 'id'>;

export interface Skill {
  id: string;
  name: string;
  category: string;
  confirmed: boolean;
  origin: string;
  sortOrder: number;
}

export type SkillWrite = Omit<Skill, 'id'>;

export interface LanguageSkill {
  id: string;
  name: string;
  comprehension: string | null;
  reading: string | null;
  writing: string | null;
  conversation: string | null;
}

export type LanguageWrite = Omit<LanguageSkill, 'id'>;

export interface ProjectItem {
  id: string;
  name: string;
  description: string | null;
  url: string | null;
  personal: boolean;
  sortOrder: number;
}

export type ProjectWrite = Omit<ProjectItem, 'id'>;

export interface InterviewQuestion {
  key: string;
  text: string;
  required: boolean;
  canSkip: boolean;
}

export interface InterviewMessage {
  id: string;
  role: string;
  stage: string;
  questionKey: string | null;
  content: string;
  skipped: boolean;
  createdAt: string;
}

export interface Interview {
  id: string;
  status: string;
  stage: string;
  questionKey: string | null;
  progressPercent: number;
  currentQuestion: InterviewQuestion | null;
  messages: InterviewMessage[];
}

export interface ImprovedPiece {
  company: string;
  roleTitle: string;
  original: string;
  revised: string;
  keptOriginal: boolean;
}

export interface InterviewImprovement {
  resumeId: string;
  summary: string | null;
  experiences: ImprovedPiece[];
  note: string;
}

export interface InterviewSummary {
  id: string;
  status: string;
  stage: string;
  questionKey: string | null;
  progressPercent?: number;
  updatedAt?: string;
  createdAt?: string;
}

export interface ResumeSummary {
  id: string;
  title: string;
  status: string;
  targetRole: string | null;
  updatedAt?: string;
  createdAt?: string;
}

export interface ResumeHeader {
  fullName?: string | null;
  targetRole?: string | null;
  city?: string | null;
  state?: string | null;
  workMode?: string | null;
  professionalEmail?: string | null;
  phone?: string | null;
  linkedinUrl?: string | null;
  githubUrl?: string | null;
  portfolioUrl?: string | null;
  otherProfessionalLinks?: string | null;
  age?: number | null;
  includeAge?: boolean | null;
  hasPhoto?: boolean | null;
}

export interface SnapshotExperience {
  id?: string;
  company?: string | null;
  roleTitle?: string | null;
  startYear?: number | null;
  startMonth?: number | null;
  endYear?: number | null;
  endMonth?: number | null;
  currentJob?: boolean | null;
  projectLabel?: string | null;
  responsibilities?: string | null;
  tools?: string | null;
  results?: string | null;
  detailLevel?: string | null;
}

export interface SnapshotSkill {
  name?: string | null;
  category?: string | null;
}

export interface SnapshotEducation {
  level?: string | null;
  course?: string | null;
  institution?: string | null;
  status?: string | null;
  startYear?: number | null;
  endYear?: number | null;
}

export interface SnapshotLanguage {
  name?: string | null;
  comprehension?: string | null;
  reading?: string | null;
  writing?: string | null;
  conversation?: string | null;
}

export interface SnapshotProject {
  name?: string | null;
  description?: string | null;
  url?: string | null;
  personal?: boolean | null;
}

export interface SnapshotCertification {
  name?: string | null;
  issuer?: string | null;
  year?: number | null;
}

export interface ResumeSnapshot {
  header?: ResumeHeader | null;
  summary?: string | null;
  experiences?: SnapshotExperience[] | null;
  skills?: SnapshotSkill[] | null;
  education?: SnapshotEducation[] | null;
  languages?: SnapshotLanguage[] | null;
  projects?: SnapshotProject[] | null;
  certifications?: SnapshotCertification[] | null;
}

export interface ResumeDetail {
  id: string;
  title: string;
  status: string;
  targetRole: string | null;
  summaryDraft: string | null;
  summaryApproved: string | null;
  templateCode: string;
  includePhoto: boolean;
  approvedAt: string | null;
  snapshot?: ResumeSnapshot | null;
  preview?: ResumeSnapshot | null;
  document?: ResumeSnapshot | null;
}

export interface ResumeWrite {
  title: string;
  targetRole: string | null;
  summaryDraft: string | null;
  templateCode: string;
  includePhoto: boolean;
}

export interface TextRevision {
  original: string;
  revised: string;
}

export interface ReviewRequest {
  section: 'EXPERIENCE' | 'SUMMARY';
  experienceId?: string;
  text: string;
}

export interface JobPosting {
  id: string;
  title: string;
  company: string | null;
  description: string;
}

export interface JobMatch {
  demonstrated: string[];
  related: string[];
  missing: string[];
  keywords: string[];
  suggestions: string;
}

export function emptyPage<T>(): Page<T> {
  return { content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 };
}

export function asList<T>(body: T[] | Page<T> | null | undefined): T[] {
  if (!body) {
    return [];
  }
  if (Array.isArray(body)) {
    return body;
  }
  return body.content ?? [];
}

export function normalizePage<T>(body: Page<T> | T[] | null | undefined): Page<T> {
  if (!body) {
    return emptyPage<T>();
  }
  if (Array.isArray(body)) {
    return {
      content: body,
      page: 0,
      size: body.length,
      totalElements: body.length,
      totalPages: body.length ? 1 : 0,
    };
  }
  return {
    content: body.content ?? [],
    page: body.page ?? 0,
    size: body.size ?? 20,
    totalElements: body.totalElements ?? body.content?.length ?? 0,
    totalPages: body.totalPages ?? 0,
  };
}
