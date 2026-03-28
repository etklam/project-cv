package me.hker.module.reward.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.config.AppBusinessProperties
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.service.InviteCodeRewardRedemptionService
import me.hker.module.user.mapper.UserMapper
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional

@Service
class InviteCodeRewardRedemptionServiceImpl(
    private val userService: UserService,
    private val creditService: CreditService,
    private val inviteCodeRedemptionMapper: InviteCodeRedemptionMapper,
    private val userMapper: UserMapper,
    private val businessProperties: AppBusinessProperties,
) : InviteCodeRewardRedemptionService {
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    override fun redeem(userId: Long, normalizedCode: String): RewardRedeemResultDto {
        val invitee = requireUser(userId)
        require(invitee.inviteCode != normalizedCode) { "cannot redeem your own invite code" }

        // Use pessimistic lock on inviter to prevent race conditions
        val inviter = userMapper.selectForUpdate(invitee.inviteCode)
            ?: throw IllegalArgumentException("reward code is invalid")

        val existingInviteRedemptionCount = inviteCodeRedemptionMapper.selectCount(
            QueryWrapper<InviteCodeRedemption>()
                .eq("invitee_user_id", userId),
        )
        require(existingInviteRedemptionCount == 0L) { "invite code already redeemed" }

        val inviterBonus = businessProperties.reward.invite.inviterBonus
        val inviteeBonus = businessProperties.reward.invite.inviteeBonus

        inviteCodeRedemptionMapper.insert(
            InviteCodeRedemption().apply {
                inviterUserId = inviter.id
                inviteeUserId = invitee.id
                inviteCode = normalizedCode
                inviterReward = inviterBonus
                inviteeReward = inviteeBonus
            },
        )

        creditService.credit(
            userId = inviter.id ?: throw IllegalArgumentException("inviter not found"),
            amount = inviterBonus,
            type = "INVITE_CODE_INVITER",
            referenceType = "INVITE_CODE",
            referenceId = invitee.id,
            description = "invite code redeemed by user ${invitee.id}",
        )
        creditService.credit(
            userId = invitee.id ?: throw IllegalArgumentException("invitee not found"),
            amount = inviteeBonus,
            type = "INVITE_CODE_INVITEE",
            referenceType = "INVITE_CODE",
            referenceId = inviter.id,
            description = "redeemed invite code $normalizedCode",
        )

        return RewardRedeemResultDto(
            type = "INVITE_CODE",
            code = normalizedCode,
            creditedAmount = inviteeBonus,
            balanceAfter = creditService.getBalance(userId),
            inviterReward = inviterBonus,
            inviteeReward = inviteeBonus,
            message = "invite code redeemed",
        )
    }

    private fun requireUser(userId: Long) =
        userService.findById(userId) ?: throw IllegalArgumentException("user not found")
}
