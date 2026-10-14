import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { api } from "../services/api/client";
import { Loading, Failure } from "../components/ui/Feedback";
interface AnalyticsData {
  configured: boolean;
  totalViews: number;
  totalDownloads: number;
  rows: { day: string; path: string; event: string; count: number }[];
}
export default function Analytics() {
  const result = useQuery({
    queryKey: ["analytics"],
    queryFn: () => api<AnalyticsData>("/admin/analytics"),
  });
  if (result.isPending) return <Loading />;
  if (result.isError) return <Failure retry={() => void result.refetch()} />;
  const data = result.data;
  return (
    <>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / INSIGHTS</span>
          <h1>Analytics</h1>
        </div>
      </div>
      {!data.configured && (
        <div className="notice">
          Coleta desativada.{" "}
          <Link to="/admin/settings">Configure os eventos agregados</Link> para
          começar. Nenhuma visita é simulada.
        </div>
      )}
      <div className="stats-grid">
        <article className="stat-card">
          <span>Visualizações registradas</span>
          <strong>{data.totalViews}</strong>
        </article>
        <article className="stat-card">
          <span>Downloads registrados</span>
          <strong>{data.totalDownloads}</strong>
        </article>
      </div>
      <section className="panel">
        <h2>Eventos dos últimos 90 dias</h2>
        {!data.rows.length ? (
          <p className="muted">Nenhum evento registrado.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Dia</th>
                  <th>Página</th>
                  <th>Evento</th>
                  <th>Total</th>
                </tr>
              </thead>
              <tbody>
                {data.rows.map((r, i) => (
                  <tr key={i}>
                    <td>{r.day}</td>
                    <td>{r.path}</td>
                    <td>{r.event}</td>
                    <td>{r.count}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
