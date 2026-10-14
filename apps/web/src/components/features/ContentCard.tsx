import { TranslationFallback } from "./TranslationFallback";
import { Link, useParams } from "react-router-dom";
import { ArrowUpRight, Code2, Award, ArrowRight } from "lucide-react";
import { useTranslation } from "react-i18next";
import { contentName, translated, type Content } from "../../types/content";
import { downloadUrl } from "../../services/api/client";
export function ContentCard({
  item,
  kind,
}: {
  item: Content;
  kind: "projects" | "certificates";
}) {
  const { locale = "pt-BR" } = useParams();
  const { t } = useTranslation();
  const text = translated(item, locale);
  return (
    <article className="content-card">
      <div className="card-art">
        {item.mediaIds?.[0] ? (
          <img
            loading="lazy"
            src={downloadUrl(item.mediaIds[0], false, true)}
            alt={contentName(item, locale)}
          />
        ) : kind === "projects" ? (
          <Code2 size={40} strokeWidth={1.25} />
        ) : (
          <Award size={40} strokeWidth={1.25} />
        )}
        <span className="technical">{item.category || item.issuer}</span>
      </div>
      <div className="card-body">
        <TranslationFallback item={item} />
        <div className="card-meta">
          <span className="badge">
            {item.status ? t(item.status) : item.issuer}
          </span>
          {item.featured && <span className="dot" aria-label={t("featured")} />}
        </div>
        <h3>
          <Link to={`/${locale}/${kind}/${item.slug}`}>
            {contentName(item, locale)}
            <ArrowUpRight size={19} />
          </Link>
        </h3>
        <p>{text.summary || text.description}</p>
        <Link className="text-link" to={`/${locale}/${kind}/${item.slug}`}>
          {t("details")}
          <ArrowRight size={15} />
        </Link>
      </div>
    </article>
  );
}
