import { useSeo } from "../hooks/useSeo";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { ArrowLeft, ExternalLink, Download } from "lucide-react";
import { api, downloadUrl } from "../services/api/client";
import {
  hasTranslationFallback,
  contentName,
  translated,
  type Content,
  type Page,
} from "../types/content";
import { Failure, Loading } from "../components/ui/Feedback";
export default function PublicDetails({
  kind,
}: {
  kind: "projects" | "certificates";
}) {
  const { locale = "pt-BR", slug } = useParams();
  const { t } = useTranslation();
  const result = useQuery({
    queryKey: [kind, slug],
    queryFn: () => api<Content>(`/public/${kind}/${slug}`),
  });
  const skills = useQuery({
    queryKey: ["public-skills"],
    queryFn: () => api<Page<Content>>("/public/skills?size=100"),
  });
  useSeo(
    result.data ? contentName(result.data, locale) : t(kind),
    result.data
      ? translated(result.data, locale).summary ||
          translated(result.data, locale).description ||
          ""
      : "",
  );
  if (result.isPending) return <Loading />;
  if (result.isError) return <Failure retry={() => void result.refetch()} />;
  const item = result.data;
  const text = translated(item, locale);
  const fields = [
    "summary",
    "description",
    "problem",
    "solution",
    "contribution",
    "architecture",
    "challenges",
    "learnings",
    "results",
  ] as const;
  const date = (value: string) =>
    new Intl.DateTimeFormat(locale, {
      dateStyle: "medium",
      timeZone: "UTC",
    }).format(new Date(value));
  return (
    <>
      <Link className="text-link" to={`/${locale}/${kind}`}>
        <ArrowLeft size={16} />
        {t("back")}
      </Link>
      <div className="page-heading">
        <span className="eyebrow">{item.category || item.issuer}</span>
        <h1>{contentName(item, locale)}</h1>
        {hasTranslationFallback(item, locale) && (
          <p className="notice">{t("fallback")}</p>
        )}
      </div>
      <div className="detail-layout">
        <article className="prose">
          {fields.map(
            (f) =>
              text[f] && (
                <section key={f}>
                  <h2>{t(f)}</h2>
                  <p>{text[f]}</p>
                </section>
              ),
          )}
          {item.mediaIds?.map((id) => (
            <img
              className="gallery-image"
              loading="lazy"
              key={id}
              src={downloadUrl(id, false, true)}
              alt={contentName(item, locale)}
            />
          ))}
          {kind === "certificates" && item.fileId && (
            <div className="actions">
              <a
                className="button secondary"
                href={downloadUrl(item.fileId, false, true)}
                target="_blank"
                rel="noreferrer"
              >
                {t("preview")}
                <ExternalLink size={16} />
              </a>
              <a className="button" href={downloadUrl(item.fileId)}>
                {t("download")}
                <Download size={16} />
              </a>
            </div>
          )}
        </article>
        <aside className="detail-sidebar">
          <h2>{t("skillsLinked")}</h2>
          <div className="tags">
            {skills.data?.content
              .filter((s) => item.skillIds?.includes(s.id))
              .map((s) => (
                <span className="badge" key={s.id}>
                  {contentName(s, locale)}
                </span>
              ))}
          </div>
          {item.status && (
            <p>
              {t("status")}: {t(item.status)}
            </p>
          )}
          {item.startedAt && (
            <p>
              {t("started")}: {date(item.startedAt)}
            </p>
          )}
          {item.completedAt && (
            <p>
              {t("completedDate")}: {date(item.completedAt)}
            </p>
          )}
          {item.credentialId && (
            <p>
              {t("credential")}: {item.credentialId}
            </p>
          )}
          {item.issuedAt && (
            <p>
              {t("issued")}: {date(item.issuedAt)}
            </p>
          )}
          {item.expiresAt && (
            <p>
              {t("expires")}: {date(item.expiresAt)}
            </p>
          )}
          {item.hours && (
            <p>
              {t("hours")}: {item.hours}
            </p>
          )}
          {item.githubUrl && (
            <a
              className="text-link"
              href={item.githubUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t("github")}
              <ExternalLink size={16} />
            </a>
          )}
          {item.demoUrl && (
            <a
              className="text-link"
              href={item.demoUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t("demo")}
              <ExternalLink size={16} />
            </a>
          )}
          {item.verificationUrl && (
            <a
              className="text-link"
              href={item.verificationUrl}
              target="_blank"
              rel="noreferrer"
            >
              {t("validation")}
              <ExternalLink size={16} />
            </a>
          )}
        </aside>
      </div>
    </>
  );
}
