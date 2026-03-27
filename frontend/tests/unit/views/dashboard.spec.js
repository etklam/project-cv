import { flushPromises, mount } from "@vue/test-utils";
import { describe, expect, it, vi, beforeEach } from "vitest";
import DashboardView from "@/views/dashboard/DashboardView.vue";
import { getRewardSummary, redeemCode } from "@/api/reward";
import { listTemplates } from "@/api/template";

vi.mock("@/api/reward", () => ({
  getRewardSummary: vi.fn(),
  redeemCode: vi.fn(),
}));

vi.mock("@/api/template", () => ({
  listTemplates: vi.fn(),
}));

vi.mock("@/components/templates/TemplateCard.vue", () => ({
  __esModule: true,
  default: {
    name: "TemplateCardStub",
    props: ["template"],
    template: "<div data-testid='template-card-stub'></div>",
  },
}));

describe("DashboardView", () => {
  const summaryPayload = {
    balance: 120,
    inviteCode: "INV-SAMPLE",
    inviteStats: { inviterRedemptions: 2, totalInviterCredits: 40 },
    promoRedemptions: 1,
    inviteRedemption: {
      inviteCode: "INV-ALICE",
      inviterUserId: 2,
      inviterDisplayName: "Alice",
      inviteeReward: 20,
    },
  };

  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(listTemplates).mockResolvedValue({ templates: [] });
  });

  it("loads summary and renders balance details", async () => {
    vi.mocked(getRewardSummary).mockResolvedValue(summaryPayload);
    vi.mocked(listTemplates).mockResolvedValue({ templates: [] });
    const wrapper = mount(DashboardView);
    await flushPromises();

    expect(wrapper.get("[data-testid=reward-balance]").text()).toContain("120");
    expect(wrapper.get("[data-testid=reward-invite-code]").text()).toContain("INV-SAMPLE");
    expect(wrapper.get("[data-testid=promo-redemption-count]").text()).toContain("1");
    expect(wrapper.findAll("[data-testid=promo-record]")).toHaveLength(0);
  });

  it("redeems a code and shows message", async () => {
    vi.mocked(getRewardSummary).mockResolvedValue(summaryPayload);
    vi.mocked(redeemCode).mockResolvedValue({
      type: "PROMO_CODE",
      code: "WINTER",
      creditedAmount: 15,
      balanceAfter: 135,
      message: "Applied reward",
    });

    const wrapper = mount(DashboardView);
    await flushPromises();

    const input = wrapper.get("[data-testid=redeem-input]");
    await input.setValue("winter");
    await wrapper.get("[data-testid=redeem-button]").trigger("click");
    await flushPromises();

    expect(vi.mocked(redeemCode)).toHaveBeenCalledWith({ code: "WINTER" });
    expect(wrapper.get("[data-testid=reward-balance]").text()).toContain("135");
    expect(wrapper.get("[data-testid=redeem-message]").text()).toContain("Applied reward");
    expect(wrapper.findAll("[data-testid=promo-record]")).toHaveLength(1);
    expect(wrapper.get("[data-testid=promo-redemption-count]").text()).toContain("2");
  });
});
