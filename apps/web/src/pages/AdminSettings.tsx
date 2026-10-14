import { useForm } from "react-hook-form";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "../services/api/client";
import type { Settings } from "../types/content";
import { Loading, Failure } from "../components/ui/Feedback";
import { Save } from "lucide-react";
const fields: Record<string, string> = {
  name: "Nome profissional",
  titlePt: "Título (PT-BR)",
  titleEn: "Título (EN)",
  bioPt: "Bio (PT-BR)",
  bioEn: "Bio (EN)",
  location: "Localização",
  email: "E-mail",
  githubUrl: "GitHub",
  linkedinUrl: "LinkedIn",
  availabilityPt: "Disponibilidade (PT-BR)",
  availabilityEn: "Disponibilidade (EN)",
  seoTitle: "Título SEO",
  seoDescription: "Descrição SEO",
};
export default function AdminSettings() {
  const result = useQuery({
    queryKey: ["admin-settings"],
    queryFn: () => api<Settings>("/admin/settings"),
  });
  if (result.isPending) return <Loading />;
  if (result.isError) return <Failure retry={() => void result.refetch()} />;
  return <SettingsForm settings={result.data} />;
}
function SettingsForm({ settings }: { settings: Settings }) {
  const defaults: Record<string, string | boolean> = {};
  Object.keys(fields).forEach((k) => (defaults[k] = settings[k] || ""));
  defaults.defaultLocale = settings.defaultLocale || "pt-BR";
  defaults.analyticsEnabled = settings.analyticsEnabled === "true";
  const form = useForm({ defaultValues: defaults });
  const cache = useQueryClient();
  const save = useMutation({
    mutationFn: (data: Record<string, string | boolean>) =>
      api<Settings>("/admin/settings", {
        method: "PUT",
        body: JSON.stringify(data),
      }),
    onSuccess: () => {
      void cache.invalidateQueries();
    },
  });
  return (
    <>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / SETTINGS</span>
          <h1>Configurações</h1>
        </div>
      </div>
      <form
        className="panel editor"
        onSubmit={form.handleSubmit((data) => save.mutate(data))}
      >
        <div className="form-grid">
          {Object.entries(fields).map(([key, label]) => (
            <div
              className={
                key.startsWith("bio") ? "form-field wide" : "form-field"
              }
              key={key}
            >
              <label htmlFor={key}>{label}</label>
              {key.startsWith("bio") || key === "seoDescription" ? (
                <textarea id={key} rows={4} {...form.register(key)} />
              ) : (
                <input
                  id={key}
                  type={key === "email" ? "email" : "text"}
                  {...form.register(key, { required: key === "name" })}
                />
              )}
            </div>
          ))}
          <div className="form-field">
            <label htmlFor="defaultLocale">Idioma padrão</label>
            <select id="defaultLocale" {...form.register("defaultLocale")}>
              <option value="pt-BR">Português</option>
              <option value="en">English</option>
            </select>
          </div>
          <div className="form-field">
            <label htmlFor="analyticsEnabled">
              Habilitar analytics agregado
            </label>
            <input
              id="analyticsEnabled"
              type="checkbox"
              {...form.register("analyticsEnabled")}
            />
            <p className="muted">
              Sem cookies de rastreamento e sem armazenamento de IP. Eventos
              agregados por dia e página.
            </p>
          </div>
        </div>
        {save.isError && (
          <p role="alert" className="error-message">
            {save.error.message}
          </p>
        )}
        {save.isSuccess && <p role="status">Configurações salvas.</p>}
        <button className="button" disabled={save.isPending}>
          <Save size={17} />
          Salvar configurações
        </button>
      </form>
    </>
  );
}
