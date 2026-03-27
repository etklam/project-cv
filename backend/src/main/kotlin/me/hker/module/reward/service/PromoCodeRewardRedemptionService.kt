package me.hker.module.reward.service

import me.hker.module.reward.dto.RewardRedeemResultDto

interface PromoCodeRewardRedemptionService {
    fun redeem(userId: Long, normalizedCode: String): RewardRedeemResultDto
}
