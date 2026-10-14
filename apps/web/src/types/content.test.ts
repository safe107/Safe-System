import { describe, it, expect } from "vitest";
import {
  translated,
  hasTranslationFallback,
  contentName,
  type Content,
} from "./content";
describe("Conteúdo bilíngue", () => {
  it("preserva conteúdo PT-BR quando EN está parcial e sinaliza o fallback", () => {
    const item: Content = {
      id: "test",
      displayOrder: 0,
      createdAt: "2026-01-01",
      updatedAt: "2026-01-01",
      translations: {
        "pt-BR": {
          title: "Título confirmado",
          description: "Descrição confirmada",
        },
        en: { title: "Confirmed title", description: "" },
      },
    };
    expect(translated(item, "en")).toEqual({
      title: "Confirmed title",
      description: "Descrição confirmada",
    });
    expect(hasTranslationFallback(item, "en")).toBe(true);
    expect(hasTranslationFallback(item, "pt-BR")).toBe(false);
    expect(contentName(item, "en")).toBe("Confirmed title");
  });
});
