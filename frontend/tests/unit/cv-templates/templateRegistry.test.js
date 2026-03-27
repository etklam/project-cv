import { describe, expect, it } from "vitest";
import {
  TEMPLATE_KEYS,
  getTemplateMeta,
  resolveTemplateComponent,
} from "@/components/cv-templates/templateRegistry";

describe("templateRegistry", () => {
  it("registers core templates", () => {
    expect(TEMPLATE_KEYS).toEqual(["minimal", "sidebar", "modern"]);
  });

  it("resolves existing template component", () => {
    const component = resolveTemplateComponent("minimal");
    expect(component?.name).toBe("MinimalTemplate");
  });

  it("falls back to MissingTemplate for unknown key", () => {
    const component = resolveTemplateComponent("non-existent");
    expect(component?.name).toBe("MissingTemplate");
  });

  it.todo("enforces supportsPrint contract for print mode rendering");
  it.todo("aligns registry keys with backend templates.component_key contract check");

  it("returns template metadata for onboarding/dashboard cards", () => {
    const meta = getTemplateMeta("modern");
    expect(meta).toMatchObject({
      key: "modern",
      supportsPrint: true,
    });
  });
});
