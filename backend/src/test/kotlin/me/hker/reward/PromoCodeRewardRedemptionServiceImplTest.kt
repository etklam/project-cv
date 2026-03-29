package me.hker.reward

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.entity.PromoCode
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.PromoCodeMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.impl.PromoCodeRewardRedemptionServiceImpl
import me.hker.module.user.entity.User
import me.hker.module.user.service.UserService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class PromoCodeRewardRedemptionServiceImplTest {
    private val userService = mock<UserService>()
    private val creditService = mock<CreditService>()
    private val promoCodeMapper = mock<PromoCodeMapper>()
    private val promoCodeRedemptionMapper = mock<PromoCodeRedemptionMapper>()

    private val service = PromoCodeRewardRedemptionServiceImpl(
        userService = userService,
        creditService = creditService,
        promoCodeMapper = promoCodeMapper,
        promoCodeRedemptionMapper = promoCodeRedemptionMapper,
    )

    @Test
    fun `redeem should credit promo code once`() {
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
        whenever(promoCodeMapper.selectForUpdate(any<QueryWrapper<PromoCode>>())).thenReturn(promoCode)
        whenever(promoCodeRedemptionMapper.selectCount(any<QueryWrapper<PromoCodeRedemption>>())).thenReturn(0, 3)
        whenever(creditService.getBalance(1L)).thenReturn(80)

        val result = service.redeem(1L, "WELCOME2026")

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
