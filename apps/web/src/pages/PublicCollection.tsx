import { TranslationFallback } from "../components/features/TranslationFallback";
import { useSeo } from "../hooks/useSeo";
import { useParams, Link, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { Download, ExternalLink } from "lucide-react";
import { api, downloadUrl } from "../services/api/client";
import {
  contentName,
  translated,
  type Content,
  type Page,
} from "../types/content";
import { Empty, Failure, Loading } from "../components/ui/Feedback";
export default function PublicCollection({
  kind,
}: {
  kind: "experience" | "skills" | "resume";
}) {
  const { locale = "pt-BR" } = useParams();
  const { t } = useTranslation();
  const [params] = useSearchParams();
  useSeo(t(kind));
  const endpoint = {
    experience: "experiences",
    skills: "skills",
    resume: "resumes",
  }[kind];
  const result = useQuery({
    queryKey: [endpoint, locale],
    queryFn: () =>
      api<Page<Content>>(
        `/public/${endpoint}?size=100${kind === "resume" ? "&locale=" + locale : ""}`,
      ),
  });
  const evidence = useQuery({
    queryKey: ["skill-evidence"],
    queryFn: () => api<Page<Content>>("/public/projects?size=100"),
    enabled: kind === "skills",
  });
  const certificateEvidence = useQuery({
    queryKey: ["skill-certificate-evidence"],
    queryFn: () => api<Page<Content>>("/public/certificates?size=100"),
    enabled: kind === "skills",
  });
  const experienceEvidence = useQuery({
    queryKey: ["skill-experience-evidence"],
    queryFn: () => api<Page<Content>>("/public/experiences?size=100"),
    enabled: kind === "skills",
  });
  const label = {
    experience: "timeline",
    skills: "skillMatrix",
    resume: "resumeExport",
  }[kind];
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">{t(label)}</span>
        <h1>{t(kind)}</h1>
      </div>
      {result.isPending ? (
        <Loading />
      ) : result.isError ? (
        <Failure retry={() => void result.refetch()} />
      ) : !result.data.content.length ? (
        <Empty />
      ) : kind === "experience" ? (
        <div className="timeline">
          {result.data.content.map((item) => {
            const text = translated(item, locale);
            return (
              <article key={item.id} id={item.id}>
                <span className="timeline-dot" />
                <div className="eyebrow">
                  {item.organization} · {item.type}
                </div>
                <h2>{text.role}</h2>
                <TranslationFallback item={item} />
                <p className="technical">
                  {item.startedAt
                    ? new Intl.DateTimeFormat(locale, {
                        month: "short",
                        year: "numeric",
                        timeZone: "UTC",
                      }).format(new Date(item.startedAt))
                    : t("noDates")}
                  {item.current
                    ? " — " + t("current")
                    : item.endedAt
                      ? " — " +
                        new Intl.DateTimeFormat(locale, {
                          month: "short",
                          year: "numeric",
                          timeZone: "UTC",
                        }).format(new Date(item.endedAt))
                      : ""}
                </p>
                <p>{text.description}</p>
                {text.responsibilities && (
                  <p className="prewrap">{text.responsibilities}</p>
                )}
                {text.results && <p>{text.results}</p>}
              </article>
            );
          })}
        </div>
      ) : kind === "skills" ? (
        <div className="skill-groups">
          {Array.from(new Set(result.data.content.map((s) => s.category))).map(
            (category) => (
              <section className="panel" key={category}>
                <h2>{category ? t(category) : ""}</h2>
                {result.data.content
                  .filter((s) => s.category === category)
                  .map((s) => (
                    <article className="skill-row" key={s.id}>
                      <h3>{contentName(s, locale)}</h3>
                      <TranslationFallback item={s} />
                      <p>{translated(s, locale).description}</p>
                      <div className="tags">
                        {certificateEvidence.data?.content
                          .filter((c) => c.skillIds?.includes(s.id))
                          .map((c) => (
                            <Link
                              key={c.id}
                              className="badge"
                              to={`/${locale}/certificates/${c.slug}`}
                            >
                              {contentName(c, locale)}
                            </Link>
                          ))}
                        {experienceEvidence.data?.content
                          .filter((e) => e.skillIds?.includes(s.id))
                          .map((e) => (
                            <Link
                              key={e.id}
                              className="badge"
                              to={`/${locale}/experience#${e.id}`}
                            >
                              {e.organization} · {contentName(e, locale)}
                            </Link>
                          ))}
                        {evidence.data?.content
                          .filter((p) => p.skillIds?.includes(s.id))
                          .map((p) => (
                            <Link
                              className="badge"
                              key={p.id}
                              to={`/${locale}/projects/${p.slug}`}
                            >
                              {contentName(p, locale)}
                            </Link>
                          ))}
                      </div>
                    </article>
                  ))}
              </section>
            ),
          )}
        </div>
      ) : (
        <div className="resume-grid">
          {result.data.content
            .filter((r) => r.locale === locale)
            .sort(
              (a, b) =>
                Number(b.id === params.get("resumeId") || b.isPrimary) -
                Number(a.id === params.get("resumeId") || a.isPrimary),
            )
            .map((r) => (
              <article className="panel resume-card" key={r.id}>
                <span className="eyebrow">
                  {r.locale} / {r.targetProfile}
                </span>
                <h2>{r.title}</h2>
                {r.isPrimary && <span className="badge">{t("primary")}</span>}
                <p>{r.description}</p>
                {r.fileId && (
                  <div className="actions">
                    <a className="button" href={downloadUrl(r.fileId)}>
                      {t("download")}
                      <Download size={16} />
                    </a>
                    <a
                      className="button secondary"
                      href={downloadUrl(r.fileId, false, true)}
                      target="_blank"
                      rel="noreferrer"
                    >
                      {t("preview")}
                      <ExternalLink size={16} />
                    </a>
                  </div>
                )}
              </article>
            ))}
        </div>
      )}
    </>
  );
}
