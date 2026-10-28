import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Api } from '../../core/api.service';
import { readApiError, readHttpError } from '../../core/api-error';
import { downloadBlob } from '../../core/download';
import {
  educationStatusLabel,
  formatPeriod,
  languageLevelLabel,
  linesOf,
  skillCategoryLabel,
  workModeLabel,
} from '../../core/format';
import {
  ResumeDetail,
  ResumeHeader,
  SnapshotEducation,
  SnapshotExperience,
  SnapshotLanguage,
  SnapshotSkill,
} from '../../core/models';
import { headerOf, snapshotOf } from '../../core/snapshot';

@Component({
  selector: 'app-previa',
  imports: [RouterLink],
  templateUrl: './previa.html',
})
export class Previa {
  private readonly api = inject(Api);
  private readonly route = inject(ActivatedRoute);

  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly previewing = signal(false);
  readonly previewError = signal<string | null>(null);
  readonly pageCount = signal<number | null>(null);
  readonly pageCountKnown = signal(false);
  readonly previewFinished = signal(false);
  readonly resume = signal<ResumeDetail | null>(null);
  readonly photoBroken = signal(false);
  readonly id = signal('');
  readonly sheet = computed(() => snapshotOf(this.resume()));
  readonly header = computed(() => headerOf(this.resume()));

  constructor() {
    this.route.paramMap.pipe(takeUntilDestroyed()).subscribe((params) => {
      const id = params.get('id') ?? '';
      this.id.set(id);
      this.photoBroken.set(false);
      if (id) {
        this.load(id);
      }
    });
  }

  load(id = this.id()): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.resume(id).subscribe({
      next: (resume) => {
        this.resume.set(resume);
        this.loading.set(false);
      },
      error: (err: unknown) => {
        this.error.set(readApiError(err).message ?? 'Não foi possível abrir a prévia.');
        this.loading.set(false);
      },
    });
  }

  requestPdf(): void {
    if (this.previewing()) {
      return;
    }
    this.previewing.set(true);
    this.previewError.set(null);
    this.pageCount.set(null);
    this.pageCountKnown.set(false);
    this.previewFinished.set(false);
    this.api.previewPdf(this.id()).subscribe({
      next: (response) => {
        const raw = response.headers.get('X-Page-Count');
        const count = raw == null || raw.trim() === '' ? Number.NaN : Number(raw);
        if (Number.isFinite(count)) {
          this.pageCount.set(count);
          this.pageCountKnown.set(true);
        }
        const saved = downloadBlob(response.body, 'previa.pdf');
        if (!saved) {
          this.previewError.set('A resposta chegou, mas o navegador não entregou o PDF de prévia.');
        }
        this.previewFinished.set(true);
        this.previewing.set(false);
      },
      error: (err: unknown) => {
        void this.failPreview(err);
      },
    });
  }

  showPhoto(): boolean {
    const resume = this.resume();
    if (!resume?.includePhoto || this.photoBroken()) {
      return false;
    }
    return this.header().hasPhoto !== false;
  }

  place(header: ResumeHeader): string {
    const city = [header.city, header.state].filter((part) => !!part).join('/');
    const mode = workModeLabel(header.workMode ?? null);
    const age = header.includeAge && header.age != null ? `${header.age} anos` : '';
    return [city, mode, age].filter((part) => part.length > 0).join(' · ');
  }

  contacts(header: ResumeHeader): { href: string | null; caption: string; label: string }[] {
    const items: { href: string | null; caption: string; label: string }[] = [];
    if (header.professionalEmail) {
      items.push({ href: `mailto:${header.professionalEmail}`, caption: 'E-mail', label: header.professionalEmail });
    }
    if (header.phone) {
      items.push({ href: `tel:${header.phone}`, caption: 'Telefone/WhatsApp', label: header.phone });
    }
    if (header.linkedinUrl) {
      items.push({ href: header.linkedinUrl, caption: 'LinkedIn', label: header.linkedinUrl });
    }
    if (header.githubUrl) {
      items.push({ href: header.githubUrl, caption: 'GitHub', label: header.githubUrl });
    }
    if (header.portfolioUrl) {
      items.push({ href: header.portfolioUrl, caption: 'Portfólio', label: header.portfolioUrl });
    }
    if (header.otherProfessionalLinks) {
      items.push({ href: null, caption: 'Outros', label: header.otherProfessionalLinks });
    }
    return items;
  }

  technologies(): string {
    return (this.sheet().skills ?? [])
      .map((skill) => skill.name?.trim() ?? '')
      .filter((name) => name.length > 0)
      .join(' · ');
  }

  summary(): string {
    return this.sheet().summary ?? this.resume()?.summaryApproved ?? this.resume()?.summaryDraft ?? '';
  }

  experiences(): SnapshotExperience[] {
    return this.sheet().experiences ?? [];
  }

  skillGroups(): { label: string; names: string }[] {
    const groups = new Map<string, string[]>();
    for (const skill of this.sheet().skills ?? []) {
      const name = skill.name?.trim();
      if (!name) {
        continue;
      }
      const key = skill.category || 'OTHER';
      groups.set(key, [...(groups.get(key) ?? []), name]);
    }
    return [...groups.entries()].map(([category, names]) => ({
      label: skillCategoryLabel(category),
      names: names.join(', '),
    }));
  }

  education(): SnapshotEducation[] {
    return this.sheet().education ?? [];
  }

  languages(): SnapshotLanguage[] {
    return this.sheet().languages ?? [];
  }

  projects() {
    return this.sheet().projects ?? [];
  }

  certifications() {
    return this.sheet().certifications ?? [];
  }

  period(item: SnapshotExperience): string {
    return formatPeriod(item);
  }

  bullets(item: SnapshotExperience): string[] {
    if (item.detailLevel === 'SUMMARY') {
      return [];
    }
    return linesOf(item.responsibilities);
  }

  educationLine(item: SnapshotEducation): string {
    const years = [item.startYear, item.endYear].filter((year) => year != null).join('–');
    return [item.course, item.institution, educationStatusLabel(item.status), years].filter((part) => !!part).join(' · ');
  }

  level(value: string | null | undefined): string {
    return languageLevelLabel(value);
  }

  trackSkill(_index: number, skill: SnapshotSkill): string {
    return `${skill.category ?? ''}:${skill.name ?? ''}`;
  }

  private async failPreview(err: unknown): Promise<void> {
    const body = await readHttpError(err);
    this.previewError.set(body.message ?? 'Não foi possível pedir o PDF de prévia.');
    this.previewing.set(false);
  }
}
