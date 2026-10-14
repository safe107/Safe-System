import { useTranslation } from "react-i18next";
import { Inbox, AlertTriangle, LoaderCircle } from "lucide-react";
export function Loading() {
  const { t } = useTranslation();
  return (
    <div className="feedback" role="status">
      <LoaderCircle className="spin" size={24} />
      <p>{t("loading")}</p>
    </div>
  );
}
export function Empty({ filtered = false }: { filtered?: boolean }) {
  const { t } = useTranslation();
  return (
    <div className="feedback empty">
      <Inbox size={32} />
      <p>{t(filtered ? "emptyFiltered" : "empty")}</p>
    </div>
  );
}
export function Failure({ retry }: { retry?: () => void }) {
  const { t } = useTranslation();
  return (
    <div className="feedback" role="alert">
      <AlertTriangle />
      <p>{t("error")}</p>
      {retry && (
        <button className="button secondary" onClick={retry}>
          {t("retry")}
        </button>
      )}
    </div>
  );
}
