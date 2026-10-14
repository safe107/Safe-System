import { beforeEach, afterEach, describe, it, expect, vi } from "vitest";
import { render, screen, waitFor, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import type { ReactNode } from "react";
import { api } from "../services/api/client";
import i18n from "../i18n";
import PublicIndex from "./PublicIndex";
import PublicCollection from "./PublicCollection";
import Contact from "./Contact";
import Login from "./Login";
import AdminEditor from "./AdminEditor";
import { useTheme } from "../hooks/useTheme";
vi.mock("../services/api/client", () => ({
  api: vi.fn(),
  clearCsrf: vi.fn(),
  downloadUrl: (id: string) => `/files/${id}`,
}));
const mockApi = vi.mocked(api);
const page = {
  content: [],
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
};
function wrapper(
  component: ReactNode,
  path = "/pt-BR/projects",
  route = "/:locale/projects",
) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false }, mutations: { retry: false } },
  });
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path={route} element={component} />
          <Route path="/admin/projects" element={<p>Saved list</p>} />
          <Route path="/admin" element={<p>Authenticated</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  );
}
beforeEach(async () => {
  mockApi.mockReset();
  mockApi.mockResolvedValue(page);
  localStorage.clear();
  await i18n.changeLanguage("pt-BR");
});
afterEach(cleanup);
describe("Fluxos públicos", () => {
  it("mostra estado vazio com resposta real vazia", async () => {
    wrapper(<PublicIndex kind="projects" />);
    expect(
      await screen.findByText("Nenhum conteúdo publicado nesta seção."),
    ).toBeVisible();
    expect(mockApi).toHaveBeenCalledWith("/public/projects?");
  });
  it("sincroniza busca e filtro combinável com requisição", async () => {
    const user = userEvent.setup();
    wrapper(<PublicIndex kind="projects" />);
    await screen.findByText("Nenhum conteúdo publicado nesta seção.");
    await user.type(screen.getByPlaceholderText("Buscar"), "java");
    await user.selectOptions(screen.getByLabelText("Categoria"), "backend");
    await waitFor(() =>
      expect(mockApi).toHaveBeenCalledWith(
        "/public/projects?search=java&category=backend",
      ),
    );
    await user.click(screen.getByRole("button", { name: "Limpar filtros" }));
    await waitFor(() =>
      expect(mockApi).toHaveBeenLastCalledWith("/public/projects?"),
    );
  });
  it("permite recuperação quando API falha", async () => {
    mockApi.mockRejectedValue(new Error("network"));
    wrapper(<PublicIndex kind="certificates" />);
    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Não foi possível carregar",
    );
    mockApi.mockResolvedValue(page);
    await userEvent.click(
      screen.getByRole("button", { name: "Tentar novamente" }),
    );
    expect(
      await screen.findByText("Nenhum conteúdo publicado nesta seção."),
    ).toBeVisible();
  });
  it("não oferece contato fictício quando não há email", async () => {
    mockApi.mockResolvedValue({ name: "José Miguel" });
    wrapper(<Contact />, "/pt-BR/contact", "/:locale/contact");
    expect(
      await screen.findByText(
        "Os links de contato ainda não foram cadastrados.",
      ),
    ).toBeVisible();
    expect(
      screen.queryByRole("button", { name: "Copiar e-mail" }),
    ).not.toBeInTheDocument();
  });
  it("busca currículos no idioma da rota", async () => {
    await i18n.changeLanguage("en");
    wrapper(
      <PublicCollection kind="resume" />,
      "/en/resume",
      "/:locale/resume",
    );
    expect(
      await screen.findByText("No published content in this section."),
    ).toBeVisible();
    expect(mockApi).toHaveBeenCalledWith("/public/resumes?size=100&locale=en");
  });
  it("alterna e persiste tema sem guardar credenciais", async () => {
    document.documentElement.dataset.theme = "dark";
    function ThemeButton() {
      const { toggle, theme } = useTheme();
      return <button onClick={toggle}>{theme}</button>;
    }
    render(<ThemeButton />);
    await userEvent.click(screen.getByRole("button", { name: "dark" }));
    expect(document.documentElement.dataset.theme).toBe("light");
    expect(localStorage.getItem("safe-theme")).toBe("light");
    await userEvent.click(screen.getByRole("button", { name: "light" }));
    expect(localStorage.getItem("safe-theme")).toBe("dark");
  });
});
describe("Formulários privados", () => {
  it("login rejeita campos vazios sem chamar API", async () => {
    wrapper(<Login />, "/admin/login", "/admin/login");
    await userEvent.click(screen.getByRole("button", { name: "Entrar" }));
    expect(await screen.findByText("Informe o usuário")).toBeVisible();
    expect(mockApi).not.toHaveBeenCalled();
  });
  it("envia credenciais somente para endpoint de sessão", async () => {
    mockApi.mockResolvedValue({ username: "admin" });
    const user = userEvent.setup();
    wrapper(<Login />, "/admin/login", "/admin/login");
    await user.type(screen.getByLabelText("Usuário"), "admin");
    await user.type(screen.getByLabelText("Senha"), "test-password");
    await user.click(screen.getByRole("button", { name: "Entrar" }));
    expect(await screen.findByText("Authenticated")).toBeVisible();
    expect(mockApi).toHaveBeenCalledWith("/auth/login", {
      method: "POST",
      body: JSON.stringify({ username: "admin", password: "test-password" }),
    });
    expect(localStorage.length).toBe(0);
  });
  it("projeto exige slug e tradução PT-BR antes de gravar", async () => {
    wrapper(
      <AdminEditor module="projects" />,
      "/admin/projects/new",
      "/admin/projects/new",
    );
    await userEvent.click(screen.getByRole("button", { name: "Salvar" }));
    expect(await screen.findAllByText("Campo obrigatório")).toHaveLength(2);
    expect(
      mockApi.mock.calls.filter((c) => c[1]?.method === "POST"),
    ).toHaveLength(0);
  });
  it("salva projeto com DTO e tradução sem simulação", async () => {
    const user = userEvent.setup();
    wrapper(
      <AdminEditor module="projects" />,
      "/admin/projects/new",
      "/admin/projects/new",
    );
    await user.type(screen.getByLabelText(/Slug único/), "api-real");
    await user.type(screen.getByLabelText("Título *"), "API real");
    await user.click(screen.getByRole("button", { name: "Salvar" }));
    expect(await screen.findByText("Saved list")).toBeVisible();
    const call = mockApi.mock.calls.find((c) => c[1]?.method === "POST");
    expect(call?.[0]).toBe("/admin/projects");
    const body = JSON.parse(String(call?.[1]?.body));
    expect(body.translations["pt-BR"].title).toBe("API real");
    expect(body.published).toBe(false);
    expect(body.mediaIds).toEqual([]);
    expect(body.translations.en).toBeUndefined();
  });
});
