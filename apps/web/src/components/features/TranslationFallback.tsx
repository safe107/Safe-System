import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { hasTranslationFallback, type Content } from "../../types/content";
export function TranslationFallback({ item }: { item: Content }) {
  const { locale = "pt-BR" } = useParams();
  const { t } = useTranslation();
  return hasTranslationFallback(item, locale) ? (
    <p className="translation-fallback">{t("fallback")}</p>
  ) : null;
}
