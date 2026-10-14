import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { api, downloadUrl } from "../services/api/client";
import type { Page } from "../types/content";
import { Loading, Failure, Empty } from "../components/ui/Feedback";
import { Confirm } from "../components/ui/Confirm";
interface Entry {
  id: string;
  originalName: string;
  mimeType: string;
  sizeBytes: number;
  createdAt: string;
  linked: boolean;
}
export default function Files() {
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Entry>();
  const cache = useQueryClient();
  const result = useQuery({
    queryKey: ["admin-files", page],
    queryFn: () => api<Page<Entry>>(`/admin/files?page=${page}`),
  });
  const remove = useMutation({
    mutationFn: (id: string) => api(`/admin/files/${id}`, { method: "DELETE" }),
    onSuccess: () => {
      setSelected(undefined);
      void cache.invalidateQueries({ queryKey: ["admin-files"] });
    },
  });
  return (
    <>
      <div className="admin-page-heading">
        <div>
          <span className="eyebrow">WORKSPACE / FILE VAULT</span>
          <h1>Arquivos</h1>
          <p className="muted">
            Objetos privados. Desvincule o conteúdo antes de excluir um arquivo.
          </p>
        </div>
      </div>
      {remove.isError && (
        <p role="alert" className="error-message">
          {remove.error.message}
        </p>
      )}
      {result.isPending ? (
        <Loading />
      ) : result.isError ? (
        <Failure retry={() => void result.refetch()} />
      ) : !result.data.content.length ? (
        <Empty />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Arquivo</th>
                <th>Formato / tamanho</th>
                <th>Vínculos</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {result.data.content.map((f) => (
                <tr key={f.id}>
                  <td>{f.originalName}</td>
                  <td>
                    {f.mimeType}
                    <small>{(f.sizeBytes / 1024).toFixed(1)} KB</small>
                  </td>
                  <td>
                    <span className="badge">
                      {f.linked ? "Vinculado" : "Sem vínculo"}
                    </span>
                  </td>
                  <td>
                    <div className="row-actions">
                      <a
                        className="button secondary"
                        href={downloadUrl(f.id, true)}
                      >
                        Baixar
                      </a>
                      <button
                        className="button ghost danger-text"
                        disabled={f.linked}
                        onClick={() => setSelected(f)}
                      >
                        Excluir
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
            disabled={!page}
            onClick={() => setPage(page - 1)}
          >
            Anterior
          </button>
          <span>{result.data.totalElements} arquivos</span>
          <button
            className="button secondary"
            disabled={page + 1 >= result.data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Próxima
          </button>
        </div>
      )}
      <Confirm
        open={!!selected}
        onClose={() => setSelected(undefined)}
        onConfirm={() => selected && remove.mutate(selected.id)}
        title="Excluir arquivo permanentemente?"
        description="O arquivo deixará de estar acessível e sua exclusão física será processada pela fila segura. Essa ação não pode ser desfeita."
        pending={remove.isPending}
      />
    </>
  );
}
