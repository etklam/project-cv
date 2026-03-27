import { computed, ref } from "vue";
import { getRewardSummary, redeemCode } from "@/api/reward";

export function useRewardCenter() {
  const summary = ref(null);
  const loading = ref(false);
  const error = ref("");
  const redeemMessage = ref("");
  const redeemStatus = ref("");
  const recentRedemptions = ref([]);

  const showBalance = computed(() => summary.value?.balance ?? 0);
  const promoRedemptionsCount = computed(() => summary.value?.promoRedemptions ?? 0);
  const inviteStats = computed(() => summary.value?.inviteStats || {
    inviterRedemptions: 0,
    totalInviterCredits: 0,
  });
  const inviteRedemption = computed(() => summary.value?.inviteRedemption);

  const loadSummary = async () => {
    loading.value = true;
    error.value = "";
    try {
      summary.value = await getRewardSummary();
      recentRedemptions.value = [];
    } catch (_error) {
      error.value = "Unable to load reward summary";
    } finally {
      loading.value = false;
    }
  };

  const redeem = async (rawCode) => {
    const code = rawCode.trim().toUpperCase();
    if (!code) {
      redeemMessage.value = "Please enter a code.";
      redeemStatus.value = "error";
      return false;
    }

    redeemStatus.value = "pending";
    try {
      const result = await redeemCode({ code });
      const existing = summary.value ?? {};
      const isPromoRedemption = result.type === "PROMO_CODE";
      summary.value = {
        ...existing,
        balance: result.balanceAfter ?? existing.balance,
        promoRedemptions: isPromoRedemption
          ? (existing.promoRedemptions ?? 0) + 1
          : (existing.promoRedemptions ?? 0),
        inviteRedemption: result.type === "INVITE_CODE"
          ? {
              inviteCode: result.code,
              inviterUserId: existing.inviteRedemption?.inviterUserId ?? null,
              inviterDisplayName: existing.inviteRedemption?.inviterDisplayName ?? "",
              inviteeReward: result.inviteeReward ?? 0,
            }
          : existing.inviteRedemption,
      };
      recentRedemptions.value = [result, ...recentRedemptions.value].slice(0, 5);
      redeemMessage.value = result.message || "Reward applied.";
      redeemStatus.value = "success";
      return true;
    } catch (requestError) {
      redeemMessage.value =
        requestError?.response?.data?.message || requestError?.message || "Failed to apply code";
      redeemStatus.value = "error";
      return false;
    }
  };

  return {
    summary,
    loading,
    error,
    redeemMessage,
    redeemStatus,
    recentRedemptions,
    showBalance,
    promoRedemptionsCount,
    inviteStats,
    inviteRedemption,
    loadSummary,
    redeem,
  };
}
