import { useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Save, ArrowLeft, Upload, Download, X } from "lucide-react";
import {
  modules,
  endpoint,
  translationLabels,
  type Module,
} from "../features/admin/modules";
import { api, downloadUrl, ApiError } from "../services/api/client";
import { contentName, type Content, type Page } from "../types/content";
import { Loading, Failure } from "../components/ui/Feedback";
type Values = Record<string, string | boolean | string[]>;
interface Event {
  id: string;
  previousStatus: string;
  newStatus: string;
  notes?: string;
  occurredAt: string;
}
export default function AdminEditor({ module }: { module: string }) {
  const { id } = useParams();
  const result = useQuery({
    queryKey: ["admin", endpoint(module), id],
    queryFn: () => api<Content>(`/admin/${endpoint(module)}/${id}`),
    enabled: !!id,
  });
  if (id && result.isPending) return <Loading />;
  if (id && result.isError)
    return <Failure retry={() => void result.refetch()} />;
  return <EditorForm key={id || "new"} module={module} item={result.data} />;
}
function schemaFor(config: Module) {
  const shape: Record<
    string,
    z.ZodType<string | boolean | string[], string | boolean | string[]>
  > = {};
  config.fields.forEach((f) => {
    if (["skills", "projects", "media"].includes(f.type || ""))
      shape[f.key] = z.array(z.string());
    else if (f.type === "checkbox") shape[f.key] = z.boolean();
    else {
      let validator = z.string().max(f.type === "textarea" ? 12000 : 1000);
      if (f.required) validator = validator.min(1, "Campo obrigatório");
      if (f.key === "slug")
        validator = validator.regex(
          /^[a-z0-9]+(?:-[a-z0-9]+)*$/,
          "Use letras minúsculas, números e hífens",
        );
      shape[f.key] =
        f.type === "url"
          ? validator.refine(
              (v) => !v || /^https:\/\/[^\s]+$/.test(v),
              "Informe uma URL HTTPS",
            )
          : validator;
    }
  });
  config.translations?.forEach((field, index) => {
    shape[`pt_${field}`] =
      index === 0
        ? z.string().min(1, "Campo obrigatório").max(12000)
        : z.string().max(12000);
    shape[`en_${field}`] = z.string().max(12000);
  });
  return z.object(shape);
}
function EditorForm({ module, item }: { module: string; item?: Content }) {
  const config = modules[module];
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");
  const [saveError, setSaveError] = useState("");
  const cache = useQueryClient();
  const navigate = useNavigate();
  const raw: Record<string, unknown> = item
    ? Object.fromEntries(Object.entries(item))
    : {};
  const defaults: Values = {};
  config.fields.forEach((f) => {
    const value = raw[f.key];
    defaults[f.key] = ["skills", "projects", "media"].includes(f.type || "")
      ? Array.isArray(value)
        ? (value as string[])
        : []
      : f.type === "checkbox"
        ? Boolean(value)
        : value == null
          ? f.key === "displayOrder"
            ? "0"
            : f.type === "select"
              ? f.options?.[0] || ""
              : ""
          : String(value);
  });
  config.translations?.forEach((f) => {
    defaults[`pt_${f}`] =
      item?.translations?.["pt-BR"]?.[
        f as keyof NonNullable<Content["translations"]>[string]
      ] || "";
    defaults[`en_${f}`] =
      item?.translations?.en?.[
        f as keyof NonNullable<Content["translations"]>[string]
      ] || "";
  });
  const form = useForm<Values>({
    defaultValues: defaults,
    resolver: zodResolver(schemaFor(config)),
  });
  const relatedSkills = useQuery({
    queryKey: ["admin", "skills", "lookup"],
    queryFn: () => api<Page<Content>>("/admin/skills?size=100"),
    enabled: config.fields.some((f) => f.type === "skills"),
  });
  const relatedProjects = useQuery({
    queryKey: ["admin", "projects", "lookup"],
    queryFn: () => api<Page<Content>>("/admin/projects?size=100"),
    enabled: config.fields.some((f) => f.type === "projects"),
  });
  const relatedResumes = useQuery({
    queryKey: ["admin", "resumes", "lookup"],
    queryFn: () => api<Page<Content>>("/admin/resumes?size=100"),
    enabled: config.fields.some((f) => f.type === "resumes"),
  });
  const relatedCertificates = useQuery({
    queryKey: ["admin", "certificates", "lookup"],
    queryFn: () => api<Page<Content>>("/admin/certificates?size=100"),
    enabled: config.fields.some((f) => f.type === "certificates"),
  });
  const related = {
    skills: relatedSkills,
    projects: relatedProjects,
    resumes: relatedResumes,
    certificates: relatedCertificates,
  };
  const history = useQuery({
    queryKey: ["application-history", item?.id],
    queryFn: () => api<Event[]>(`/admin/applications/${item?.id}/events`),
    enabled: module === "applications" && !!item,
  });
  const save = useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      api<Content>(`/admin/${endpoint(module)}${item ? "/" + item.id : ""}`, {
        method: item ? "PUT" : "POST",
        body: JSON.stringify(data),
      }),
    onSuccess: () => {
      void cache.invalidateQueries();
      navigate(`/admin/${module}`);
    },
    onError: (e) => {
      setSaveError(e.message);
      if (e instanceof ApiError)
        Object.entries(e.body.fields || {}).forEach(([key, message]) =>
          form.setError(key, { message }),
        );
    },
  });
  async function upload(
    files: FileList | null,
    field: string,
    multiple: boolean,
  ) {
    if (!files?.length) return;
    setUploading(true);
    setUploadError("");
    try {
      for (const file of Array.from(files)) {
        const body = new FormData();
        body.append("file", file);
        const metadata = await api<{ id: string }>("/admin/files", {
          method: "POST",
          body,
        });
        form.setValue(
          field,
          multiple
            ? [...(form.getValues(field) as string[]), metadata.id]
            : metadata.id,
          { shouldDirty: true, shouldValidate: true },
        );
      }
    } catch (e) {
      setUploadError(e instanceof Error ? e.message : "Falha no upload");
    } finally {
      setUploading(false);
    }
  }
  function submit(values: Values) {
    setSaveError("");
    const body: Record<string, unknown> = {};
    config.fields.forEach((f) => {
      const v = values[f.key];
      body[f.key] =
        f.type === "number"
          ? v === ""
            ? null
            : Number(v)
          : ["checkbox", "skills", "projects", "media"].includes(f.type || "")
            ? v
            : v === ""
              ? null
              : v;
    });
    if (config.translations?.length) {
      const translations: Record<string, Record<string, string>> = {
        "pt-BR": {},
      };
      for (const f of config.translations)
        translations["pt-BR"][f] = String(values[`pt_${f}`] || "");
      if (config.translations.some((f) => values[`en_${f}`])) {
        translations.en = {};
        for (const f of config.translations)
          translations.en[f] = String(values[`en_${f}`] || "");
      }
      body.translations = translations;
    }
    save.mutate(body);
  }
  const watched = useWatch({ control: form.control }) as Values;
  return (
    <>
      <Link className="text-link" to={`/admin/${module}`}>
        <ArrowLeft size={16} />
        {config.name}
      </Link>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / EDITOR</span>
          <h1>
            {item ? "Editar" : "Adicionar"} · {config.name}
          </h1>
        </div>
      </div>
      <form className="editor" onSubmit={form.handleSubmit(submit)}>
        <section className="panel">
          <h2>Informações e publicação</h2>
          <div className="form-grid">
            {config.fields.map((f) => {
              const choices =
                f.type && f.type in related
                  ? related[f.type as keyof typeof related]
                  : undefined;
              return (
                <div
                  className={
                    f.type === "textarea" ||
                    f.type === "file" ||
                    f.type === "media"
                      ? "form-field wide"
                      : "form-field"
                  }
                  key={f.key}
                >
                  <label htmlFor={f.key}>
                    {f.label}
                    {f.required ? " *" : ""}
                  </label>
                  {f.type === "checkbox" ? (
                    <input
                      id={f.key}
                      type="checkbox"
                      {...form.register(f.key)}
                    />
                  ) : f.type === "textarea" ? (
                    <textarea id={f.key} rows={4} {...form.register(f.key)} />
                  ) : f.type === "select" ? (
                    <select id={f.key} {...form.register(f.key)}>
                      {f.options?.map((o) => (
                        <option key={o}>{o}</option>
                      ))}
                    </select>
                  ) : choices ? (
                    <>
                      <select
                        id={f.key}
                        multiple={f.type === "skills" || f.type === "projects"}
                        {...form.register(f.key)}
                      >
                        {(f.type === "resumes" ||
                          f.type === "certificates") && (
                          <option value="">Nenhum</option>
                        )}
                        {choices.data?.content.map((c) => (
                          <option key={c.id} value={c.id}>
                            {contentName(c)}
                          </option>
                        ))}
                      </select>
                      {choices.isError && (
                        <p role="alert">
                          Não foi possível carregar os vínculos.
                        </p>
                      )}
                      {(f.type === "skills" || f.type === "projects") && (
                        <small className="muted">
                          Seleção múltipla: Ctrl/Cmd + clique.
                        </small>
                      )}
                    </>
                  ) : f.type === "file" || f.type === "media" ? (
                    <div className="file-field">
                      <input
                        id={f.key}
                        type="file"
                        accept={
                          f.type === "file"
                            ? "application/pdf"
                            : ".jpg,.jpeg,.png,.webp"
                        }
                        multiple={f.type === "media"}
                        disabled={uploading}
                        onChange={(e) =>
                          void upload(e.target.files, f.key, f.type === "media")
                        }
                      />
                      <small className="muted">
                        <Upload size={13} />
                        Máximo 10 MB por arquivo. Armazenamento privado.
                      </small>
                      {(f.type === "media"
                        ? (watched[f.key] as string[])
                        : watched[f.key]
                          ? [String(watched[f.key])]
                          : []
                      ).map((fileId) => (
                        <div className="file-entry" key={fileId}>
                          <a
                            className="text-link"
                            href={downloadUrl(fileId, true)}
                            target="_blank"
                            rel="noreferrer"
                          >
                            <Download size={14} />
                            Arquivo anexado
                          </a>
                          <button
                            type="button"
                            className="icon-button"
                            aria-label="Desvincular arquivo"
                            onClick={() =>
                              form.setValue(
                                f.key,
                                f.type === "media"
                                  ? (watched[f.key] as string[]).filter(
                                      (v) => v !== fileId,
                                    )
                                  : "",
                                { shouldDirty: true },
                              )
                            }
                          >
                            <X size={15} />
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <input
                      id={f.key}
                      type={
                        f.type === "date"
                          ? "date"
                          : f.type === "number"
                            ? "number"
                            : "text"
                      }
                      min={f.min ?? 0}
                      max={f.max}
                      step={f.key.startsWith("salary") ? "0.01" : "1"}
                      {...form.register(f.key)}
                    />
                  )}{" "}
                  {form.formState.errors[f.key] && (
                    <p className="error-message" role="alert">
                      {String(form.formState.errors[f.key]?.message)}
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </section>
        {config.translations &&
          (["pt", "en"] as const).map((lang) => (
            <section className="panel" key={lang}>
              <h2>
                {lang === "pt"
                  ? "Português brasileiro (obrigatório)"
                  : "English (opcional)"}
              </h2>
              <div className="form-grid">
                {config.translations?.map((f, index) => {
                  const key = `${lang}_${f}`;
                  return (
                    <div className="form-field wide" key={f}>
                      <label htmlFor={key}>
                        {translationLabels[f]}
                        {lang === "pt" && index === 0 ? " *" : ""}
                      </label>
                      {index === 0 ? (
                        <input id={key} {...form.register(key)} />
                      ) : (
                        <textarea
                          id={key}
                          rows={f === "summary" ? 2 : 4}
                          {...form.register(key)}
                        />
                      )}{" "}
                      {form.formState.errors[key] && (
                        <p role="alert" className="error-message">
                          {String(form.formState.errors[key]?.message)}
                        </p>
                      )}
                    </div>
                  );
                })}
              </div>
            </section>
          ))}
        {uploadError && (
          <p role="alert" className="error-message">
            {uploadError}
          </p>
        )}
        {uploading && <p role="status">Enviando arquivo…</p>}
        {saveError && (
          <p role="alert" className="error-message">
            {saveError}
          </p>
        )}
        <div className="editor-actions">
          <Link className="button secondary" to={`/admin/${module}`}>
            Cancelar
          </Link>
          <button className="button" disabled={uploading || save.isPending}>
            <Save size={17} />
            {save.isPending ? "Salvando…" : "Salvar"}
          </button>
        </div>
      </form>
      {module === "applications" && item && (
        <section className="panel">
          <h2>Histórico de etapas</h2>
          {history.isPending ? (
            <Loading />
          ) : history.isError ? (
            <Failure retry={() => void history.refetch()} />
          ) : (
            history.data.map((event) => (
              <div className="dashboard-row" key={event.id}>
                <div>
                  {event.previousStatus || "Criação"} → {event.newStatus}
                  {event.notes && <p>{event.notes}</p>}
                </div>
                <time>
                  {new Intl.DateTimeFormat("pt-BR", {
                    dateStyle: "short",
                    timeStyle: "short",
                  }).format(new Date(event.occurredAt))}
                </time>
              </div>
            ))
          )}
        </section>
      )}
    </>
  );
}
