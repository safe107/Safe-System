import { useSeo } from "../hooks/useSeo";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { Mail, Copy, ArrowUpRight, MapPin } from "lucide-react";
import { api } from "../services/api/client";
import type { Settings } from "../types/content";
import { Failure, Loading } from "../components/ui/Feedback";
export default function Contact() {
  const { t } = useTranslation();
  useSeo(t("contact"));
  const [feedback, setFeedback] = useState("");
  const settings = useQuery({
    queryKey: ["settings"],
    queryFn: () => api<Settings>("/public/settings"),
  });
  if (settings.isPending) return <Loading />;
  if (settings.isError)
    return <Failure retry={() => void settings.refetch()} />;
  const s = settings.data;
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">{t("connect")}</span>
        <h1>{t("contact")}</h1>
      </div>
      <section className="panel contact-panel">
        <h2>{t("connect")}</h2>
        {s.email ? (
          <>
            <a className="contact-email" href={`mailto:${s.email}`}>
              <Mail />
              {s.email}
            </a>
            <button
              className="button secondary"
              onClick={async () => {
                try {
                  await navigator.clipboard.writeText(s.email);
                  setFeedback(t("copied"));
                } catch {
                  setFeedback(t("copyError"));
                }
              }}
            >
              <Copy size={16} />
              {t("copyEmail")}
            </button>
          </>
        ) : (
          <p className="muted">{t("contactEmpty")}</p>
        )}
        <p role="status">{feedback}</p>
        {s.location && (
          <p className="location">
            <MapPin size={17} />
            {s.location}
          </p>
        )}
        <div className="hero-links">
          {s.githubUrl && (
            <a href={s.githubUrl} target="_blank" rel="noreferrer">
              GitHub
              <ArrowUpRight size={16} />
            </a>
          )}
          {s.linkedinUrl && (
            <a href={s.linkedinUrl} target="_blank" rel="noreferrer">
              LinkedIn
              <ArrowUpRight size={16} />
            </a>
          )}
        </div>
      </section>
    </>
  );
}
