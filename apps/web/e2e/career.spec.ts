import { test, expect } from "@playwright/test";
import { readFileSync } from "node:fs";
const document = readFileSync(
  new URL("./fixtures/document.pdf", import.meta.url),
);
const user = process.env.ADMIN_USERNAME;
const password = process.env.ADMIN_PASSWORD;
if (!user || !password)
  throw new Error(
    "E2E requires explicit ADMIN_USERNAME and ADMIN_PASSWORD. Use an isolated test database.",
  );
const tag = Date.now().toString();
test("login, conteúdo, publicação, PDF, carreira, currículo e logout", async ({
  page,
  request,
}) => {
  await page.goto("/admin/login");
  await page.getByLabel("Usuário").fill(user!);
  await page.getByLabel("Senha", { exact: true }).fill(password!);
  await page.getByRole("button", { name: "Entrar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin$/);
  await expect(
    page.getByRole("heading", { name: "Sua carreira, organizada." }),
  ).toBeVisible();
  await page.goto("/admin/projects/new");
  await page.getByLabel("Slug único (minúsculas e hífens)").fill(`e2e-${tag}`);
  await page.getByLabel("Título *", { exact: true }).fill(`Projeto E2E ${tag}`);
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/projects$/);
  await page
    .getByRole("row")
    .filter({ hasText: `Projeto E2E ${tag}` })
    .getByRole("button", { name: "Publicar", exact: true })
    .click();
  await page.goto(`/pt-BR/projects/e2e-${tag}`);
  await expect(page.getByRole("heading", { level: 1 })).toHaveText(
    `Projeto E2E ${tag}`,
  );
  await page.goto("/admin/certificates/new");
  await page
    .getByLabel("Slug único (minúsculas e hífens)")
    .fill(`cert-e2e-${tag}`);
  await page
    .getByLabel("Instituição emissora *")
    .fill("Instituição de teste E2E");
  await page
    .getByLabel("Título *", { exact: true })
    .fill(`Certificado E2E ${tag}`);
  await page.getByLabel("Documento PDF").setInputFiles({
    name: "certificate.pdf",
    mimeType: "application/pdf",
    buffer: document,
  });
  await expect(page.getByText("Arquivo anexado")).toBeVisible();
  await page.getByLabel("Publicado", { exact: true }).check();
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/certificates$/);
  await page.goto(`/pt-BR/certificates/cert-e2e-${tag}`);
  const download = page.getByRole("link", { name: "Baixar PDF" });
  await expect(download).toBeVisible();
  const href = await download.getAttribute("href");
  expect((await request.get(href!)).status()).toBe(200);
  await page.goto("/admin/certificates");
  await page
    .getByRole("row")
    .filter({ hasText: `Certificado E2E ${tag}` })
    .getByRole("button", { name: "Ocultar" })
    .click();
  await expect(
    page.getByRole("row").filter({ hasText: `Certificado E2E ${tag}` }),
  ).toContainText("Rascunho");
  expect((await request.get(href!)).status()).toBe(404);
  await page.goto("/admin/applications/new");
  await page.getByLabel("Empresa *").fill("Empresa de teste E2E");
  await page.getByLabel("Cargo *").fill(`Cargo ${tag}`);
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/applications$/);
  await page
    .getByRole("row")
    .filter({ hasText: `Cargo ${tag}` })
    .getByRole("link", { name: /Editar/ })
    .click();
  await page.getByLabel("Status *", { exact: true }).selectOption("interview");
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(
    page.getByRole("row").filter({ hasText: `Cargo ${tag}` }),
  ).toContainText("interview");
  await page.goto("/admin/learning/new");
  await page.getByLabel("Curso *").fill(`Curso E2E ${tag}`);
  await page
    .getByLabel("Plataforma ou instituição *")
    .fill("Plataforma de teste E2E");
  await page
    .getByLabel("Status *", { exact: true })
    .selectOption("in-progress");
  await page.getByLabel("Progresso (%)").fill("45");
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/learning$/);
  await page
    .getByRole("row")
    .filter({ hasText: `Curso E2E ${tag}` })
    .getByRole("link", { name: /Editar/ })
    .click();
  await page.getByLabel("Progresso (%)").fill("80");
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await page.goto("/admin/resumes/new");
  await page
    .getByLabel("Título *", { exact: true })
    .fill(`Currículo E2E ${tag}`);
  await page.getByLabel("Currículo PDF").setInputFiles({
    name: "resume.pdf",
    mimeType: "application/pdf",
    buffer: document,
  });
  await expect(page.getByText("Arquivo anexado")).toBeVisible();
  await page.getByLabel("Publicado", { exact: true }).check();
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/resumes$/);
  await page.goto("/pt-BR/resume");
  const resume = page
    .getByRole("article")
    .filter({ hasText: `Currículo E2E ${tag}` });
  const resumeHref = await resume
    .getByRole("link", { name: "Baixar PDF" })
    .getAttribute("href");
  expect((await request.get(resumeHref!)).status()).toBe(200);
  await page.goto("/admin");
  await page.getByRole("button", { name: "Sair", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/login$/);
  await page.goto("/admin/applications");
  await expect(page).toHaveURL(/\/admin\/login$/);
});
test("idiomas, tema, filtros, mobile e teclado", async ({ page }) => {
  await page.goto("/pt-BR");
  await expect(
    page.getByRole("heading", { name: /José Miguel/ }),
  ).toBeVisible();
  await page
    .getByRole("button", { name: "Change language to English" })
    .click();
  await expect(page).toHaveURL(/\/en$/);
  await expect(
    page.getByRole("link", { name: "Explore projects" }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Toggle theme" }).click();
  expect(await page.locator("html").getAttribute("data-theme")).toBe("light");
  await page.reload();
  expect(await page.locator("html").getAttribute("data-theme")).toBe("light");
  await page.setViewportSize({ width: 390, height: 844 });
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
  await page.goto("/en/contact");
  await page.getByRole("heading", { name: "Contact", exact: true }).waitFor();
  await page.keyboard.press("Tab");
  await expect(
    page.getByRole("link", { name: "Skip to content" }),
  ).toBeFocused();
  await page.screenshot({
    path: "test-results/public-mobile.png",
    fullPage: true,
  });
});

test("acessibilidade automatizada no público e login", async ({ page }) => {
  const { default: AxeBuilder } = await import("@axe-core/playwright");
  for (const path of ["/pt-BR", "/pt-BR/projects", "/admin/login"]) {
    await page.goto(path);
    await page.waitForLoadState("networkidle");
    const result = await new AxeBuilder({ page })
      .withTags(["wcag2a", "wcag2aa", "wcag21aa", "wcag22aa"])
      .analyze();
    expect(result.violations).toEqual([]);
  }
  await page.goto("/pt-BR");
  await page.waitForLoadState("networkidle");
  await page.screenshot({
    path: "test-results/public-desktop.png",
    fullPage: true,
  });
});

test("admin, vínculos, perfis, settings e analytics reais", async ({
  page,
  request,
}) => {
  await page.goto("/admin/login");
  await page.getByLabel("Usuário").fill(user!);
  await page.getByLabel("Senha", { exact: true }).fill(password!);
  await page.getByRole("button", { name: "Entrar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin$/);
  const projectName = `Projeto vinculado E2E ${tag}`;
  await page.goto("/admin/projects/new");
  await page
    .getByLabel("Slug único (minúsculas e hífens)")
    .fill(`profile-project-${tag}`);
  await page.getByLabel("Título *", { exact: true }).fill(projectName);
  await page.getByLabel("Publicado", { exact: true }).check();
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/projects$/);
  await page.goto("/admin/skills/new");
  await page
    .getByLabel("Slug único (minúsculas e hífens)")
    .fill(`skill-e2e-${tag}`);
  await page
    .getByLabel("Nome *", { exact: true })
    .fill(`Competência E2E ${tag}`);
  await page.getByLabel("Publicado", { exact: true }).check();
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/skills$/);
  await page.goto("/admin/experiences/new");
  await page.getByLabel("Organização *").fill("Organização de teste E2E");
  await page
    .getByLabel("Cargo ou função *", { exact: true })
    .fill(`Função E2E ${tag}`);
  await page.getByLabel("Publicado", { exact: true }).check();
  await page
    .getByLabel("Competências relacionadas", { exact: true })
    .selectOption({ label: `Competência E2E ${tag}` });
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/experiences$/);
  await page.goto("/admin/profiles/new");
  await page
    .getByLabel("Slug único (minúsculas e hífens)")
    .fill(`profile-e2e-${tag}`);
  await page
    .getByLabel("Título *", { exact: true })
    .fill(`Perfil de teste ${tag}`);
  await page.getByLabel("Ativo", { exact: true }).check();
  await page
    .getByLabel("Competências relacionadas", { exact: true })
    .selectOption({ label: `Competência E2E ${tag}` });
  await page
    .getByLabel("Projetos em destaque", { exact: true })
    .selectOption({ label: projectName });
  await page.getByRole("button", { name: "Salvar", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/profiles$/);
  await page.goto("/pt-BR");
  await page
    .getByLabel("Perfil profissional", { exact: true })
    .selectOption({ label: `Perfil de teste ${tag}` });
  await expect(
    page.getByRole("heading", { name: `Perfil de teste ${tag}`, exact: true }),
  ).toBeVisible();
  await page
    .getByRole("link", { name: "Explorar projetos", exact: true })
    .click();
  await expect(page.getByRole("heading", { name: projectName })).toBeVisible();
  await page
    .getByLabel("Perfil profissional", { exact: true })
    .selectOption("");
  await page.goto("/admin/settings");
  await page.getByLabel("Idioma padrão", { exact: true }).selectOption("en");
  await page
    .getByLabel("Habilitar analytics agregado", { exact: true })
    .check();
  await page.getByRole("button", { name: "Salvar configurações" }).click();
  await expect(page.getByText("Configurações salvas.")).toBeVisible();
  await page.goto("/pt-BR/contact");
  await page.waitForLoadState("networkidle");
  await page.goto("/admin/analytics");
  await expect(
    page.getByText("Coleta desativada.", { exact: false }),
  ).not.toBeVisible();
  const csrfResponse = await page.request.get("/api/v1/auth/csrf");
  const token = (await csrfResponse.json()) as { token: string };
  const summary = await page.request.get("/api/v1/admin/analytics");
  expect((await summary.json()).totalViews).toBeGreaterThan(0);
  expect((await request.get("/api/v1/admin/files")).status()).toBe(401);
  const spec = await page.request.get("http://localhost:8080/v3/api-docs");
  expect(spec.status()).toBe(200);
  expect(Object.keys((await spec.json()).paths)).toContain(
    "/api/v1/admin/projects",
  );
  const invalid = await page.request.post("/api/v1/admin/projects", {
    headers: { "X-CSRF-TOKEN": token.token },
    data: { slug: "invalid-only" },
  });
  expect(invalid.status()).toBe(400);
  await page.evaluate(() => localStorage.removeItem("safe-locale"));
  await page.goto("/");
  await expect(page).toHaveURL(/\/en$/);
  await page.goto("/admin/files");
  await expect(
    page.getByRole("heading", { name: "Arquivos", exact: true }),
  ).toBeVisible();
  await page.getByRole("button", { name: "Sair", exact: true }).click();
});
