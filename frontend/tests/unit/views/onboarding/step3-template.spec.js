import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi, beforeEach } from "vitest";
import Step3Template from "@/views/onboarding/Step3Template.vue";
import { listTemplates } from "@/api/template";

vi.mock("@/api/template", () => ({
  listTemplates: vi.fn(),
}));

vi.mock("@/components/templates/TemplateCard.vue", () => ({
  __esModule: true,
  default: {
    name: "TemplateCardStub",
    props: ["template", "selected"],
    template: "<div data-testid='template-card-stub'></div>",
  },
}));

describe("Step3Template", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listTemplates).mockResolvedValue({ templates: [] });
  });

  it("renders featured templates and selects the first item", async () => {
    vi.mocked(listTemplates).mockResolvedValue({
      templates: [
        { key: "extra", displayName: "Extra", creditCost: 22 },
        { key: "minimal", displayName: "Minimal", description: "Minimal desc", creditCost: 15 },
        { key: "sidebar", displayName: "Sidebar", description: "Sidebar desc", creditCost: 18 },
        { key: "modern", displayName: "Modern", description: "Modern desc", creditCost: 20 },
      ],
    });

    const wrapper = mount(Step3Template);
    await flushPromises();

    expect(listTemplates).toHaveBeenCalled();
    expect(wrapper.get("[data-testid=template-grid]").exists()).toBe(true);
    expect(wrapper.findAll("[data-testid=template-card-stub]")).toHaveLength(3);
    expect(wrapper.get("[data-testid=selected-template]").text()).toContain("minimal");
  });

  it("displays an error when template fetch fails", async () => {
    vi.mocked(listTemplates).mockRejectedValue(new Error("network"));

    const wrapper = mount(Step3Template);
    await flushPromises();

    expect(wrapper.get("[data-testid=templates-error]").text()).toContain(
      "Failed to load templates",
    );
  });
});
