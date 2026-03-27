package me.hker.reward

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.config.AppBusinessProperties
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.entity.PromoCode
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.mapper.PromoCodeMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.impl.RewardServiceImpl
import me.hker.module.user.entity.User
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
import java.time.LocalDateTime

class RewardServiceImplTest {
    private val userService = mock<UserService>()
    private val creditService = mock<CreditService>()
    private val promoCodeMapper = mock<PromoCodeMapper>()
    private val promoCodeRedemptionMapper = mock<PromoCodeRedemptionMapper>()
    private val inviteCodeRedemptionMapper = mock<InviteCodeRedemptionMapper>()
    private val businessProperties = AppBusinessProperties(
        reward = AppBusinessProperties.Reward(
            invite = AppBusinessProperties.Reward.Invite(
                inviterBonus = 20,
                inviteeBonus = 20,
            ),
        ),
    )

    private val service = RewardServiceImpl(
        userService = userService,
        creditService = creditService,
        promoCodeMapper = promoCodeMapper,
        promoCodeRedemptionMapper = promoCodeRedemptionMapper,
        inviteCodeRedemptionMapper = inviteCodeRedemptionMapper,
        businessProperties = businessProperties,
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

    @Test
    fun `redeem should normalize and credit promo code once`() {
        val promoCode = PromoCode().apply {
            id = 9L
            code = "WELCOME2026"
            rewardValue = 30
            rewardType = "CREDIT_FIXED"
            isActive = true
            maxRedemptions = 10
            startsAt = LocalDateTime.now().minusDays(1)
            expiresAt = LocalDateTime.now().plusDays(1)
        }

        whenever(userService.findById(1L)).thenReturn(
            user(
                id = 1L,
                displayName = "Alice",
                inviteCode = "INV-ALICE1",
                creditBalance = 50,
            ),
        )
        whenever(promoCodeMapper.selectOne(any<QueryWrapper<PromoCode>>())).thenReturn(promoCode)
        whenever(promoCodeRedemptionMapper.selectCount(any<QueryWrapper<PromoCodeRedemption>>())).thenReturn(0, 3)
        whenever(creditService.getBalance(1L)).thenReturn(80)

        val result = service.redeem(1L, " welcome2026 ")

        val redemptionCaptor = argumentCaptor<PromoCodeRedemption>()
        verify(promoCodeRedemptionMapper).insert(redemptionCaptor.capture())
        verify(creditService).credit(
            eq(1L),
            eq(30),
            eq("PROMO_CODE"),
            eq("PROMO_CODE"),
            eq(9L),
            eq("redeemed promo code WELCOME2026"),
        )
        assertEquals("PROMO_CODE", result.type)
        assertEquals("WELCOME2026", result.code)
        assertEquals(30, result.creditedAmount)
        assertEquals(80, result.balanceAfter)
        assertEquals(9L, redemptionCaptor.firstValue.promoCodeId)
        assertEquals(1L, redemptionCaptor.firstValue.userId)
        assertEquals(30, redemptionCaptor.firstValue.rewardValue)
    }

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
            service.redeem(1L, "inv-alice1")
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
        whenever(userService.findByInviteCode("INV-BOB001")).thenReturn(
            user(
                id = 2L,
                displayName = "Bob",
                inviteCode = "INV-BOB001",
                creditBalance = 120,
            ),
        )
        whenever(inviteCodeRedemptionMapper.selectCount(any<QueryWrapper<InviteCodeRedemption>>())).thenReturn(0)
        whenever(creditService.getBalance(1L)).thenReturn(70)

        val result = service.redeem(1L, "inv-bob001")

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
