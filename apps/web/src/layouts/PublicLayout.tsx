import { useEffect, useState } from "react";
import {
  NavLink,
  Outlet,
  Link,
  useLocation,
  useNavigate,
  useParams,
  useSearchParams,
} from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
  Moon,
  Sun,
  Languages,
  Menu,
  X,
  Shield,
  ArrowUpRight,
} from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { useTheme } from "../hooks/useTheme";
import { api } from "../services/api/client";
import {
  contentName,
  type Content,
  type Page,
  type Settings,
} from "../types/content";
export default function PublicLayout() {
  const { locale = "pt-BR" } = useParams();
  const { t, i18n } = useTranslation();
  const { theme, toggle } = useTheme();
  const [menu, setMenu] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const [params, setParams] = useSearchParams();
  const profiles = useQuery({
    queryKey: ["profiles"],
    queryFn: () => api<Page<Content>>("/public/professional-profiles?size=100"),
  });
  const settings = useQuery({
    queryKey: ["settings"],
    queryFn: () => api<Settings>("/public/settings"),
  });
  useEffect(() => {
    void i18n.changeLanguage(locale);
    document.documentElement.lang = locale;
    try {
      localStorage.setItem("safe-locale", locale);
    } catch {
      /* Optional persistence */
    }
  }, [locale, i18n]);
  useEffect(() => {
    if (
      location.pathname !== `/${locale}` &&
      location.pathname !== `/${locale}/`
    )
      return;
    document.title = `${settings.data?.seoTitle || settings.data?.name || "José Miguel"} · SAFE System`;
    let meta = document.querySelector<HTMLMetaElement>(
      'meta[name="description"]',
    );
    if (!meta) {
      meta = document.createElement("meta");
      meta.name = "description";
      document.head.append(meta);
    }
    meta.content =
      settings.data?.seoDescription ||
      settings.data?.[locale === "en" ? "bioEn" : "bioPt"] ||
      "";
  }, [location.pathname, settings.data, locale]);
  useEffect(() => {
    if (settings.data?.analyticsEnabled === "true")
      void api("/public/analytics/events", {
        method: "POST",
        body: JSON.stringify({ path: location.pathname, event: "view" }),
      }).catch(() => undefined);
  }, [location.pathname, settings.data?.analyticsEnabled]);
  useEffect(() => {
    try {
      const profile = params.get("professionalProfile");
      if (profile) localStorage.setItem("safe-profile", profile);
      else {
        const stored = localStorage.getItem("safe-profile");
        if (stored) {
          const next = new URLSearchParams(params);
          next.set("professionalProfile", stored);
          setParams(next, { replace: true });
        }
      }
    } catch {
      /* Optional persistence */
    }
  }, [location.pathname, params, setParams]);
  const switchLanguage = () => {
    const next = locale === "en" ? "pt-BR" : "en";
    navigate(
      location.pathname.replace(`/${locale}`, `/${next}`) + location.search,
    );
  };
  const links = [
    ["", "home"],
    ["projects", "projects"],
    ["certificates", "certificates"],
    ["experience", "experience"],
    ["skills", "skills"],
    ["resume", "resume"],
    ["contact", "contact"],
  ];
  return (
    <>
      <a className="skip" href="#content">
        {t("skip")}
      </a>
      <header className="public-header">
        <div className="container header-inner">
          <Link className="logo" to={`/${locale}`}>
            <span className="logo-symbol">
              <Shield size={21} />
            </span>
            <span>
              SAFE<span className="logo-light"> System</span>
            </span>
            <span className="version">01</span>
          </Link>
          <nav aria-label={t("menu")} className={menu ? "nav open" : "nav"}>
            {links.map(([path, label]) => (
              <NavLink
                key={path}
                end={!path}
                to={`/${locale}${path ? "/" + path : ""}`}
                onClick={() => setMenu(false)}
              >
                {t(label)}
              </NavLink>
            ))}
          </nav>
          <div className="header-actions">
            <button
              className="icon-button"
              onClick={switchLanguage}
              aria-label={t("language")}
            >
              <Languages size={18} />
              <span>{locale === "en" ? "EN" : "PT"}</span>
            </button>
            <button
              className="icon-button"
              onClick={toggle}
              aria-label={t("theme")}
            >
              {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <button
              className="icon-button mobile-toggle"
              aria-label={t("menu")}
              aria-expanded={menu}
              onClick={() => setMenu(!menu)}
            >
              {menu ? <X /> : <Menu />}
            </button>
          </div>
        </div>
      </header>
      {(profiles.data?.content.length ?? 0) > 0 && (
        <div className="profile-bar container">
          <label htmlFor="professional-profile">{t("profile")}</label>
          <select
            id="professional-profile"
            value={params.get("professionalProfile") || ""}
            onChange={(e) => {
              const next = new URLSearchParams(params);
              if (e.target.value)
                next.set("professionalProfile", e.target.value);
              else {
                next.delete("professionalProfile");
                try {
                  localStorage.removeItem("safe-profile");
                } catch {
                  /* Optional persistence */
                }
              }
              next.delete("page");
              setParams(next);
            }}
          >
            <option value="">{t("allProfiles")}</option>
            {profiles.data?.content.map((p) => (
              <option key={p.id} value={p.id}>
                {contentName(p, locale)}
              </option>
            ))}
          </select>
        </div>
      )}
      <main id="content" className="container public-main">
        <Outlet />
      </main>
      <footer className="container footer">
        <div>
          <Link className="logo" to={`/${locale}`}>
            <Shield size={18} /> SAFE System
          </Link>
          <p>{t("footer")}</p>
        </div>
        <Link className="text-link" to="/admin/login">
          {t("admin")}
          <ArrowUpRight size={15} />
        </Link>
      </footer>
    </>
  );
}
