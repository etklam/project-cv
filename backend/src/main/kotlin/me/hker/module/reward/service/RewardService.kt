package me.hker.module.reward.service

import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto

interface RewardService {
    fun summary(userId: Long): RewardSummaryDto
    fun redeem(userId: Long, code: String): RewardRedeemResultDto
}
