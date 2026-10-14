import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import {
  Plus,
  ArrowUpRight,
  FolderKanban,
  Award,
  BookOpen,
  Send,
} from "lucide-react";
import { api } from "../services/api/client";
import { Loading, Failure } from "../components/ui/Feedback";
interface DashboardData {
  analyticsConfigured: boolean;
  totalViews: number | null;
  projects: number;
  publishedProjects: number;
  certificates: number;
  publishedCertificates: number;
  activeCourses: number;
  activeApplications: number;
  draftProjects: { id: string; slug: string; title: string }[];
  pendingCourses: {
    id: string;
    name: string;
    status: string;
    progress?: number;
  }[];
  nextActions: {
    id: string;
    company: string;
    position: string;
    nextAction: string;
    followUpAt: string;
  }[];
  recentActivity: { type: string; label: string; updatedAt: string }[];
  applicationStats: { status: string; count: number }[];
}
export default function Dashboard() {
  const query = useQuery({
    queryKey: ["dashboard"],
    queryFn: () => api<DashboardData>("/admin/dashboard"),
  });
  if (query.isPending) return <Loading />;
  if (query.isError) return <Failure retry={() => void query.refetch()} />;
  const d = query.data;
  const cards = [
    ["Projetos", d.projects, `${d.publishedProjects} publicados`, FolderKanban],
    [
      "Certificados",
      d.certificates,
      `${d.publishedCertificates} publicados`,
      Award,
    ],
    ["Cursos em andamento", d.activeCourses, "Learning Center", BookOpen],
    ["Candidaturas ativas", d.activeApplications, "Application Tracker", Send],
  ] as const;
  return (
    <>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / OVERVIEW</span>
          <h1>Sua carreira, organizada.</h1>
          <p className="muted">
            Conteúdo público e próximos passos em um só lugar.
          </p>
        </div>
        <Link className="button" to="/admin/projects/new">
          <Plus size={17} />
          Novo projeto
        </Link>
      </div>
      <div className="stats-grid">
        {cards.map(([title, count, subtitle, Icon]) => (
          <article className="stat-card" key={title}>
            <div>
              <span>{title}</span>
              <Icon size={20} />
            </div>
            <strong>{count}</strong>
            <p>{subtitle}</p>
          </article>
        ))}
      </div>
      {d.analyticsConfigured && (
        <section className="panel analytics-overview">
          <h2>Visualizações registradas</h2>
          <p>{d.totalViews ?? 0}</p>
          <Link className="text-link" to="/admin/analytics">
            Ver analytics <ArrowUpRight size={16} />
          </Link>
        </section>
      )}
      <div className="dashboard-grid">
        <section className="panel">
          <div className="section-heading">
            <h2>Projetos em rascunho</h2>
            <Link
              to="/admin/projects"
              className="icon-button"
              aria-label="Ver projetos"
            >
              <ArrowUpRight size={17} />
            </Link>
          </div>
          {d.draftProjects.length ? (
            d.draftProjects.map((p) => (
              <Link
                className="dashboard-row"
                key={p.id}
                to={`/admin/projects/${p.id}/edit`}
              >
                <span>{p.title || p.slug}</span>
                <span className="badge">Rascunho</span>
              </Link>
            ))
          ) : (
            <p className="muted">Nenhum rascunho.</p>
          )}
        </section>
        <section className="panel">
          <h2>Próximas ações</h2>
          {d.nextActions.length ? (
            d.nextActions.map((a) => (
              <Link
                className="dashboard-row"
                key={a.id}
                to={`/admin/applications/${a.id}/edit`}
              >
                <div>
                  <strong>{a.company}</strong>
                  <p>{a.nextAction || a.position}</p>
                </div>
                <span className="technical">{a.followUpAt}</span>
              </Link>
            ))
          ) : (
            <p className="muted">Nenhum acompanhamento pendente.</p>
          )}
        </section>
        <section className="panel">
          <h2>Learning Center</h2>
          {d.pendingCourses.length ? (
            d.pendingCourses.map((c) => (
              <Link
                className="dashboard-row"
                key={c.id}
                to={`/admin/learning/${c.id}/edit`}
              >
                <span>{c.name}</span>
                <span className="badge">
                  {c.progress != null ? `${c.progress}%` : c.status}
                </span>
              </Link>
            ))
          ) : (
            <p className="muted">Nenhum curso pendente.</p>
          )}
        </section>
        <section className="panel">
          <h2>Atividade recente</h2>
          {d.recentActivity.length ? (
            d.recentActivity.map((a, i) => (
              <div className="dashboard-row" key={i}>
                <span>{a.label}</span>
                <span className="technical">
                  {new Intl.DateTimeFormat("pt-BR", {
                    dateStyle: "short",
                  }).format(new Date(a.updatedAt))}
                </span>
              </div>
            ))
          ) : (
            <p className="muted">Sem atividade registrada.</p>
          )}
        </section>
      </div>
    </>
  );
}
