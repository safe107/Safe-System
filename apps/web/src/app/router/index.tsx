import { useQuery } from "@tanstack/react-query";
import { api } from "../../services/api/client";
import type { Settings } from "../../types/content";
import { lazy, Suspense } from "react";
import {
  createBrowserRouter,
  Navigate,
  RouterProvider,
  Link,
  useParams,
} from "react-router-dom";
import { useTranslation } from "react-i18next";
import { Loading } from "../../components/ui/Feedback";
import { savedLocale } from "../../i18n";
import { modules } from "../../features/admin/modules";
const PublicLayout = lazy(() => import("../../layouts/PublicLayout"));
const AdminLayout = lazy(() => import("../../layouts/AdminLayout"));
const Home = lazy(() => import("../../pages/Home"));
const PublicIndex = lazy(() => import("../../pages/PublicIndex"));
const PublicDetails = lazy(() => import("../../pages/PublicDetails"));
const PublicCollection = lazy(() => import("../../pages/PublicCollection"));
const Contact = lazy(() => import("../../pages/Contact"));
const Login = lazy(() => import("../../pages/Login"));
const Dashboard = lazy(() => import("../../pages/Dashboard"));
const AdminList = lazy(() => import("../../pages/AdminList"));
const AdminEditor = lazy(() => import("../../pages/AdminEditor"));
const AdminSettings = lazy(() => import("../../pages/AdminSettings"));
const Files = lazy(() => import("../../pages/Files"));
const Analytics = lazy(() => import("../../pages/Analytics"));
function NotFound() {
  const { t } = useTranslation();
  const { locale } = useParams();
  return (
    <div className="feedback">
      <h1>{t("notFound")}</h1>
      <Link className="button" to={`/${locale || savedLocale()}`}>
        {t("backHome")}
      </Link>
    </div>
  );
}
function LocaleBoundary() {
  const { locale } = useParams();
  return locale === "pt-BR" || locale === "en" ? (
    <PublicLayout />
  ) : (
    <NotFound />
  );
}
function InitialLocale() {
  let stored: string | null = null;
  try {
    stored = localStorage.getItem("safe-locale");
  } catch {
    /* Optional persistence */
  }
  const validStored = stored === "en" || stored === "pt-BR" ? stored : null;
  const settings = useQuery({
    queryKey: ["settings"],
    queryFn: () => api<Settings>("/public/settings"),
    enabled: !validStored,
  });
  if (!validStored && settings.isPending) return <Loading />;
  const locale =
    validStored || (settings.data?.defaultLocale === "en" ? "en" : "pt-BR");
  return <Navigate to={`/${locale}`} replace />;
}
const router = createBrowserRouter([
  { path: "/", element: <InitialLocale /> },
  { path: "/admin/login", element: <Login /> },
  {
    path: "/admin",
    element: <AdminLayout />,
    children: [
      { index: true, element: <Dashboard /> },
      ...Object.keys(modules).flatMap((module) => [
        { path: module, element: <AdminList module={module} /> },
        { path: `${module}/new`, element: <AdminEditor module={module} /> },
        {
          path: `${module}/:id/edit`,
          element: <AdminEditor module={module} />,
        },
      ]),
      { path: "settings", element: <AdminSettings /> },
      { path: "analytics", element: <Analytics /> },
      { path: "files", element: <Files /> },
      { path: "*", element: <NotFound /> },
    ],
  },
  {
    path: "/:locale",
    element: <LocaleBoundary />,
    children: [
      { index: true, element: <Home /> },
      { path: "projects", element: <PublicIndex kind="projects" /> },
      { path: "projects/:slug", element: <PublicDetails kind="projects" /> },
      { path: "certificates", element: <PublicIndex kind="certificates" /> },
      {
        path: "certificates/:slug",
        element: <PublicDetails kind="certificates" />,
      },
      { path: "experience", element: <PublicCollection kind="experience" /> },
      { path: "skills", element: <PublicCollection kind="skills" /> },
      { path: "resume", element: <PublicCollection kind="resume" /> },
      { path: "contact", element: <Contact /> },
      { path: "*", element: <NotFound /> },
    ],
  },
]);
export default function AppRouter() {
  return (
    <Suspense fallback={<Loading />}>
      <RouterProvider router={router} />
    </Suspense>
  );
}
