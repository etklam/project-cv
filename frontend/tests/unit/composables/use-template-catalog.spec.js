import { describe, expect, it, beforeEach, vi } from "vitest";
import { useTemplateCatalog } from "@/composables/useTemplateCatalog";
import { listTemplates } from "@/api/template";

vi.mock("@/api/template", () => ({
  listTemplates: vi.fn(),
}));

describe("useTemplateCatalog", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("filters out templates missing from the registry", async () => {
    vi.mocked(listTemplates).mockResolvedValue({
      templates: [
        { key: "minimal", displayName: "Minimal", creditCost: 0 },
        { key: "unknown", displayName: "Unknown", creditCost: 0 },
      ],
    });

    const catalog = useTemplateCatalog();
    await catalog.loadTemplates();

    expect(catalog.templates.value).toHaveLength(2)
    expect(catalog.supportedTemplates.value).toHaveLength(1)
    expect(catalog.supportedTemplates.value[0].key).toBe("minimal")
  });

  it("exposes loading error state when fetch fails", async () => {
    vi.mocked(listTemplates).mockRejectedValue(new Error("network"));

    const catalog = useTemplateCatalog();
    await catalog.loadTemplates();

    expect(catalog.error.value).toBe("Failed to load templates");
  });
})
