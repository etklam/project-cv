package me.hker.reward

import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.service.InviteCodeRewardRedemptionService
import me.hker.module.reward.service.PromoCodeRewardRedemptionService
import me.hker.module.reward.service.impl.RewardCodeRedemptionServiceImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RewardCodeRedemptionServiceImplTest {
    private val promoCodeRewardRedemptionService = mock<PromoCodeRewardRedemptionService>()
    private val inviteCodeRewardRedemptionService = mock<InviteCodeRewardRedemptionService>()

    private val service = RewardCodeRedemptionServiceImpl(
        promoCodeRewardRedemptionService = promoCodeRewardRedemptionService,
        inviteCodeRewardRedemptionService = inviteCodeRewardRedemptionService,
    )

    @Test
    fun `redeem should reject blank code`() {
        val error = assertThrows(IllegalArgumentException::class.java) {
            service.redeem(1L, "   ")
        }

        assertEquals("reward code is required", error.message)
    }

    @Test
    fun `redeem should normalize and route invite codes`() {
        val expected = RewardRedeemResultDto(
            type = "INVITE_CODE",
            code = "INV-BOB001",
            creditedAmount = 20,
            balanceAfter = 70,
            inviterReward = 20,
            inviteeReward = 20,
            message = "invite code redeemed",
        )
        whenever(inviteCodeRewardRedemptionService.redeem(1L, "INV-BOB001")).thenReturn(expected)

        val result = service.redeem(1L, " inv-bob001 ")

        assertSame(expected, result)
        verify(inviteCodeRewardRedemptionService).redeem(1L, "INV-BOB001")
    }

    @Test
    fun `redeem should normalize and route promo codes`() {
        val expected = RewardRedeemResultDto(
            type = "PROMO_CODE",
            code = "WELCOME2026",
            creditedAmount = 30,
            balanceAfter = 80,
            inviterReward = null,
            inviteeReward = null,
            message = "promo code redeemed",
        )
        whenever(promoCodeRewardRedemptionService.redeem(1L, "WELCOME2026")).thenReturn(expected)

        val result = service.redeem(1L, " welcome2026 ")

        assertSame(expected, result)
        verify(promoCodeRewardRedemptionService).redeem(1L, "WELCOME2026")
    }
}
