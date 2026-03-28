import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import PrintCvView from "@/views/print/PrintCvView.vue";
import { getCv } from "@/api/cv";

const routeParams = { id: "7" };

vi.mock("vue-router", () => ({
  useRoute: () => ({ params: routeParams }),
}));

vi.mock("@/api/cv", () => ({
  getCv: vi.fn(),
}));

vi.mock("@/components/cv-templates/CvTemplateRenderer.vue", () => ({
  __esModule: true,
  default: {
    name: "CvTemplateRendererStub",
    props: ["templateKey", "cv", "sections", "mode"],
    template: "<div data-testid='template-renderer-stub'></div>",
  },
}));

describe("PrintCvView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    routeParams.id = "7";
  });

  it("loads cv detail and renders print mode template", async () => {
    vi.mocked(getCv).mockResolvedValue({
      cv: { id: 7, title: "Print Resume", templateKey: "modern" },
      sections: [{ id: 1, sectionType: "summary", content: { text: "Hello" } }],
    });

    const wrapper = mount(PrintCvView);
    await flushPromises();

    expect(getCv).toHaveBeenCalledWith("7");
    const renderer = wrapper.getComponent({ name: "CvTemplateRendererStub" });
    expect(renderer.props("templateKey")).toBe("modern");
    expect(renderer.props("mode")).toBe("print");
  });

  it("shows error state when cv load fails", async () => {
    vi.mocked(getCv).mockRejectedValue(new Error("network down"));

    const wrapper = mount(PrintCvView);
    await flushPromises();

    expect(wrapper.get("[data-testid=print-error]").text()).toContain("network down");
  });
});
