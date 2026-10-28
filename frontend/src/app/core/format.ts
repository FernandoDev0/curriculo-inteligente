const MONTHS = ['jan.', 'fev.', 'mar.', 'abr.', 'mai.', 'jun.', 'jul.', 'ago.', 'set.', 'out.', 'nov.', 'dez.'];

const QUESTIONS: Record<string, string> = {
  full_name: 'Nome completo',
  target_role: 'Cargo desejado',
  current_profession: 'Profissão atual',
  city_state: 'Cidade e estado',
  email: 'E-mail profissional',
  phone: 'Telefone',
  linkedin: 'LinkedIn',
  other_links: 'Outros links',
  work_mode: 'Disponibilidade',
  target_opportunity: 'Vaga ou empresa de interesse',
  age_choice: 'Idade',
  career_change: 'Mudança de profissão',
  company: 'Empresa',
  role_title: 'Cargo na experiência',
  period: 'Período',
  day_in_the_life: 'Dia de trabalho',
  activities: 'Atividades',
  tools: 'Ferramentas',
  problems: 'Problemas',
  people: 'Pessoas',
  responsibility: 'Responsabilidade',
  autonomy: 'Autonomia',
  results: 'Resultados',
};

const STAGES: Record<string, string> = {
  IDENTIFICATION: 'Identificação',
  EXPERIENCE: 'Experiência',
  EDUCATION: 'Formação',
  SKILLS: 'Competências',
  SUMMARY: 'Resumo',
  REVIEW: 'Revisão',
  TRANSITION: 'Transição',
  PHOTO: 'Foto',
  PREVIEW: 'Prévia',
};

const SETUP: Record<string, { title: string; detail: string }> = {
  KEY_MISSING: { title: 'Chave ausente', detail: 'Nenhuma chave foi configurada.' },
  UNTESTED: { title: 'Ainda não testada', detail: 'A chave está salva e ainda não foi testada.' },
  TESTING: { title: 'Teste em andamento', detail: 'Teste de conexão em andamento.' },
  VALIDATED: { title: 'Conexão validada', detail: 'Conexão validada.' },
  QUOTA: { title: 'Cota', detail: 'A cota da API foi atingida.' },
  INVALID_CREDENTIALS: { title: 'Credencial inválida', detail: 'A chave foi recusada pelo provedor.' },
  CONNECTIVITY: { title: 'Conectividade', detail: 'Não houve conexão com o provedor.' },
  UNAVAILABLE: { title: 'Indisponível', detail: 'O serviço de IA está indisponível.' },
};

export function setupStateView(state: string | null | undefined): { title: string; detail: string } {
  if (!state) {
    return { title: 'Sem leitura', detail: 'A situação da chave ainda não foi lida.' };
  }
  return (
    SETUP[state] ?? {
      title: 'Situação desconhecida',
      detail: 'A API devolveu uma situação que esta tela não reconhece.',
    }
  );
}

export function questionLabel(key: string | null | undefined): string {
  if (!key) {
    return 'Resposta';
  }
  return QUESTIONS[key] ?? key;
}

export function stageLabel(stage: string | null | undefined): string {
  if (!stage) {
    return 'Etapa não informada';
  }
  return STAGES[stage] ?? stage;
}

export function interviewStatusLabel(status: string | null | undefined): string {
  switch (status) {
    case 'IN_PROGRESS':
      return 'Em andamento';
    case 'PAUSED':
      return 'Pausada';
    case 'COMPLETED':
      return 'Concluída';
    default:
      return status || 'Situação não informada';
  }
}

export function resumeStatusLabel(status: string | null | undefined): string {
  switch (status) {
    case 'DRAFT':
      return 'Rascunho';
    case 'APPROVED':
      return 'Aprovado';
    default:
      return status || 'Situação não informada';
  }
}

export function workModeLabel(mode: string | null | undefined): string {
  switch (mode) {
    case 'REMOTE':
      return 'Remoto';
    case 'HYBRID':
      return 'Híbrido';
    case 'ONSITE':
      return 'Presencial';
    case 'FLEXIBLE':
      return 'Flexível';
    default:
      return '';
  }
}

export function educationStatusLabel(status: string | null | undefined): string {
  switch (status) {
    case 'COMPLETED':
      return 'Concluído';
    case 'IN_PROGRESS':
      return 'Em andamento';
    case 'INTERRUPTED':
      return 'Interrompido';
    default:
      return status || '';
  }
}

export function languageLevelLabel(level: string | null | undefined): string {
  switch (level) {
    case 'BASIC':
      return 'básico';
    case 'INTERMEDIATE':
      return 'intermediário';
    case 'ADVANCED':
      return 'avançado';
    case 'FLUENT':
      return 'fluente';
    case 'NATIVE':
      return 'nativo';
    default:
      return 'não informado';
  }
}

export function skillCategoryLabel(category: string | null | undefined): string {
  switch (category) {
    case 'TECHNICAL':
      return 'Técnica';
    case 'TOOL':
      return 'Ferramenta';
    case 'DOMAIN':
      return 'Domínio';
    case 'BEHAVIORAL':
      return 'Comportamental';
    default:
      return category || 'Outras';
  }
}

export function detailPreferenceLabel(value: string | null | undefined): string {
  switch (value) {
    case 'AUTO':
      return 'Automático';
    case 'DETAILED':
      return 'Detalhada';
    case 'SUMMARY':
      return 'Resumida';
    case 'HIDDEN':
      return 'Oculta';
    default:
      return value || 'Automático';
  }
}

export function formatMonthYear(month: number | null | undefined, year: number | null | undefined): string {
  if (year == null) {
    return '';
  }
  if (month == null || month < 1 || month > 12) {
    return String(year);
  }
  return `${MONTHS[month - 1]} ${year}`;
}

export function formatPeriod(item: {
  startMonth?: number | null;
  startYear?: number | null;
  endMonth?: number | null;
  endYear?: number | null;
  currentJob?: boolean | null;
}): string {
  const start = formatMonthYear(item.startMonth, item.startYear);
  if (item.currentJob) {
    return start ? `${start} — atual` : 'Atual';
  }
  if (item.endYear == null) {
    return start;
  }
  const end = formatMonthYear(item.endMonth, item.endYear);
  if (!start) {
    return end;
  }
  return `${start} — ${end}`;
}

export function formatDateTime(value: string | null | undefined): string {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }
  return date.toLocaleString('pt-BR');
}

export function blankToNull(value: string | null | undefined): string | null {
  const trimmed = value?.trim() ?? '';
  return trimmed ? trimmed : null;
}

export function numberOrNull(value: unknown): number | null {
  if (value === null || value === undefined || value === '') {
    return null;
  }
  const parsed = typeof value === 'number' ? value : Number(value);
  if (!Number.isFinite(parsed)) {
    return null;
  }
  return parsed;
}

export function linesOf(value: string | null | undefined): string[] {
  if (!value) {
    return [];
  }
  return value
    .split(/\r?\n|•/u)
    .map((line) => line.trim())
    .filter((line) => line.length > 0);
}

export function safeKeySource(source: string | null | undefined): string {
  if (!source) {
    return 'NONE';
  }
  if (/^[A-Z0-9_]{1,40}$/.test(source)) {
    return source;
  }
  return 'origem registrada';
}
