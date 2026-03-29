import { createPinia } from "pinia";
import { flushPromises, mount } from "@vue/test-utils";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import CvEditorView from "@/views/editor/CvEditorView.vue";
import { exportCvPdf } from "@/api/export";
import { getCv, saveCvDraft, updateCv, updateSections } from "@/api/cv";
import { listTemplates } from "@/api/template";

const routeParams = { id: "11" };

vi.mock("vue-router", () => ({
  useRoute: () => ({ params: routeParams }),
}));

vi.mock("@/api/cv", () => ({
  getCv: vi.fn(),
  saveCvDraft: vi.fn(),
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

const mountEditor = () =>
  mount(CvEditorView, {
    global: {
      plugins: [createPinia()],
    },
  });

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

  afterEach(() => {
    vi.useRealTimers();
  });

  it("renders the workspace shell with toolbar, form pane, and preview pane", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: {
        id: 11,
        title: "Initial CV",
        templateKey: "modern",
        isPublic: true,
        slug: "initial-cv",
      },
      sections: [
        {
          id: 9,
          sectionType: "contact",
          title: "Contact",
          content: {
            displayName: "Ada Lovelace",
            headline: "Staff Engineer",
            email: "ada@example.com",
            location: "London",
            website: "https://ada.dev",
          },
        },
        { id: 1, sectionType: "summary", content: { text: "Intro" } },
      ],
    });

    const wrapper = mountEditor();
    await flushPromises();

    expect(getCv).toHaveBeenCalledWith("11");
    expect(wrapper.get("[data-testid=editor-toolbar]").exists()).toBe(true);
    expect(wrapper.get("[data-testid=editor-form-pane]").exists()).toBe(true);
    expect(wrapper.get("[data-testid=editor-preview-pane]").exists()).toBe(true);
    expect(wrapper.get("[data-testid=editor-toolbar-title]").text()).toContain("Initial CV");
    expect(wrapper.get("[data-testid=editor-autosave-status]").text()).toContain("Autosaved");
    expect(wrapper.get("[data-testid=editor-preview-zoom-label]").text()).toContain("85%");
    expect(wrapper.get("[data-testid=editor-title-input]").element.value).toBe("Initial CV");
    expect(wrapper.get("[data-testid=editor-slug-input]").element.value).toBe("initial-cv");
    expect(wrapper.get("[data-testid=editor-contact-display-name]").element.value).toBe("Ada Lovelace");
    expect(wrapper.get("[data-testid=editor-contact-email]").element.value).toBe("ada@example.com");

    const renderer = wrapper.getComponent({ name: "CvTemplateRendererStub" });
    expect(renderer.props("templateKey")).toBe("modern");
    expect(renderer.props("mode")).toBe("editor-preview");
    expect(renderer.props("sections")).toEqual(
      expect.arrayContaining([
        expect.objectContaining({
          sectionType: "contact",
          content: expect.objectContaining({
            displayName: "Ada Lovelace",
          }),
        }),
      ]),
    );
  });

  it("zooms the preview canvas in and out from the preview controls", async () => {
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

    const wrapper = mountEditor();
    await flushPromises();

    expect(wrapper.get("[data-testid=editor-preview-zoom-label]").text()).toContain("85%");

    await wrapper.get("[data-testid=editor-preview-zoom-in]").trigger("click");
    await flushPromises();
    expect(wrapper.get("[data-testid=editor-preview-zoom-label]").text()).toContain("90%");

    await wrapper.get("[data-testid=editor-preview-zoom-out]").trigger("click");
    await flushPromises();
    expect(wrapper.get("[data-testid=editor-preview-zoom-label]").text()).toContain("85%");
  });

  it("autosaves metadata changes and updates the toolbar status text", async () => {
    vi.useFakeTimers();
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
    vi.mocked(saveCvDraft).mockResolvedValue({
      cv: {
        id: 11,
        title: "Updated CV",
        templateKey: "modern",
        isPublic: true,
        slug: "my-cv-2026",
        email: "ada@example.com",
      },
      sections: [],
    });

    const wrapper = mountEditor();
    await flushPromises();

    await wrapper.get("[data-testid=editor-title-input]").setValue("Updated CV");
    await wrapper.get("[data-testid=editor-tab-style]").trigger("click");
    await flushPromises();
    await wrapper.get("[data-testid=editor-style-template-modern]").trigger("click");
    await wrapper.get("[data-testid=editor-tab-preview]").trigger("click");
    await wrapper.get("[data-testid=editor-public-toggle]").setValue(true);
    await flushPromises();
    await wrapper.get("[data-testid=editor-slug-input]").setValue("  My CV 2026  ");
    await flushPromises();

    await vi.advanceTimersByTimeAsync(800);
    await flushPromises();

    expect(saveCvDraft).toHaveBeenCalledWith("11", {
      title: "Updated CV",
      templateKey: "modern",
      isPublic: true,
      slug: "my-cv-2026",
      sections: [],
    });
    expect(wrapper.get("[data-testid=editor-autosave-status]").text()).toContain("Autosaved");
    expect(wrapper.get("[data-testid=editor-autosave-status]").text()).toContain("just now");
  });

  it("keeps metadata save behavior and the public path intact", async () => {
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
        email: "ada@example.com",
      },
      sections: [],
    });

    const wrapper = mountEditor();
    await flushPromises();

    await wrapper.get("[data-testid=editor-title-input]").setValue("Updated CV");
    await wrapper.get("[data-testid=editor-tab-style]").trigger("click");
    await flushPromises();
    await wrapper.get("[data-testid=editor-style-template-modern]").trigger("click");
    await wrapper.get("[data-testid=editor-tab-preview]").trigger("click");
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

    const wrapper = mountEditor();
    await flushPromises();

    await wrapper.get("[data-testid=editor-tab-style]").trigger("click");
    await flushPromises();

    expect(wrapper.find("[data-testid=editor-style-template-minimal]").exists()).toBe(true);
    expect(wrapper.find("[data-testid=editor-style-template-ghost]").exists()).toBe(false);
  });

  it("keeps export available from the editor workspace", async () => {
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

    const wrapper = mountEditor();
    await flushPromises();

    await wrapper.get("[data-testid=editor-export-button]").trigger("click");
    await flushPromises();

    expect(exportCvPdf).toHaveBeenCalledWith("11");
    expect(wrapper.get("[data-testid=editor-export-message]").text()).toContain("PDF downloaded successfully");
  });

  it("shows style controls and updates preview status from the style workspace", async () => {
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

    const wrapper = mountEditor();
    await flushPromises();

    await wrapper.get("[data-testid=editor-tab-style]").trigger("click");
    await flushPromises();
    await wrapper.get("[data-testid=editor-style-template-modern]").trigger("click");
    await wrapper.get("[data-testid=editor-style-accent-ocean]").trigger("click");
    await wrapper.get("[data-testid=editor-style-density-compact]").trigger("click");
    await flushPromises();

    expect(wrapper.get("[data-testid=editor-style-pane]").exists()).toBe(true);
    expect(wrapper.get("[data-testid=editor-style-preview-status]").text()).toContain("modern");
    expect(wrapper.get("[data-testid=editor-style-preview-status]").text()).toContain("ocean");
    expect(wrapper.get("[data-testid=editor-style-preview-status]").text()).toContain("compact");
  });

  it("pushes template changes from style workspace into the live preview renderer", async () => {
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

    const wrapper = mountEditor();
    await flushPromises();

    expect(wrapper.getComponent({ name: "CvTemplateRendererStub" }).props("templateKey")).toBe("minimal");

    await wrapper.get("[data-testid=editor-tab-style]").trigger("click");
    await flushPromises();
    await wrapper.get("[data-testid=editor-style-template-modern]").trigger("click");
    await flushPromises();

    expect(wrapper.getComponent({ name: "CvTemplateRendererStub" }).props("templateKey")).toBe("modern");
  });
});
