package me.hker.reward

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.impl.RewardSummaryServiceImpl
import me.hker.module.user.entity.User
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RewardSummaryServiceImplTest {
    private val userService = mock<UserService>()
    private val creditService = mock<CreditService>()
    private val promoCodeRedemptionMapper = mock<PromoCodeRedemptionMapper>()
    private val inviteCodeRedemptionMapper = mock<InviteCodeRedemptionMapper>()

    private val service = RewardSummaryServiceImpl(
        userService = userService,
        creditService = creditService,
        promoCodeRedemptionMapper = promoCodeRedemptionMapper,
        inviteCodeRedemptionMapper = inviteCodeRedemptionMapper,
    )

    @Test
    fun `summary should aggregate invite and promo redemption data`() {
        whenever(userService.findById(1L)).thenReturn(
            user(
                id = 1L,
                displayName = "Alice",
                inviteCode = "INV-ALICE1",
                creditBalance = 90,
            ),
        )
        whenever(creditService.getBalance(1L)).thenReturn(90)
        whenever(promoCodeRedemptionMapper.selectCount(any<QueryWrapper<PromoCodeRedemption>>())).thenReturn(2)
        whenever(inviteCodeRedemptionMapper.selectList(any<QueryWrapper<InviteCodeRedemption>>())).thenReturn(
            listOf(
                InviteCodeRedemption().apply {
                    inviterUserId = 1L
                    inviterReward = 20
                },
                InviteCodeRedemption().apply {
                    inviterUserId = 1L
                    inviterReward = 20
                },
            ),
        )
        whenever(inviteCodeRedemptionMapper.selectOne(any<QueryWrapper<InviteCodeRedemption>>())).thenReturn(
            InviteCodeRedemption().apply {
                inviteCode = "INV-BOB001"
                inviterUserId = 2L
                inviteeReward = 20
            },
        )
        whenever(userService.findById(2L)).thenReturn(
            user(
                id = 2L,
                displayName = "Bob",
                inviteCode = "INV-BOB001",
                creditBalance = 100,
            ),
        )

        val summary = service.summary(1L)

        assertEquals(90, summary.balance)
        assertEquals("INV-ALICE1", summary.inviteCode)
        assertEquals(2, summary.inviteStats.inviterRedemptions)
        assertEquals(40, summary.inviteStats.totalInviterCredits)
        assertEquals(2, summary.promoRedemptions)
        assertEquals("Bob", summary.inviteRedemption?.inviterDisplayName)
    }

    private fun user(
        id: Long,
        displayName: String,
        inviteCode: String,
        creditBalance: Int,
    ) = User().apply {
        this.id = id
        this.email = "${displayName.lowercase()}@example.com"
        this.displayName = displayName
        this.inviteCode = inviteCode
        this.creditBalance = creditBalance
    }
}
