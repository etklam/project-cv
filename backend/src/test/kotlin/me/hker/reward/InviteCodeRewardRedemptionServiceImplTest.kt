package me.hker.reward

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.config.AppBusinessProperties
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.service.impl.InviteCodeRewardRedemptionServiceImpl
import me.hker.module.user.entity.User
import me.hker.module.user.mapper.UserMapper
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class InviteCodeRewardRedemptionServiceImplTest {
    private val userService = mock<UserService>()
    private val creditService = mock<CreditService>()
    private val inviteCodeRedemptionMapper = mock<InviteCodeRedemptionMapper>()
    private val userMapper = mock<UserMapper>()
    private val businessProperties = AppBusinessProperties(
        reward = AppBusinessProperties.Reward(
            invite = AppBusinessProperties.Reward.Invite(
                inviterBonus = 20,
                inviteeBonus = 20,
            ),
        ),
    )

    private val service = InviteCodeRewardRedemptionServiceImpl(
        userService = userService,
        creditService = creditService,
        inviteCodeRedemptionMapper = inviteCodeRedemptionMapper,
        userMapper = userMapper,
        businessProperties = businessProperties,
    )

    @Test
    fun `redeem should reject self invite code`() {
        whenever(userService.findById(1L)).thenReturn(
            user(
                id = 1L,
                displayName = "Alice",
                inviteCode = "INV-ALICE1",
                creditBalance = 50,
            ),
        )

        val error = assertThrows(IllegalArgumentException::class.java) {
            service.redeem(1L, "INV-ALICE1")
        }

        assertEquals("cannot redeem your own invite code", error.message)
        verify(creditService, never()).credit(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `redeem should reward inviter and invitee for valid invite code`() {
        whenever(userService.findById(1L)).thenReturn(
            user(
                id = 1L,
                displayName = "Alice",
                inviteCode = "INV-ALICE1",
                creditBalance = 50,
            ),
        )
        whenever(userMapper.selectForUpdate("INV-BOB001")).thenReturn(
            user(
                id = 2L,
                displayName = "Bob",
                inviteCode = "INV-BOB001",
                creditBalance = 120,
            ),
        )
        whenever(inviteCodeRedemptionMapper.selectCount(any<QueryWrapper<InviteCodeRedemption>>())).thenReturn(0)
        whenever(creditService.getBalance(1L)).thenReturn(70)

        val result = service.redeem(1L, "INV-BOB001")

        val redemptionCaptor = argumentCaptor<InviteCodeRedemption>()
        verify(inviteCodeRedemptionMapper).insert(redemptionCaptor.capture())
        verify(creditService).credit(
            eq(2L),
            eq(20),
            eq("INVITE_CODE_INVITER"),
            eq("INVITE_CODE"),
            eq(1L),
            eq("invite code redeemed by user 1"),
        )
        verify(creditService).credit(
            eq(1L),
            eq(20),
            eq("INVITE_CODE_INVITEE"),
            eq("INVITE_CODE"),
            eq(2L),
            eq("redeemed invite code INV-BOB001"),
        )
        assertEquals("INVITE_CODE", result.type)
        assertEquals("INV-BOB001", result.code)
        assertEquals(20, result.creditedAmount)
        assertEquals(70, result.balanceAfter)
        assertEquals(20, result.inviterReward)
        assertEquals(20, result.inviteeReward)
        assertEquals(2L, redemptionCaptor.firstValue.inviterUserId)
        assertEquals(1L, redemptionCaptor.firstValue.inviteeUserId)
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
