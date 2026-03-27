package me.hker.reward

import me.hker.common.DefaultCurrentUserResolver
import me.hker.module.reward.RewardController
import me.hker.module.reward.dto.InviteRedemptionDto
import me.hker.module.reward.dto.InviteStatsDto
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.service.RewardService
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@WebMvcTest(RewardController::class)
@AutoConfigureMockMvc(addFilters = false)
@Import(DefaultCurrentUserResolver::class)
class RewardControllerContractTest(
    @Autowired private val mockMvc: MockMvc,
) {
    @MockBean
    private lateinit var rewardService: RewardService

    @Test
    fun `summary endpoint should follow unified response contract`() {
        given(rewardService.summary(1L)).willReturn(
            RewardSummaryDto(
                balance = 70,
                inviteCode = "INV-ALICE1",
                inviteStats = InviteStatsDto(
                    inviterRedemptions = 2,
                    totalInviterCredits = 40,
                ),
                promoRedemptions = 1,
                inviteRedemption = InviteRedemptionDto(
                    inviteCode = "INV-BOB001",
                    inviterUserId = 2L,
                    inviterDisplayName = "Bob",
                    inviteeReward = 20,
                ),
            ),
        )

        mockMvc.get("/api/v1/me/rewards/summary") {
            header("X-User-Id", 1L)
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value(0) }
            jsonPath("$.message") { value("OK") }
            jsonPath("$.data.balance") { value(70) }
            jsonPath("$.data.inviteCode") { value("INV-ALICE1") }
            jsonPath("$.data.inviteStats.inviterRedemptions") { value(2) }
            jsonPath("$.data.promoRedemptions") { value(1) }
            jsonPath("$.data.inviteRedemption.inviterDisplayName") { value("Bob") }
        }
    }

    @Test
    fun `redeem endpoint should return redemption result in unified response contract`() {
        given(rewardService.redeem(1L, "welcome2026")).willReturn(
            RewardRedeemResultDto(
                type = "PROMO_CODE",
                code = "WELCOME2026",
                creditedAmount = 30,
                balanceAfter = 80,
                inviterReward = null,
                inviteeReward = null,
                message = "promo code redeemed",
            ),
        )

        mockMvc.post("/api/v1/me/rewards/redeem") {
            header("X-User-Id", 1L)
            contentType = MediaType.APPLICATION_JSON
            content = """{"code":"welcome2026"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.code") { value(0) }
            jsonPath("$.message") { value("OK") }
            jsonPath("$.data.type") { value("PROMO_CODE") }
            jsonPath("$.data.code") { value("WELCOME2026") }
            jsonPath("$.data.creditedAmount") { value(30) }
            jsonPath("$.data.balanceAfter") { value(80) }
        }
    }
}
