package me.hker.module.reward.service

import me.hker.module.reward.dto.RewardSummaryDto

interface RewardSummaryService {
    fun summary(userId: Long): RewardSummaryDto
}
