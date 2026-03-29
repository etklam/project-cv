import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Step2ProfessionInfo from "@/views/onboarding/Step2ProfessionInfo.vue";

const push = vi.fn();
const applyUser = vi.fn();
const submitStep2 = vi.fn();
const skipStep2 = vi.fn();

vi.mock("vue-router", () => ({
  useRouter: () => ({ push }),
}));

vi.mock("@/stores/auth", () => ({
  useAuthStore: () => ({ applyUser }),
}));

vi.mock("@/api/onboarding", () => ({
  submitStep2: (...args) => submitStep2(...args),
  skipStep2: (...args) => skipStep2(...args),
}));

describe("Step2ProfessionInfo", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders the professional context flow and continues to step 3", async () => {
    submitStep2.mockResolvedValue({ user: { onboarding_status: "STEP_3" } });
    const wrapper = mount(Step2ProfessionInfo);

    expect(wrapper.get("[data-testid=view-onboarding-step2]").text()).toContain("Tell us where your next role should move you");
    expect(wrapper.get("[data-testid=onboarding-industry-select]").element.value).toBe("Technology & Software");
    expect(wrapper.get("[data-testid=onboarding-years-select]").element.value).toBe("3-5 Years (Mid)");
    expect(wrapper.get("[data-testid=onboarding-leadership-individual]").element.checked).toBe(true);
    expect(wrapper.get("[data-testid=onboarding-target-label]").text()).toContain("What role are you growing into next");

    await wrapper.get("[data-testid=onboarding-target-title]").setValue("Staff Product Designer");
    await wrapper.get("[data-testid=onboarding-leadership-manager]").setValue();
    await wrapper.get("form").trigger("submit");

    expect(submitStep2).toHaveBeenCalled();
    expect(applyUser).toHaveBeenCalledWith({ onboarding_status: "STEP_3" });
    expect(push).toHaveBeenCalledWith("/onboarding/step3");
  });

  it("changes the questions when the experience tier changes", async () => {
    const wrapper = mount(Step2ProfessionInfo);

    await wrapper.get("[data-testid=onboarding-years-select]").setValue("0-2 Years (Entry)");
    expect(wrapper.get("[data-testid=onboarding-experience-badge]").text()).toContain("Early career");
    expect(wrapper.get("[data-testid=onboarding-target-label]").text()).toContain("trying to land first");
    expect(wrapper.get("[data-testid=onboarding-leadership-label]").text()).toContain("How do you want to contribute");
    expect(wrapper.get("[data-testid=onboarding-why-copy]").text()).toContain("learning velocity");

    await wrapper.get("[data-testid=onboarding-years-select]").setValue("10+ Years (Expert)");
    expect(wrapper.get("[data-testid=onboarding-experience-badge]").text()).toContain("Executive track");
    expect(wrapper.get("[data-testid=onboarding-target-label]").text()).toContain("mandate, title, or portfolio");
    expect(wrapper.get("[data-testid=onboarding-why-copy]").text()).toContain("scale, transformation, and senior decision-making");
  });
});
