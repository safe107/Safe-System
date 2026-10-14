export type Translation = Partial<
  Record<
    | "title"
    | "name"
    | "summary"
    | "description"
    | "problem"
    | "solution"
    | "contribution"
    | "architecture"
    | "challenges"
    | "learnings"
    | "results"
    | "role"
    | "responsibilities",
    string
  >
>;
export interface Content {
  id: string;
  slug?: string;
  title?: string;
  name?: string;
  category?: string;
  status?: string;
  published?: boolean;
  enabled?: boolean;
  featured?: boolean;
  displayOrder: number;
  translations?: Record<string, Translation>;
  skillIds?: string[];
  projectIds?: string[];
  mediaIds?: string[];
  fileId?: string;
  locale?: string;
  targetProfile?: string;
  description?: string;
  issuer?: string;
  credentialId?: string;
  verificationUrl?: string;
  hours?: number;
  issuedAt?: string;
  expiresAt?: string;
  organization?: string;
  type?: string;
  current?: boolean;
  startedAt?: string;
  endedAt?: string;
  completedAt?: string;
  isPrimary?: boolean;
  provider?: string;
  sourceUrl?: string;
  progress?: number;
  notes?: string;
  certificateId?: string;
  company?: string;
  position?: string;
  jobUrl?: string;
  workMode?: string;
  salaryMin?: number;
  salaryMax?: number;
  currency?: string;
  appliedAt?: string;
  source?: string;
  resumeId?: string;
  nextAction?: string;
  followUpAt?: string;
  defaultResumeId?: string;
  githubUrl?: string;
  demoUrl?: string;
  createdAt: string;
  updatedAt: string;
}
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface ApiErrorBody {
  message?: string;
  code?: string;
  fields?: Record<string, string>;
}
export type Settings = Record<string, string>;
export function translated(item: Content, locale: string): Translation {
  const fallback = item.translations?.["pt-BR"] ?? {};
  const requested = item.translations?.[locale] ?? {};
  const result: Translation = { ...fallback };
  for (const key of Object.keys(requested) as (keyof Translation)[]) {
    const value = requested[key];
    if (value?.trim()) result[key] = value;
  }
  return result;
}
export function hasTranslationFallback(item: Content, locale: string) {
  if (locale === "pt-BR") return false;
  const fallback = item.translations?.["pt-BR"] ?? {};
  const requested = item.translations?.[locale] ?? {};
  return (Object.keys(fallback) as (keyof Translation)[]).some(
    (key) => fallback[key]?.trim() && !requested[key]?.trim(),
  );
}
export function contentName(item: Content, locale = "pt-BR"): string {
  const t = translated(item, locale);
  return (
    t.title ||
    t.name ||
    t.role ||
    item.title ||
    item.name ||
    item.position ||
    item.slug ||
    ""
  );
}
