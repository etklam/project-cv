package me.hker.module.reward.service.impl

import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.service.RewardCodeRedemptionService
import me.hker.module.reward.service.RewardService
import me.hker.module.reward.service.RewardSummaryService
import org.springframework.stereotype.Service

@Service
class RewardServiceImpl(
    private val rewardSummaryService: RewardSummaryService,
    private val rewardCodeRedemptionService: RewardCodeRedemptionService,
) : RewardService {
    override fun summary(userId: Long): RewardSummaryDto = rewardSummaryService.summary(userId)

    override fun redeem(userId: Long, code: String): RewardRedeemResultDto =
        rewardCodeRedemptionService.redeem(userId, code)
}
