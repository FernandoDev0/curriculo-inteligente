import { ResumeDetail, ResumeHeader, ResumeSnapshot } from './models';

export function snapshotOf(resume: ResumeDetail | null | undefined): ResumeSnapshot {
  if (!resume) {
    return {};
  }
  return resume.snapshot ?? resume.preview ?? resume.document ?? {};
}

export function headerOf(resume: ResumeDetail | null | undefined): ResumeHeader {
  return snapshotOf(resume).header ?? {};
}
