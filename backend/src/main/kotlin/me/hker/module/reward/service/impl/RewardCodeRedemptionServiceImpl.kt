package me.hker.module.reward.service.impl

import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.service.InviteCodeRewardRedemptionService
import me.hker.module.reward.service.PromoCodeRewardRedemptionService
import me.hker.module.reward.service.RewardCodeRedemptionService
import org.springframework.stereotype.Service

@Service
class RewardCodeRedemptionServiceImpl(
    private val promoCodeRewardRedemptionService: PromoCodeRewardRedemptionService,
    private val inviteCodeRewardRedemptionService: InviteCodeRewardRedemptionService,
) : RewardCodeRedemptionService {
    override fun redeem(userId: Long, code: String): RewardRedeemResultDto {
        val normalizedCode = code.trim().uppercase()
        require(normalizedCode.isNotBlank()) { "reward code is required" }

        return if (normalizedCode.startsWith(INVITE_PREFIX)) {
            inviteCodeRewardRedemptionService.redeem(userId, normalizedCode)
        } else {
            promoCodeRewardRedemptionService.redeem(userId, normalizedCode)
        }
    }

    companion object {
        private const val INVITE_PREFIX = "INV-"
    }
}
