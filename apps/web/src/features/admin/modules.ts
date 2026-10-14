export interface Field {
  key: string;
  label: string;
  type?:
    | "text"
    | "textarea"
    | "url"
    | "date"
    | "number"
    | "checkbox"
    | "select"
    | "skills"
    | "projects"
    | "file"
    | "media"
    | "resumes"
    | "certificates";
  required?: boolean;
  options?: string[];
  min?: number;
  max?: number;
}
export interface Module {
  name: string;
  fields: Field[];
  translations?: string[];
  publication?: boolean;
}
const f = (
  key: string,
  label: string,
  type: Field["type"] = "text",
  required = false,
): Field => ({ key, label, type, required });
const slug = f("slug", "Slug único (minúsculas e hífens)", "text", true);
const category: Field = {
  key: "category",
  label: "Categoria",
  type: "select",
  required: true,
  options: [
    "backend",
    "frontend",
    "fullStack",
    "automation",
    "infrastructure",
    "academic",
  ],
};
const order = f("displayOrder", "Ordem", "number");
const pub = f("published", "Publicado", "checkbox");
const skills = f("skillIds", "Competências relacionadas", "skills");
const status = (options: string[]): Field => ({
  key: "status",
  label: "Status",
  type: "select",
  required: true,
  options,
});
export const modules: Record<string, Module> = {
  projects: {
    name: "Projetos",
    publication: true,
    translations: [
      "title",
      "summary",
      "description",
      "problem",
      "solution",
      "contribution",
      "architecture",
      "challenges",
      "learnings",
      "results",
    ],
    fields: [
      slug,
      category,
      status(["planned", "in-progress", "completed", "archived"]),
      f("githubUrl", "GitHub", "url"),
      f("demoUrl", "Demonstração", "url"),
      f("featured", "Destaque", "checkbox"),
      pub,
      order,
      f("startedAt", "Início", "date"),
      f("completedAt", "Conclusão", "date"),
      skills,
      f("mediaIds", "Imagens do projeto", "media"),
    ],
  },
  certificates: {
    name: "Certificados",
    publication: true,
    translations: ["title", "description"],
    fields: [
      slug,
      f("issuer", "Instituição emissora", "text", true),
      category,
      f("issuedAt", "Emissão", "date"),
      f("expiresAt", "Expiração", "date"),
      { ...f("hours", "Carga horária", "number"), min: 1, max: 10000 },
      f("credentialId", "Identificador da credencial"),
      f("verificationUrl", "Validação", "url"),
      f("fileId", "Documento PDF", "file"),
      pub,
      order,
      skills,
    ],
  },
  experiences: {
    name: "Experiências",
    publication: true,
    translations: ["role", "description", "responsibilities", "results"],
    fields: [
      f("organization", "Organização", "text", true),
      {
        key: "type",
        label: "Modalidade",
        type: "select",
        required: true,
        options: ["professional", "academic", "volunteer", "leadership"],
      },
      f("startedAt", "Início", "date"),
      f("endedAt", "Término", "date"),
      f("current", "Experiência atual", "checkbox"),
      pub,
      order,
      skills,
    ],
  },
  skills: {
    name: "Competências",
    publication: true,
    translations: ["name", "description"],
    fields: [
      slug,
      {
        key: "category",
        label: "Grupo",
        type: "select",
        required: true,
        options: [
          "languages",
          "frameworks",
          "databases",
          "development",
          "infrastructure",
          "tools",
          "interpersonal",
        ],
      },
      pub,
      order,
    ],
  },
  resumes: {
    name: "Currículos",
    publication: true,
    fields: [
      f("title", "Título", "text", true),
      {
        key: "locale",
        label: "Idioma",
        type: "select",
        required: true,
        options: ["pt-BR", "en"],
      },
      f("targetProfile", "Perfil-alvo"),
      f("description", "Descrição", "textarea"),
      f("fileId", "Currículo PDF", "file"),
      f("isPrimary", "Principal neste idioma", "checkbox"),
      pub,
      order,
    ],
  },
  learning: {
    name: "Learning Center",
    fields: [
      f("name", "Curso", "text", true),
      f("provider", "Plataforma ou instituição", "text", true),
      f("category", "Categoria"),
      f("sourceUrl", "URL", "url"),
      status(["planned", "in-progress", "completed", "paused"]),
      { ...f("progress", "Progresso (%)", "number"), min: 0, max: 100 },
      f("startedAt", "Início", "date"),
      f("completedAt", "Conclusão", "date"),
      f("notes", "Observações privadas", "textarea"),
      f("certificateId", "Certificado relacionado", "certificates"),
      order,
      skills,
    ],
  },
  applications: {
    name: "Application Tracker",
    fields: [
      f("company", "Empresa", "text", true),
      f("position", "Cargo", "text", true),
      f("jobUrl", "Link da vaga", "url"),
      f("workMode", "Modalidade"),
      f("salaryMin", "Salário mínimo", "number"),
      f("salaryMax", "Salário máximo", "number"),
      f("currency", "Moeda (ex.: BRL)"),
      f("appliedAt", "Data da candidatura", "date"),
      status([
        "interest",
        "sent",
        "screening",
        "interview",
        "technical-test",
        "offer",
        "hired",
        "rejected",
        "withdrawn",
      ]),
      f("source", "Fonte"),
      f("resumeId", "Currículo utilizado", "resumes"),
      f("notes", "Anotações privadas", "textarea"),
      f("nextAction", "Próxima ação"),
      f("followUpAt", "Acompanhamento", "date"),
      order,
    ],
  },
  profiles: {
    name: "Perfis profissionais",
    translations: ["title", "summary"],
    fields: [
      slug,
      f("enabled", "Ativo", "checkbox"),
      f("defaultResumeId", "Currículo associado", "resumes"),
      order,
      skills,
      f("projectIds", "Projetos em destaque", "projects"),
    ],
  },
};
export function endpoint(module: string) {
  return module === "learning" ? "courses" : module;
}
export const translationLabels: Record<string, string> = {
  title: "Título",
  name: "Nome",
  role: "Cargo ou função",
  summary: "Resumo",
  description: "Descrição",
  problem: "Problema",
  solution: "Solução",
  contribution: "Minha contribuição",
  architecture: "Arquitetura técnica",
  challenges: "Desafios",
  learnings: "Aprendizados",
  results: "Resultados verificáveis",
  responsibilities: "Responsabilidades",
};
