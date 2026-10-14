import { useDebounce } from "../hooks/useDebounce";
import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus, Pencil, Trash2, Search, LayoutGrid, List } from "lucide-react";
import { modules, endpoint } from "../features/admin/modules";
import { api } from "../services/api/client";
import { contentName, type Content, type Page } from "../types/content";
import { Empty, Failure, Loading } from "../components/ui/Feedback";
import { Confirm } from "../components/ui/Confirm";
export default function AdminList({ module }: { module: string }) {
  const config = modules[module];
  const [params, setParams] = useSearchParams();
  const [selected, setSelected] = useState<Content>();
  const [kanban, setKanban] = useState(false);
  const cache = useQueryClient();
  const path = endpoint(module);
  const debouncedQuery = useDebounce(params.toString());
  const result = useQuery({
    queryKey: ["admin", path, debouncedQuery, kanban],
    queryFn: () =>
      api<Page<Content>>(
        `/admin/${path}?${debouncedQuery}&size=${kanban ? 100 : 20}`,
      ),
  });
  const refresh = () => {
    void cache.invalidateQueries({ queryKey: ["admin", path] });
    void cache.invalidateQueries({ queryKey: ["dashboard"] });
    void cache.invalidateQueries({ queryKey: [path] });
  };
  const remove = useMutation({
    mutationFn: (id: string) =>
      api(`/admin/${path}/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      setSelected(undefined);
      refresh();
    },
  });
  const publish = useMutation({
    mutationFn: (item: Content) =>
      api(`/admin/${path}/${item.id}/publication`, {
        method: "PATCH",
        body: JSON.stringify({ published: !item.published }),
      }),
    onSuccess: refresh,
  });
  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    if (key !== "page") next.delete("page");
    setParams(next);
  };
  const statuses = config.fields.find((f) => f.key === "status")?.options;
  const card = (item: Content) => (
    <article className="kanban-card" key={item.id}>
      <Link to={`/admin/${module}/${item.id}/edit`}>
        <strong>{item.company}</strong>
        <p>{item.position}</p>
      </Link>
      {item.nextAction && <p className="muted">{item.nextAction}</p>}
      {item.followUpAt && <span className="technical">{item.followUpAt}</span>}
    </article>
  );
  return (
    <>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / CONTENT</span>
          <h1>{config.name}</h1>
        </div>
        <Link className="button" to={`/admin/${module}/new`}>
          <Plus size={17} />
          Adicionar
        </Link>
      </div>
      <div className="filters">
        <label className="search-field">
          <span className="sr-only">Buscar</span>
          <Search size={18} />
          <input
            placeholder="Buscar"
            value={params.get("search") || ""}
            onChange={(e) => update("search", e.target.value)}
          />
        </label>
        {statuses && (
          <label>
            Status
            <select
              value={params.get("status") || ""}
              onChange={(e) => update("status", e.target.value)}
            >
              <option value="">Todos</option>
              {statuses.map((s) => (
                <option key={s}>{s}</option>
              ))}
            </select>
          </label>
        )}
        <label>
          Ordenação
          <select
            value={params.get("sort") || "relevance"}
            onChange={(e) => update("sort", e.target.value)}
          >
            <option value="relevance">Ordem definida</option>
            <option value="recent">Mais recentes</option>
            <option value="oldest">Mais antigos</option>
          </select>
        </label>
        <button className="button ghost" onClick={() => setParams({})}>
          Limpar filtros
        </button>
        {module === "applications" && (
          <button
            className="button secondary"
            onClick={() => setKanban(!kanban)}
          >
            {kanban ? <List size={17} /> : <LayoutGrid size={17} />}{" "}
            {kanban ? "Tabela" : "Kanban"}
          </button>
        )}
      </div>
      {remove.isError && (
        <p role="alert" className="error-message">
          {remove.error.message}
        </p>
      )}
      {publish.isError && (
        <p role="alert" className="error-message">
          {publish.error.message}
        </p>
      )}
      {result.isPending ? (
        <Loading />
      ) : result.isError ? (
        <Failure retry={() => void result.refetch()} />
      ) : !result.data.content.length ? (
        <Empty />
      ) : kanban ? (
        <>
          <p className="muted">
            Exibindo {result.data.content.length} de {result.data.totalElements}{" "}
            candidaturas nesta página.
          </p>
          <div className="kanban">
            {statuses?.map((s) => (
              <section className="kanban-column" key={s}>
                <h2>
                  {s}
                  <span className="badge">
                    {result.data.content.filter((i) => i.status === s).length}
                  </span>
                </h2>
                {result.data.content.filter((i) => i.status === s).map(card)}
              </section>
            ))}
          </div>
        </>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Conteúdo</th>
                <th>Status</th>
                <th>Atualizado</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {result.data.content.map((item) => (
                <tr key={item.id}>
                  <td>
                    <Link to={`/admin/${module}/${item.id}/edit`}>
                      {module === "applications"
                        ? `${item.company} · ${item.position}`
                        : contentName(item)}
                    </Link>
                    <small>
                      {item.category || item.provider || item.organization}
                    </small>
                  </td>
                  <td>
                    <span className="badge">
                      {item.status ||
                        ("published" in item
                          ? item.published
                            ? "Publicado"
                            : "Rascunho"
                          : item.enabled
                            ? "Ativo"
                            : "Inativo")}
                    </span>
                  </td>
                  <td className="technical">
                    {new Intl.DateTimeFormat("pt-BR", {
                      dateStyle: "short",
                    }).format(new Date(item.updatedAt))}
                  </td>
                  <td>
                    <div className="row-actions">
                      {config.publication && (
                        <button
                          className="button ghost"
                          disabled={publish.isPending}
                          onClick={() => publish.mutate(item)}
                        >
                          {item.published ? "Ocultar" : "Publicar"}
                        </button>
                      )}
                      <Link
                        className="icon-button"
                        aria-label={`Editar ${contentName(item)}`}
                        to={`/admin/${module}/${item.id}/edit`}
                      >
                        <Pencil size={16} />
                      </Link>
                      <button
                        className="icon-button danger-text"
                        aria-label={`Excluir ${contentName(item)}`}
                        onClick={() => setSelected(item)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {result.data && (
        <div className="pagination">
          <button
            className="button secondary"
            disabled={result.data.page === 0}
            onClick={() => update("page", String(result.data.page - 1))}
          >
            Anterior
          </button>
          <span>
            {result.data.totalElements} registros · Página{" "}
            {result.data.page + 1}/{Math.max(1, result.data.totalPages)}
          </span>
          <button
            className="button secondary"
            disabled={result.data.page + 1 >= result.data.totalPages}
            onClick={() => update("page", String(result.data.page + 1))}
          >
            Próxima
          </button>
        </div>
      )}
      <Confirm
        open={!!selected}
        onClose={() => setSelected(undefined)}
        title="Excluir conteúdo?"
        description="Esta ação remove o registro e seus vínculos. Arquivos devem ser excluídos separadamente depois de desvinculados."
        pending={remove.isPending}
        onConfirm={() => selected && remove.mutate(selected.id)}
      />
    </>
  );
}
