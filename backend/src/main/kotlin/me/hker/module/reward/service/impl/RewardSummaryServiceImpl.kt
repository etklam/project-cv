package me.hker.module.reward.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.dto.InviteRedemptionDto
import me.hker.module.reward.dto.InviteStatsDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.RewardSummaryService
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service

@Service
class RewardSummaryServiceImpl(
    private val userService: UserService,
    private val creditService: CreditService,
    private val promoCodeRedemptionMapper: PromoCodeRedemptionMapper,
    private val inviteCodeRedemptionMapper: InviteCodeRedemptionMapper,
) : RewardSummaryService {
    override fun summary(userId: Long): RewardSummaryDto {
        val user = requireUser(userId)
        val inviteLogs = inviteCodeRedemptionMapper.selectList(
            QueryWrapper<InviteCodeRedemption>()
                .eq("inviter_user_id", userId),
        )
        val inviteRedemption = inviteCodeRedemptionMapper.selectOne(
            QueryWrapper<InviteCodeRedemption>()
                .eq("invitee_user_id", userId)
                .last("LIMIT 1"),
        )

        return RewardSummaryDto(
            balance = creditService.getBalance(userId),
            inviteCode = user.inviteCode,
            inviteStats = InviteStatsDto(
                inviterRedemptions = inviteLogs.size,
                totalInviterCredits = inviteLogs.sumOf { it.inviterReward },
            ),
            promoRedemptions = promoCodeRedemptionMapper.selectCount(
                QueryWrapper<PromoCodeRedemption>()
                    .eq("user_id", userId),
            ).toInt(),
            inviteRedemption = inviteRedemption?.let {
                InviteRedemptionDto(
                    inviteCode = it.inviteCode,
                    inviterUserId = it.inviterUserId ?: 0L,
                    inviterDisplayName = it.inviterUserId?.let(userService::findById)?.displayName,
                    inviteeReward = it.inviteeReward,
                )
            },
        )
    }

    private fun requireUser(userId: Long) =
        userService.findById(userId) ?: throw IllegalArgumentException("user not found")
}
