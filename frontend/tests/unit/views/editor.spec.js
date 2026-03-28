import { createPinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import CvEditorView from "@/views/editor/CvEditorView.vue";
import { exportCvPdf } from "@/api/export";
import { getCv, updateCv, updateSections } from "@/api/cv";
import { listTemplates } from "@/api/template";

const routeParams = { id: "11" };

vi.mock("vue-router", () => ({
  useRoute: () => ({ params: routeParams }),
}));

vi.mock("@/api/cv", () => ({
  getCv: vi.fn(),
  updateCv: vi.fn(),
  updateSections: vi.fn(),
}));

vi.mock("@/api/template", () => ({
  listTemplates: vi.fn(),
}));

vi.mock("@/api/export", () => ({
  exportCvPdf: vi.fn(),
}));

vi.mock("@/components/cv-templates/CvTemplateRenderer.vue", () => ({
  __esModule: true,
  default: {
    name: "CvTemplateRendererStub",
    props: ["templateKey", "cv", "sections", "mode"],
    template: "<div data-testid='template-renderer-stub'></div>",
  },
}));

describe("CvEditorView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    routeParams.id = "11";
    vi.mocked(listTemplates).mockResolvedValue({
      templates: [
        { key: "minimal", displayName: "Minimal" },
        { key: "modern", displayName: "Modern" },
      ],
    });
    vi.mocked(exportCvPdf).mockResolvedValue(new Blob(["pdf"]));
  });

  it("loads cv metadata into editor form", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "modern",
        isPublic: true,
        slug: "initial-cv",
      },
      sections: [{ id: 1, sectionType: "summary", content: { text: "Intro" } }],
    });

    const wrapper = mount(CvEditorView, {
      global: {
        plugins: [createPinia()],
      },
    });
    await flushPromises();

    expect(getCv).toHaveBeenCalledWith("11");
    expect(wrapper.get("[data-testid=editor-title-input]").element.value).toBe("Initial CV");
    expect(wrapper.get("[data-testid=editor-slug-input]").element.value).toBe("initial-cv");
    expect(wrapper.get("[data-testid=editor-template-select]").element.value).toBe("modern");

    const renderer = wrapper.getComponent({ name: "CvTemplateRendererStub" });
    expect(renderer.props("templateKey")).toBe("modern");
    expect(renderer.props("mode")).toBe("editor-preview");
  });

  it("saves metadata with normalized slug", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "minimal",
        isPublic: false,
        slug: null,
      },
      sections: [],
    });
    vi.mocked(updateCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Updated CV",
        templateKey: "modern",
        isPublic: true,
        slug: "my-cv-2026",
      },
      sections: [],
    });

    const wrapper = mount(CvEditorView, {
      global: {
        plugins: [createPinia()],
      },
    });
    await flushPromises();

    await wrapper.get("[data-testid=editor-title-input]").setValue("Updated CV");
    await wrapper.get("[data-testid=editor-template-select]").setValue("modern");
    await wrapper.get("[data-testid=editor-public-toggle]").setValue(true);
    await flushPromises();
    await wrapper.get("[data-testid=editor-slug-input]").setValue("  My CV 2026  ");
    await wrapper.get("[data-testid=editor-form]").trigger("submit");
    await flushPromises();

    expect(updateCv).toHaveBeenCalledWith("11", {
      title: "Updated CV",
      templateKey: "modern",
      isPublic: true,
      slug: "my-cv-2026",
    });
    expect(wrapper.get("[data-testid=editor-save-message]").text()).toContain("Saved");
    expect(wrapper.get("[data-testid=editor-public-path]").text()).toContain("my-cv-2026");
  });

  it("filters out templates that are missing from the frontend registry", async () => {
    vi.mocked(listTemplates).mockResolvedValue({
      templates: [
        { key: "minimal", displayName: "Minimal" },
        { key: "ghost", displayName: "Ghost" },
      ],
    });
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "minimal",
        isPublic: false,
        slug: null,
      },
      sections: [],
    });

    const wrapper = mount(CvEditorView, {
      global: {
        plugins: [createPinia()],
      },
    });
    await flushPromises();

    const options = wrapper
      .findAll("[data-testid=editor-template-select] option")
      .map((option) => option.element.value);

    expect(options).toEqual(["minimal"]);
  });

  it("saves updated sections through the cv store", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "minimal",
        isPublic: false,
        slug: null,
      },
      sections: [{ id: 1, sectionType: "summary", sortOrder: 0, title: "Summary", content: { text: "Intro" } }],
    });
    vi.mocked(updateSections).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "minimal",
        isPublic: false,
        slug: null,
      },
      sections: [{ id: 1, sectionType: "summary", sortOrder: 0, title: "Summary", content: { text: "Updated intro" } }],
    });

    const wrapper = mount(CvEditorView, {
      global: {
        plugins: [createPinia()],
      },
    });
    await flushPromises();

    await wrapper.get("[data-testid=section-summary-input]").setValue("Updated intro");
    await wrapper.get("[data-testid=editor-save-sections]").trigger("click");
    await flushPromises();

    expect(updateSections).toHaveBeenCalledWith("11", {
      sections: [{ id: 1, sectionType: "summary", sortOrder: 0, title: "Summary", content: { text: "Updated intro" } }],
    });
    expect(wrapper.get("[data-testid=editor-sections-message]").text()).toContain("Sections saved");
  });

  it("calls export api when export button is clicked", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "minimal",
        isPublic: false,
        slug: null,
      },
      sections: [],
    });

    const wrapper = mount(CvEditorView, {
      global: {
        plugins: [createPinia()],
      },
    });
    await flushPromises();

    await wrapper.get("[data-testid=editor-export-button]").trigger("click");
    await flushPromises();

    expect(exportCvPdf).toHaveBeenCalledWith("11");
    expect(wrapper.get("[data-testid=editor-export-message]").text()).toContain("PDF downloaded successfully");
  });
});
