import { preview } from "vite";
import { chromium } from "@playwright/test";
import { readFile, writeFile, mkdir } from "node:fs/promises";
const api = process.env.PRERENDER_API_URL || "http://localhost:8080";
const site = process.env.VITE_SITE_URL;
const routes = [
  "",
  "projects",
  "certificates",
  "experience",
  "skills",
  "resume",
  "contact",
];
for (const kind of ["projects", "certificates"]) {
  let page = 0;
  let last = 1;
  while (page < last) {
    const res = await fetch(
      `${api}/api/v1/public/${kind}?page=${page}&size=100`,
    );
    if (!res.ok) throw new Error(`Public API ${kind}: HTTP ${res.status}`);
    const data = await res.json();
    last = data.totalPages;
    for (const item of data.content) {
      if (!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(item.slug))
        throw new Error("Invalid public slug");
      routes.push(`${kind}/${item.slug}`);
    }
    page++;
  }
}
const server = await preview({
  preview: {
    host: "127.0.0.1",
    port: 4173,
    strictPort: true,
    proxy: { "/api": api },
  },
  logLevel: "error",
});
const browser = await chromium.launch({
  executablePath: process.env.CHROMIUM_PATH || "/usr/bin/chromium",
});
try {
  const template = await readFile("dist/index.html", "utf8");
  for (const locale of ["pt-BR", "en"]) {
    const page = await browser.newPage();
    await page.addInitScript(() => {
      localStorage.setItem("safe-theme", "dark");
      localStorage.removeItem("safe-profile");
    });
    await page.route("**/public/analytics/events", (route) =>
      route.fulfill({ status: 204 }),
    );
    for (const route of routes) {
      const path = `/${locale}${route ? "/" + route : ""}`;
      await page.goto(`http://127.0.0.1:4173${path}`, {
        waitUntil: "networkidle",
      });
      if (await page.getByRole("alert").count())
        throw new Error(`Prerender failed ${path}`);
      const content = await page.locator("#root").innerHTML();
      const head = await page.evaluate(() => ({
        title: document.title,
        metadata: Array.from(
          document.head.querySelectorAll(
            'meta[name="description"],meta[property],link[data-safe-seo]',
          ),
        )
          .map((n) => n.outerHTML)
          .join(""),
      }));
      const escape = (v) =>
        v
          .replaceAll("&", "&amp;")
          .replaceAll("<", "&lt;")
          .replaceAll(">", "&gt;");
      const html = template
        .replace(/<html lang="[^"]*"/, `<html lang="${locale}"`)
        .replace(/<title>.*?<\/title>/, `<title>${escape(head.title)}</title>`)
        .replace("</head>", head.metadata + "</head>")
        .replace('<div id="root"></div>', `<div id="root">${content}</div>`);
      const directory = `dist${path}`;
      await mkdir(directory, { recursive: true });
      await writeFile(`${directory}/index.html`, html);
    }
    await page.close();
  }
  await writeFile(
    "dist/robots.txt",
    "User-agent: *\nDisallow: /admin\n" +
      (site ? `Sitemap: ${new URL("/sitemap.xml", site)}\n` : ""),
  );
  if (site) {
    const urls = ["pt-BR", "en"].flatMap((locale) =>
      routes.map((route) =>
        new URL(`/${locale}${route ? "/" + route : ""}`, site).toString(),
      ),
    );
    await writeFile(
      "dist/sitemap.xml",
      `<?xml version="1.0" encoding="UTF-8"?><urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">${urls.map((url) => `<url><loc>${url.replaceAll("&", "&amp;")}</loc></url>`).join("")}</urlset>`,
    );
  }
  console.log(
    `Prerendered ${routes.length * 2} public pages. No administrative content included.`,
  );
} finally {
  await browser.close();
  await new Promise((resolve) => server.httpServer.close(resolve));
}
