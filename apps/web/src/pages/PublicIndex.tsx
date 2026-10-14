import { useDebounce } from "../hooks/useDebounce";
import { useSeo } from "../hooks/useSeo";
import { useParams, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useQuery } from "@tanstack/react-query";
import { Search, SlidersHorizontal } from "lucide-react";
import { api } from "../services/api/client";
import type { Content, Page } from "../types/content";
import { contentName } from "../types/content";
import { ContentCard } from "../components/features/ContentCard";
import { Empty, Failure, Loading } from "../components/ui/Feedback";
export default function PublicIndex({
  kind,
}: {
  kind: "projects" | "certificates";
}) {
  const { locale = "pt-BR" } = useParams();
  const { t } = useTranslation();
  useSeo(t(kind));
  const [params, setParams] = useSearchParams();
  const query = useDebounce(params.toString());
  const result = useQuery({
    queryKey: [kind, query],
    queryFn: () => api<Page<Content>>(`/public/${kind}?${query}`),
  });
  const skills = useQuery({
    queryKey: ["public-skills"],
    queryFn: () => api<Page<Content>>("/public/skills?size=100"),
  });
  const update = (key: string, value: string) => {
    const next = new URLSearchParams(params);
    if (value) next.set(key, value);
    else next.delete(key);
    if (key !== "page") next.delete("page");
    setParams(next, { replace: key === "search" });
  };
  const categories = [
    "backend",
    "frontend",
    "fullStack",
    "automation",
    "infrastructure",
    "academic",
  ];
  return (
    <>
      <div className="page-heading">
        <span className="eyebrow">
          {t(kind === "projects" ? "projectIndex" : "certificateVault")}
        </span>
        <h1>{t(kind)}</h1>
      </div>
      <form
        className="filters"
        role="search"
        onSubmit={(e) => e.preventDefault()}
      >
        <label className="search-field">
          <span className="sr-only">{t("search")}</span>
          <Search size={18} />
          <input
            type="search"
            placeholder={t("search")}
            value={params.get("search") || ""}
            onChange={(e) => update("search", e.target.value)}
          />
        </label>
        <label>
          {t("category")}
          <select
            value={params.get("category") || ""}
            onChange={(e) => update("category", e.target.value)}
          >
            <option value="">{t("all")}</option>
            {categories.map((c) => (
              <option key={c} value={c}>
                {t(c)}
              </option>
            ))}
          </select>
        </label>
        <label>
          {t("technology")}
          <select
            value={params.get("skill") || ""}
            onChange={(e) => update("skill", e.target.value)}
          >
            <option value="">{t("all")}</option>
            {skills.data?.content.map((s) => (
              <option key={s.id} value={s.id}>
                {contentName(s, locale)}
              </option>
            ))}
          </select>
        </label>
        {kind === "projects" && (
          <label>
            {t("status")}
            <select
              value={params.get("status") || ""}
              onChange={(e) => update("status", e.target.value)}
            >
              <option value="">{t("all")}</option>
              {["planned", "in-progress", "completed", "archived"].map((s) => (
                <option key={s} value={s}>
                  {t(s)}
                </option>
              ))}
            </select>
          </label>
        )}
        {kind === "certificates" && (
          <label>
            {t("issuer")}
            <input
              value={params.get("issuer") || ""}
              onChange={(e) => update("issuer", e.target.value)}
            />
          </label>
        )}
        <label>
          {t("sort")}
          <select
            value={params.get("sort") || "relevance"}
            onChange={(e) => update("sort", e.target.value)}
          >
            {["relevance", "recent", "oldest"].map((s) => (
              <option key={s} value={s}>
                {t(s)}
              </option>
            ))}
          </select>
        </label>
        <button
          className="button ghost"
          type="button"
          onClick={() => setParams({})}
        >
          <SlidersHorizontal size={16} />
          {t("clear")}
        </button>
      </form>
      {result.isPending ? (
        <Loading />
      ) : result.isError ? (
        <Failure retry={() => void result.refetch()} />
      ) : (
        <>
          <p className="technical results-count">
            {t("filterCount", { count: result.data.totalElements })}
          </p>
          {result.data.content.length ? (
            <div className="card-grid">
              {result.data.content.map((item) => (
                <ContentCard key={item.id} item={item} kind={kind} />
              ))}
            </div>
          ) : (
            <Empty filtered={query.length > 0} />
          )}
          <div className="pagination">
            <button
              className="button secondary"
              disabled={result.data.page === 0}
              onClick={() => update("page", String(result.data.page - 1))}
            >
              {t("previous")}
            </button>
            <span>
              {t("page", {
                page: result.data.page + 1,
                total: Math.max(1, result.data.totalPages),
              })}
            </span>
            <button
              className="button secondary"
              disabled={result.data.page + 1 >= result.data.totalPages}
              onClick={() => update("page", String(result.data.page + 1))}
            >
              {t("next")}
            </button>
          </div>
        </>
      )}
    </>
  );
}
