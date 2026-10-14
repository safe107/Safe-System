import { useAdminMetadata } from "../hooks/useAdminMetadata";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Link, useNavigate } from "react-router-dom";
import { Shield, LockKeyhole, ArrowLeft } from "lucide-react";
import { api, clearCsrf } from "../services/api/client";
import { savedLocale } from "../i18n";
const schema = z.object({
  username: z.string().min(1, "Informe o usuário").max(100),
  password: z.string().min(1, "Informe a senha").max(72),
});
type LoginValues = z.infer<typeof schema>;
export default function Login() {
  useAdminMetadata();
  const navigate = useNavigate();
  const cache = useQueryClient();
  const form = useForm<LoginValues>({ resolver: zodResolver(schema) });
  const login = useMutation({
    mutationFn: (data: LoginValues) =>
      api<{ username: string }>("/auth/login", {
        method: "POST",
        body: JSON.stringify(data),
      }),
    onSuccess: (data) => {
      clearCsrf();
      cache.setQueryData(["me"], data);
      navigate("/admin", { replace: true });
    },
  });
  return (
    <main className="login-page">
      <Link className="text-link" to={`/${savedLocale()}`}>
        <ArrowLeft size={16} />
        Portfólio
      </Link>
      <section className="login-panel">
        <span className="logo-symbol">
          <Shield size={28} />
        </span>
        <p className="eyebrow">SAFE SYSTEM / PRIVATE WORKSPACE</p>
        <h1>Acessar painel</h1>
        <p className="muted">Um espaço para organizar sua trajetória.</p>
        <form onSubmit={form.handleSubmit((values) => login.mutate(values))}>
          <label htmlFor="username">Usuário</label>
          <input
            id="username"
            autoComplete="username"
            {...form.register("username")}
          />
          {form.formState.errors.username && (
            <p role="alert">{form.formState.errors.username.message}</p>
          )}
          <label htmlFor="password">Senha</label>
          <input
            id="password"
            type="password"
            autoComplete="current-password"
            {...form.register("password")}
          />
          {form.formState.errors.password && (
            <p role="alert">{form.formState.errors.password.message}</p>
          )}
          {login.isError && (
            <p role="alert" className="error-message">
              {login.error.message}
            </p>
          )}
          <button className="button" disabled={login.isPending}>
            <LockKeyhole size={16} />
            {login.isPending ? "Entrando…" : "Entrar"}
          </button>
        </form>
        <p className="technical">ACESSO RESTRITO AO PROPRIETÁRIO</p>
      </section>
    </main>
  );
}
