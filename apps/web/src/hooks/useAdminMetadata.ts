import { useEffect } from "react";
import { useTranslation } from "react-i18next";
export function useAdminMetadata() {
  const { i18n } = useTranslation();
  useEffect(() => {
    void i18n.changeLanguage("pt-BR");
    document.documentElement.lang = "pt-BR";
    document.title = "Administração · SAFE System";
    const element = document.createElement("meta");
    element.name = "robots";
    element.content = "noindex,nofollow";
    document.head.append(element);
    return () => element.remove();
  }, [i18n]);
}
