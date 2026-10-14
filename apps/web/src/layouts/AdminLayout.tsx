import { useAdminMetadata } from "../hooks/useAdminMetadata";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { NavLink, Navigate, Outlet, Link, useNavigate } from "react-router-dom";
import { useState } from "react";
import {
  Shield,
  LayoutDashboard,
  FolderKanban,
  Award,
  Briefcase,
  Code2,
  FileText,
  BookOpen,
  Send,
  ChartNoAxesCombined,
  UserRound,
  Settings,
  LogOut,
  ArrowUpRight,
  Menu,
  Sun,
  Moon,
} from "lucide-react";
import { api, ApiError, clearCsrf } from "../services/api/client";
import { useTheme } from "../hooks/useTheme";
import { Loading, Failure } from "../components/ui/Feedback";
import { savedLocale } from "../i18n";
export default function AdminLayout() {
  useAdminMetadata();
  const me = useQuery({
    queryKey: ["me"],
    queryFn: () => api<{ username: string }>("/auth/me"),
    retry: false,
    staleTime: 0,
  });
  const cache = useQueryClient();
  const navigate = useNavigate();
  const [menu, setMenu] = useState(false);
  const [error, setError] = useState("");
  const { theme, toggle } = useTheme();
  if (me.isPending) return <Loading />;
  if (me.error instanceof ApiError && me.error.status === 401)
    return <Navigate to="/admin/login" replace />;
  if (me.isError) return <Failure retry={() => void me.refetch()} />;
  const links = [
    ["", "Visão geral", LayoutDashboard],
    ["projects", "Projetos", FolderKanban],
    ["certificates", "Certificados", Award],
    ["experiences", "Experiências", Briefcase],
    ["skills", "Competências", Code2],
    ["resumes", "Currículos", FileText],
    ["files", "Arquivos", FolderKanban],
    ["learning", "Learning Center", BookOpen],
    ["applications", "Candidaturas", Send],
    ["analytics", "Analytics", ChartNoAxesCombined],
    ["profiles", "Perfis", UserRound],
    ["settings", "Configurações", Settings],
  ] as const;
  async function logout() {
    try {
      await api("/auth/logout", { method: "POST" });
      clearCsrf();
      cache.clear();
      navigate("/admin/login", { replace: true });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Falha ao sair");
    }
  }
  return (
    <div className="admin-shell">
      <a className="skip" href="#admin-content">
        Ir para conteúdo
      </a>
      <aside className={menu ? "sidebar open" : "sidebar"}>
        <Link className="logo" to="/admin">
          <span className="logo-symbol">
            <Shield size={20} />
          </span>
          SAFE System
        </Link>
        <p className="eyebrow sidebar-label">WORKSPACE / ADMIN</p>
        <nav aria-label="Administração">
          {links.map(([path, label, Icon]) => (
            <NavLink
              key={path}
              end={!path}
              to={`/admin${path ? "/" + path : ""}`}
              onClick={() => setMenu(false)}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-bottom">
          <Link to={`/${savedLocale()}`} className="text-link">
            Ver portfólio
            <ArrowUpRight size={16} />
          </Link>
          <button className="button ghost" onClick={() => void logout()}>
            <LogOut size={16} />
            Sair
          </button>
          {error && <p role="alert">{error}</p>}
        </div>
      </aside>
      <div className="admin-body">
        <header className="admin-header">
          <button
            className="icon-button mobile-toggle"
            aria-label="Menu"
            aria-expanded={menu}
            onClick={() => setMenu(!menu)}
          >
            <Menu />
          </button>
          <span className="technical">PERSONAL CAREER OS</span>
          <div className="header-actions">
            <button
              className="icon-button"
              aria-label="Alternar tema"
              onClick={toggle}
            >
              {theme === "dark" ? <Sun size={18} /> : <Moon size={18} />}
            </button>
            <span className="avatar">
              {me.data?.username.slice(0, 2).toUpperCase()}
            </span>
            <span>{me.data?.username}</span>
          </div>
        </header>
        <main className="admin-main" id="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
