import { useSeo } from "../hooks/useSeo";
import { TranslationFallback } from "../components/features/TranslationFallback";
import { Link, useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowRight,
  FileText,
  Network,
  Code2,
  ShieldCheck,
  MapPin,
  ArrowUpRight,
} from "lucide-react";
import { api } from "../services/api/client";
import {
  type Content,
  type Page,
  type Settings,
  contentName,
  translated,
} from "../types/content";
import { ContentCard } from "../components/features/ContentCard";
import { Empty, Failure, Loading } from "../components/ui/Feedback";
export default function Home() {
  const { locale = "pt-BR" } = useParams();
  const { t } = useTranslation();
  const [params] = useSearchParams();
  const profileId = params.get("professionalProfile");
  const settings = useQuery({
    queryKey: ["settings"],
    queryFn: () => api<Settings>("/public/settings"),
  });
  const projects = useQuery({
    queryKey: ["home-projects", profileId],
    queryFn: () =>
      api<Page<Content>>(
        `/public/projects?size=100${profileId ? "&professionalProfile=" + profileId : ""}`,
      ),
  });
  const certs = useQuery({
    queryKey: ["home-certificates"],
    queryFn: () =>
      api<Page<Content>>("/public/certificates?size=3&sort=recent"),
  });
  const experiences = useQuery({
    queryKey: ["home-experiences"],
    queryFn: () => api<Page<Content>>("/public/experiences?size=3"),
  });
  const profiles = useQuery({
    queryKey: ["profiles"],
    queryFn: () => api<Page<Content>>("/public/professional-profiles?size=100"),
  });
  const skills = useQuery({
    queryKey: ["public-skills"],
    queryFn: () => api<Page<Content>>("/public/skills?size=100"),
  });
  const profile = profiles.data?.content.find((p) => p.id === profileId);
  const profileText = profile ? translated(profile, locale) : undefined;
  const s = settings.data;
  const lang = locale === "en" ? "En" : "Pt";
  useSeo(
    s?.seoTitle || s?.name || "SAFE System",
    s?.seoDescription || s?.[`bio${lang}`] || s?.bioPt || "",
  );
  if (settings.isPending) return <Loading />;
  if (settings.isError)
    return <Failure retry={() => void settings.refetch()} />;
  return (
    <>
      <section className="hero">
        <div className="hero-content">
          <div className="eyebrow">
            <span className="dot" />
            {t("heroLabel", { name: s?.name })}
          </div>
          <h1>
            {s?.name}
            <span className="hero-period">.</span>
          </h1>
          <h2>{profileText?.title || s?.[`title${lang}`] || s?.titlePt}</h2>
          {profile && <TranslationFallback item={profile} />}
          <p className="hero-bio">
            {profileText?.summary || s?.[`bio${lang}`] || s?.bioPt}
          </p>
          {s?.location && (
            <p className="muted location">
              <MapPin size={16} />
              {s.location}
            </p>
          )}
          {s?.[`availability${lang}`] && (
            <p className="availability">
              <span className="dot" />
              {s[`availability${lang}`]}
            </p>
          )}
          <div className="actions">
            <Link
              className="button"
              to={`/${locale}/projects${profileId ? "?professionalProfile=" + profileId : ""}`}
            >
              {t("explore")}
              <ArrowRight size={17} />
            </Link>
            <Link
              className="button secondary"
              to={`/${locale}/resume${profile?.defaultResumeId ? "?resumeId=" + profile.defaultResumeId : ""}`}
            >
              <FileText size={17} />
              {t("viewResume")}
            </Link>
          </div>
          <div className="tags hero-skill-tags">
            {skills.data?.content
              .filter(
                (skill) => !profile || profile.skillIds?.includes(skill.id),
              )
              .slice(0, 8)
              .map((skill) => (
                <span key={skill.id} className="badge">
                  {contentName(skill, locale)}
                </span>
              ))}
          </div>
          <div className="hero-links">
            {s?.githubUrl && (
              <a href={s.githubUrl} target="_blank" rel="noreferrer">
                GitHub
                <ArrowUpRight size={14} />
              </a>
            )}
            {s?.linkedinUrl && (
              <a href={s.linkedinUrl} target="_blank" rel="noreferrer">
                LinkedIn
                <ArrowUpRight size={14} />
              </a>
            )}
            <Link to={`/${locale}/certificates`}>
              {t("certificates")}
              <ArrowUpRight size={14} />
            </Link>
          </div>
        </div>
        <div className="profile-visual" aria-hidden="true">
          <div className="orbit orbit-one" />
          <div className="orbit orbit-two" />
          <div className="profile-core">
            <ShieldCheck size={58} strokeWidth={1} />
            <span>SAFE</span>
            <small>PERSONAL CAREER OS</small>
          </div>
          <span className="orbital-label label-one">
            <Code2 size={16} /> DEVELOPMENT
          </span>
          <span className="orbital-label label-two">
            <Network size={16} /> INFRASTRUCTURE
          </span>
          <span className="orbital-label label-three">
            <ShieldCheck size={16} /> SECURITY
          </span>
          <span className="visual-coordinate">JM / CORE PROFILE</span>
        </div>
      </section>
      <section className="section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">01 / {t("areas")}</span>
            <h2>{t("areas")}</h2>
          </div>
        </div>
        <div className="focus-grid">
          {[
            [Network, "support"],
            [Code2, "development"],
            [ShieldCheck, "security"],
          ].map(([Icon, key]) => {
            const I = Icon as typeof Network;
            return (
              <article className="focus-card" key={key as string}>
                <I size={23} />
                <h3>{t(key as string)}</h3>
              </article>
            );
          })}
        </div>
      </section>
      <section className="section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">02 / {t("projects")}</span>
            <h2>{t("featured")}</h2>
          </div>
          <Link className="text-link" to={`/${locale}/projects`}>
            {t("allProjects")}
            <ArrowRight size={16} />
          </Link>
        </div>
        {projects.isPending ? (
          <Loading />
        ) : projects.isError ? (
          <Failure retry={() => void projects.refetch()} />
        ) : projects.data.content.filter((p) => p.featured || !!profileId)
            .length ? (
          <div className="card-grid">
            {projects.data.content
              .filter((p) => p.featured || !!profileId)
              .slice(0, 3)
              .map((p) => (
                <ContentCard key={p.id} item={p} kind="projects" />
              ))}
          </div>
        ) : (
          <Empty />
        )}
      </section>
      <section className="section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">03 / {t("experience")}</span>
            <h2>{t("experience")}</h2>
          </div>
          <Link className="text-link" to={`/${locale}/experience`}>
            {t("viewAll")}
            <ArrowRight size={16} />
          </Link>
        </div>
        {experiences.isPending ? (
          <Loading />
        ) : experiences.isError ? (
          <Failure retry={() => void experiences.refetch()} />
        ) : experiences.data.content.length ? (
          <div className="timeline">
            {experiences.data.content.map((e) => (
              <article key={e.id}>
                <span className="timeline-dot" />
                <div className="eyebrow">{e.organization}</div>
                <h3>{contentName(e, locale)}</h3>
                <TranslationFallback item={e} />
                <p>{translated(e, locale).description}</p>
              </article>
            ))}
          </div>
        ) : (
          <Empty />
        )}
      </section>
      <section className="section">
        <div className="section-heading">
          <div>
            <span className="eyebrow">04 / {t("certificates")}</span>
            <h2>{t("recentCertificates")}</h2>
          </div>
          <Link className="text-link" to={`/${locale}/certificates`}>
            {t("allCertificates")}
            <ArrowRight size={16} />
          </Link>
        </div>
        {certs.isPending ? (
          <Loading />
        ) : certs.isError ? (
          <Failure retry={() => void certs.refetch()} />
        ) : certs.data.content.length ? (
          <div className="card-grid">
            {certs.data.content.map((c) => (
              <ContentCard key={c.id} item={c} kind="certificates" />
            ))}
          </div>
        ) : (
          <Empty />
        )}
      </section>
      <section className="contact-callout">
        <div>
          <span className="eyebrow">05 / {t("contact")}</span>
          <h2>{t("connect")}</h2>
        </div>
        <Link className="button secondary" to={`/${locale}/contact`}>
          {t("contact")}
          <ArrowUpRight size={18} />
        </Link>
      </section>
    </>
  );
}
