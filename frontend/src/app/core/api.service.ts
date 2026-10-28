import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map, switchMap } from 'rxjs';
import {
  Certification,
  CertificationWrite,
  Education,
  EducationWrite,
  Experience,
  ExperienceWrite,
  Interview,
  InterviewImprovement,
  InterviewSummary,
  JobMatch,
  JobPosting,
  LanguageSkill,
  LanguageWrite,
  Page,
  Profile,
  ProjectItem,
  ProjectWrite,
  ResumeDetail,
  ResumeSummary,
  ResumeWrite,
  ReviewRequest,
  Session,
  SetupStatus,
  Skill,
  SkillWrite,
  TextRevision,
  asList,
  normalizePage,
} from './models';
import { SessionStore } from './session.store';

@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly sessions = inject(SessionStore);

  session(): Observable<Session> {
    return this.sessions.load();
  }

  bootstrap(email: string, password: string, fullName: string): Observable<Session> {
    return this.http
      .post('/api/v1/auth/bootstrap', { email, password, fullName })
      .pipe(switchMap(() => this.sessions.load()));
  }

  login(email: string, password: string): Observable<Session> {
    return this.http
      .post('/api/v1/auth/login', { email, password })
      .pipe(switchMap(() => this.sessions.load()));
  }

  logout(): Observable<unknown> {
    return this.http.post('/api/v1/auth/logout', {});
  }

  deleteAccount(): Observable<unknown> {
    return this.http.delete('/api/v1/account');
  }

  setupStatus(): Observable<SetupStatus> {
    return this.http.get<SetupStatus>('/api/v1/setup/status');
  }

  saveApiKey(apiKey: string): Observable<SetupStatus> {
    return this.http.post<SetupStatus>('/api/v1/setup/api-key', { apiKey });
  }

  deleteApiKey(): Observable<SetupStatus> {
    return this.http.delete<SetupStatus>('/api/v1/setup/api-key');
  }

  testConnection(): Observable<SetupStatus> {
    return this.http.post<SetupStatus>('/api/v1/setup/test-connection', {});
  }

  profile(): Observable<Profile> {
    return this.http.get<Profile>('/api/v1/profile');
  }

  saveProfile(profile: Profile): Observable<Profile> {
    return this.http.put<Profile>('/api/v1/profile', profile);
  }

  consent(): Observable<Profile> {
    return this.http.post<Profile>('/api/v1/profile/consent', {});
  }

  uploadPhoto(file: File): Observable<Profile> {
    const data = new FormData();
    data.set('file', file);
    return this.http.post<Profile>('/api/v1/profile/photo', data);
  }

  deletePhoto(): Observable<unknown> {
    return this.http.delete('/api/v1/profile/photo');
  }

  approvePhoto(): Observable<Profile> {
    return this.http.post<Profile>('/api/v1/profile/photo/approve', {});
  }

  experiences(): Observable<Experience[]> {
    return this.http
      .get<Experience[] | Page<Experience>>('/api/v1/experiences')
      .pipe(map((body) => asList(body)));
  }

  createExperience(body: ExperienceWrite): Observable<Experience> {
    return this.http.post<Experience>('/api/v1/experiences', body);
  }

  updateExperience(id: string, body: ExperienceWrite): Observable<Experience> {
    return this.http.put<Experience>(`/api/v1/experiences/${id}`, body);
  }

  deleteExperience(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/experiences/${id}`);
  }

  orderExperiences(ids: string[]): Observable<unknown> {
    return this.http.put('/api/v1/experiences/order', { ids });
  }

  education(): Observable<Education[]> {
    return this.http
      .get<Education[] | Page<Education>>('/api/v1/education')
      .pipe(map((body) => asList(body)));
  }

  createEducation(body: EducationWrite): Observable<Education> {
    return this.http.post<Education>('/api/v1/education', body);
  }

  updateEducation(id: string, body: EducationWrite): Observable<Education> {
    return this.http.put<Education>(`/api/v1/education/${id}`, body);
  }

  deleteEducation(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/education/${id}`);
  }

  orderEducation(ids: string[]): Observable<unknown> {
    return this.http.put('/api/v1/education/order', { ids });
  }

  certifications(): Observable<Certification[]> {
    return this.http
      .get<Certification[] | Page<Certification>>('/api/v1/certifications')
      .pipe(map((body) => asList(body)));
  }

  createCertification(body: CertificationWrite): Observable<Certification> {
    return this.http.post<Certification>('/api/v1/certifications', body);
  }

  updateCertification(id: string, body: CertificationWrite): Observable<Certification> {
    return this.http.put<Certification>(`/api/v1/certifications/${id}`, body);
  }

  deleteCertification(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/certifications/${id}`);
  }

  orderCertifications(ids: string[]): Observable<unknown> {
    return this.http.put('/api/v1/certifications/order', { ids });
  }

  skills(): Observable<Skill[]> {
    return this.http.get<Skill[] | Page<Skill>>('/api/v1/skills').pipe(map((body) => asList(body)));
  }

  createSkill(body: SkillWrite): Observable<Skill> {
    return this.http.post<Skill>('/api/v1/skills', body);
  }

  updateSkill(id: string, body: SkillWrite): Observable<Skill> {
    return this.http.put<Skill>(`/api/v1/skills/${id}`, body);
  }

  deleteSkill(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/skills/${id}`);
  }

  orderSkills(ids: string[]): Observable<unknown> {
    return this.http.put('/api/v1/skills/order', { ids });
  }

  suggestSkills(): Observable<Skill[]> {
    return this.http
      .post<Skill[] | Page<Skill>>('/api/v1/skills/suggest', {})
      .pipe(map((body) => asList(body)));
  }

  languages(): Observable<LanguageSkill[]> {
    return this.http
      .get<LanguageSkill[] | Page<LanguageSkill>>('/api/v1/languages')
      .pipe(map((body) => asList(body)));
  }

  createLanguage(body: LanguageWrite): Observable<LanguageSkill> {
    return this.http.post<LanguageSkill>('/api/v1/languages', body);
  }

  updateLanguage(id: string, body: LanguageWrite): Observable<LanguageSkill> {
    return this.http.put<LanguageSkill>(`/api/v1/languages/${id}`, body);
  }

  deleteLanguage(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/languages/${id}`);
  }

  projects(): Observable<ProjectItem[]> {
    return this.http
      .get<ProjectItem[] | Page<ProjectItem>>('/api/v1/projects')
      .pipe(map((body) => asList(body)));
  }

  createProject(body: ProjectWrite): Observable<ProjectItem> {
    return this.http.post<ProjectItem>('/api/v1/projects', body);
  }

  updateProject(id: string, body: ProjectWrite): Observable<ProjectItem> {
    return this.http.put<ProjectItem>(`/api/v1/projects/${id}`, body);
  }

  deleteProject(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/projects/${id}`);
  }

  orderProjects(ids: string[]): Observable<unknown> {
    return this.http.put('/api/v1/projects/order', { ids });
  }

  interviews(page = 0, size = 20): Observable<Page<InterviewSummary>> {
    return this.http
      .get<Page<InterviewSummary> | InterviewSummary[]>('/api/v1/interviews', {
        params: { page: String(page), size: String(size) },
      })
      .pipe(map((body) => normalizePage(body)));
  }

  createInterview(): Observable<Interview> {
    return this.http.post<Interview>('/api/v1/interviews', {});
  }

  interview(id: string): Observable<Interview> {
    return this.http.get<Interview>(`/api/v1/interviews/${id}`);
  }

  sendMessage(id: string, content: string, skip: boolean): Observable<Interview> {
    return this.http.post<Interview>(`/api/v1/interviews/${id}/messages`, { content, skip });
  }

  correctMessage(interviewId: string, messageId: string, content: string): Observable<Interview> {
    return this.http.put<Interview>(`/api/v1/interviews/${interviewId}/messages/${messageId}`, {
      content,
    });
  }

  pauseInterview(id: string): Observable<unknown> {
    return this.http.post(`/api/v1/interviews/${id}/pause`, {});
  }

  resumeInterview(id: string): Observable<unknown> {
    return this.http.post(`/api/v1/interviews/${id}/resume`, {});
  }

  deleteInterview(id: string): Observable<unknown> {
    return this.http.delete(`/api/v1/interviews/${id}`);
  }

  resumeDraft(id: string): Observable<ResumeDetail> {
    return this.http.post<ResumeDetail>(`/api/v1/interviews/${id}/resume-draft`, {});
  }

  improveInterview(id: string): Observable<InterviewImprovement> {
    return this.http.post<InterviewImprovement>(`/api/v1/interviews/${id}/improve`, {});
  }

  resumes(page = 0, size = 20): Observable<Page<ResumeSummary>> {
    return this.http
      .get<Page<ResumeSummary> | ResumeSummary[]>('/api/v1/resumes', {
        params: { page: String(page), size: String(size) },
      })
      .pipe(map((body) => normalizePage(body)));
  }

  createResume(title: string): Observable<ResumeDetail> {
    return this.http.post<ResumeDetail>('/api/v1/resumes', { title });
  }

  resume(id: string): Observable<ResumeDetail> {
    return this.http.get<ResumeDetail>(`/api/v1/resumes/${id}`);
  }

  updateResume(id: string, body: ResumeWrite): Observable<ResumeDetail> {
    return this.http.put<ResumeDetail>(`/api/v1/resumes/${id}`, body);
  }

  generateSummary(id: string): Observable<TextRevision> {
    return this.http.post<TextRevision>(`/api/v1/resumes/${id}/summary`, {});
  }

  review(id: string, body: ReviewRequest): Observable<TextRevision> {
    return this.http.post<TextRevision>(`/api/v1/resumes/${id}/review`, body);
  }

  approveResume(id: string): Observable<ResumeDetail> {
    return this.http.post<ResumeDetail>(`/api/v1/resumes/${id}/approve`, {});
  }

  previewPdf(id: string): Observable<HttpResponse<Blob>> {
    return this.http.post(`/api/v1/resumes/${id}/preview/pdf`, {}, {
      observe: 'response',
      responseType: 'blob',
    });
  }

  exportPdf(id: string): Observable<HttpResponse<Blob>> {
    return this.http.post(`/api/v1/resumes/${id}/export/pdf`, {}, {
      observe: 'response',
      responseType: 'blob',
    });
  }

  createJob(body: { title: string; company: string | null; description: string }): Observable<JobPosting> {
    return this.http.post<JobPosting>('/api/v1/jobs', body);
  }

  jobMatch(resumeId: string, jobId: string): Observable<JobMatch> {
    return this.http.post<JobMatch>(`/api/v1/resumes/${resumeId}/job-match`, { jobId });
  }
}
