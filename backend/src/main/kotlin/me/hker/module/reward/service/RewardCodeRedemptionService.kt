package me.hker.module.reward.service

import me.hker.module.reward.dto.RewardRedeemResultDto

interface RewardCodeRedemptionService {
    fun redeem(userId: Long, code: String): RewardRedeemResultDto
}
