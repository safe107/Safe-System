import { useEffect } from "react";
import { useLocation, useParams } from "react-router-dom";
export function useSeo(title: string, description = "") {
  const location = useLocation();
  const { locale = "pt-BR" } = useParams();
  useEffect(() => {
    document.title = `${title} · SAFE System`;
    function meta(name: string, value: string, property = false) {
      let element = document.head.querySelector<HTMLMetaElement>(
        `meta[${property ? "property" : "name"}="${name}"]`,
      );
      if (!element) {
        element = document.createElement("meta");
        element.setAttribute(property ? "property" : "name", name);
        document.head.append(element);
      }
      element.content = value;
    }
    meta("description", description);
    meta("og:title", title, true);
    meta("og:description", description, true);
    meta("og:type", "website", true);
    meta("og:locale", locale === "en" ? "en_US" : "pt_BR", true);
    document.head
      .querySelectorAll("link[data-safe-seo]")
      .forEach((e) => e.remove());
    const base = import.meta.env.VITE_SITE_URL;
    if (base) {
      const url = new URL(location.pathname, base);
      meta("og:url", url.toString(), true);
      const canonical = document.createElement("link");
      canonical.rel = "canonical";
      canonical.href = url.toString();
      canonical.dataset.safeSeo = "true";
      document.head.append(canonical);
      for (const language of ["pt-BR", "en"]) {
        const link = document.createElement("link");
        link.rel = "alternate";
        link.hreflang = language;
        link.href = new URL(
          location.pathname.replace(`/${locale}`, `/${language}`),
          base,
        ).toString();
        link.dataset.safeSeo = "true";
        document.head.append(link);
      }
    }
    return () => {
      document.head
        .querySelectorAll("link[data-safe-seo]")
        .forEach((e) => e.remove());
    };
  }, [title, description, locale, location.pathname]);
}
