import { flushPromises, mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import PublicCvView from "@/views/public/PublicCvView.vue";
import { getPublicCv } from "@/api/public";

const routeParams = { email: "alice@example.com", slug: "backend-cv" };

vi.mock("vue-router", () => ({
  useRoute: () => ({ params: routeParams }),
}));

vi.mock("@/api/public", () => ({
  getPublicCv: vi.fn(),
}));

vi.mock("@/components/cv-templates/CvTemplateRenderer.vue", () => ({
  __esModule: true,
  default: {
    name: "CvTemplateRendererStub",
    props: ["templateKey", "cv", "sections", "mode"],
    template: "<div data-testid='template-renderer-stub'></div>",
  },
}));

describe("PublicCvView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    routeParams.email = "alice@example.com";
    routeParams.slug = "backend-cv";
  });

  it("loads public CV and passes payload into template renderer", async () => {
    vi.mocked(getPublicCv).mockResolvedValue({
      user: { email: "alice@example.com", displayName: "Alice" },
      cv: { id: 11, title: "Backend CV", slug: "backend-cv", templateKey: "modern" },
      sections: [{ id: 1, sectionType: "summary", content: { text: "Hello" } }],
    });

    const wrapper = mount(PublicCvView);
    await flushPromises();

    expect(getPublicCv).toHaveBeenCalledWith("alice@example.com", "backend-cv");
    expect(wrapper.get("[data-testid=public-cv-title]").text()).toContain("Backend CV");

    const renderer = wrapper.getComponent({ name: "CvTemplateRendererStub" });
    expect(renderer.props("templateKey")).toBe("modern");
    expect(renderer.props("mode")).toBe("public");
    expect(renderer.props("sections")).toHaveLength(1);
  });

  it("shows error state when public CV loading fails", async () => {
    vi.mocked(getPublicCv).mockRejectedValue(new Error("not found"));

    const wrapper = mount(PublicCvView);
    await flushPromises();

    expect(wrapper.get("[data-testid=public-cv-error]").text()).toContain("not found");
  });
});
