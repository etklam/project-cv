package me.hker.reward

import me.hker.module.reward.dto.InviteStatsDto
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.service.RewardCodeRedemptionService
import me.hker.module.reward.service.RewardSummaryService
import me.hker.module.reward.service.impl.RewardServiceImpl
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RewardServiceImplTest {
    private val rewardSummaryService = mock<RewardSummaryService>()
    private val rewardCodeRedemptionService = mock<RewardCodeRedemptionService>()

    private val service = RewardServiceImpl(
        rewardSummaryService = rewardSummaryService,
        rewardCodeRedemptionService = rewardCodeRedemptionService,
    )

    @Test
    fun `summary should delegate to reward summary service`() {
        val expected = RewardSummaryDto(
            balance = 90,
            inviteCode = "INV-ALICE1",
            inviteStats = InviteStatsDto(
                inviterRedemptions = 2,
                totalInviterCredits = 40,
            ),
            promoRedemptions = 1,
            inviteRedemption = null,
        )
        whenever(rewardSummaryService.summary(1L)).thenReturn(expected)

        val result = service.summary(1L)

        assertSame(expected, result)
        verify(rewardSummaryService).summary(1L)
    }

    @Test
    fun `redeem should delegate to reward code redemption service`() {
        val expected = RewardRedeemResultDto(
            type = "PROMO_CODE",
            code = "WELCOME2026",
            creditedAmount = 30,
            balanceAfter = 80,
            inviterReward = null,
            inviteeReward = null,
            message = "promo code redeemed",
        )
        whenever(rewardCodeRedemptionService.redeem(1L, "welcome2026")).thenReturn(expected)

        val result = service.redeem(1L, "welcome2026")

        assertSame(expected, result)
        verify(rewardCodeRedemptionService).redeem(1L, "welcome2026")
    }
}
