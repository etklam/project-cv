import { describe, expect, it, beforeEach, vi } from "vitest";
import { useRewardCenter } from "@/composables/useRewardCenter";
import { getRewardSummary, redeemCode } from "@/api/reward";

vi.mock("@/api/reward", () => ({
  getRewardSummary: vi.fn(),
  redeemCode: vi.fn(),
}));

describe("useRewardCenter", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("loads reward summary and exposes computed fields", async () => {
    vi.mocked(getRewardSummary).mockResolvedValue({
      balance: 80,
      inviteCode: "INV-ALICE1",
      inviteStats: { inviterRedemptions: 1, totalInviterCredits: 20 },
      promoRedemptions: 2,
      inviteRedemption: null,
    });

    const rewardCenter = useRewardCenter();
    await rewardCenter.loadSummary();

    expect(rewardCenter.showBalance.value).toBe(80);
    expect(rewardCenter.promoRedemptionsCount.value).toBe(2);
    expect(rewardCenter.inviteStats.value.totalInviterCredits).toBe(20);
  });

  it("normalizes redeem codes and updates local summary state", async () => {
    vi.mocked(getRewardSummary).mockResolvedValue({
      balance: 80,
      inviteCode: "INV-ALICE1",
      inviteStats: { inviterRedemptions: 0, totalInviterCredits: 0 },
      promoRedemptions: 2,
      inviteRedemption: null,
    });
    vi.mocked(redeemCode).mockResolvedValue({
      type: "PROMO_CODE",
      code: "WELCOME2026",
      creditedAmount: 30,
      balanceAfter: 110,
      message: "promo code redeemed",
    });

    const rewardCenter = useRewardCenter();
    await rewardCenter.loadSummary();
    await rewardCenter.redeem(" welcome2026 ");

    expect(redeemCode).toHaveBeenCalledWith({ code: "WELCOME2026" });
    expect(rewardCenter.showBalance.value).toBe(110);
    expect(rewardCenter.promoRedemptionsCount.value).toBe(3);
    expect(rewardCenter.recentRedemptions.value).toHaveLength(1);
  });
});
