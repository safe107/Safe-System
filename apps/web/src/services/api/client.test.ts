import { afterEach, describe, it, expect, vi } from "vitest";
import { api, clearCsrf, ApiError } from "./client";
afterEach(() => {
  vi.unstubAllGlobals();
  clearCsrf();
});
describe("Cliente HTTP", () => {
  it("obtém CSRF e inclui sessão em uma mutação", async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ token: "csrf-token" })),
      )
      .mockResolvedValueOnce(new Response(JSON.stringify({ id: "saved" })));
    vi.stubGlobal("fetch", fetch);
    const result = await api<{ id: string }>("/admin/projects", {
      method: "POST",
      body: "{}",
    });
    expect(result.id).toBe("saved");
    expect(fetch.mock.calls[0][0]).toBe("/api/v1/auth/csrf");
    expect(fetch.mock.calls[1][1].credentials).toBe("include");
    expect(
      (fetch.mock.calls[1][1].headers as Headers).get("X-CSRF-TOKEN"),
    ).toBe("csrf-token");
  });
  it("preserva erros de validação do servidor", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            message: "Revise os campos",
            fields: { slug: "Inválido" },
          }),
          { status: 400 },
        ),
      ),
    );
    await expect(api("/public/projects")).rejects.toBeInstanceOf(ApiError);
  });
  it("processa 204 sem tentar ler JSON vazio", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(new Response(null, { status: 204 })),
    );
    expect(await api("/public/settings")).toBeUndefined();
  });
});
