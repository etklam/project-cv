package me.hker.module.reward.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.config.AppBusinessProperties
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.dto.InviteRedemptionDto
import me.hker.module.reward.dto.InviteStatsDto
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.dto.RewardSummaryDto
import me.hker.module.reward.entity.InviteCodeRedemption
import me.hker.module.reward.entity.PromoCode
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.InviteCodeRedemptionMapper
import me.hker.module.reward.mapper.PromoCodeMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.RewardService
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RewardServiceImpl(
    private val userService: UserService,
    private val creditService: CreditService,
    private val promoCodeMapper: PromoCodeMapper,
    private val promoCodeRedemptionMapper: PromoCodeRedemptionMapper,
    private val inviteCodeRedemptionMapper: InviteCodeRedemptionMapper,
    private val businessProperties: AppBusinessProperties,
) : RewardService {
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

    @Transactional
    override fun redeem(userId: Long, code: String): RewardRedeemResultDto {
        val normalizedCode = normalizeCode(code)
        require(normalizedCode.isNotBlank()) { "reward code is required" }
        return if (normalizedCode.startsWith(INVITE_PREFIX)) {
            redeemInviteCode(userId, normalizedCode)
        } else {
            redeemPromoCode(userId, normalizedCode)
        }
    }

    @Transactional
    fun redeemPromoCode(userId: Long, normalizedCode: String): RewardRedeemResultDto {
        requireUser(userId)

        val promoCode = promoCodeMapper.selectOne(
            QueryWrapper<PromoCode>()
                .eq("code", normalizedCode)
                .last("LIMIT 1"),
        ) ?: throw IllegalArgumentException("reward code is invalid")

        validatePromoCode(promoCode)

        val existingUserRedemptionCount = promoCodeRedemptionMapper.selectCount(
            QueryWrapper<PromoCodeRedemption>()
                .eq("promo_code_id", promoCode.id)
                .eq("user_id", userId),
        )
        require(existingUserRedemptionCount == 0L) { "promo code already redeemed" }

        val totalRedemptionCount = promoCodeRedemptionMapper.selectCount(
            QueryWrapper<PromoCodeRedemption>()
                .eq("promo_code_id", promoCode.id),
        )
        if (promoCode.maxRedemptions != null) {
            require(totalRedemptionCount < promoCode.maxRedemptions!!) { "promo code redemption limit reached" }
        }

        val rewardValue = promoCode.rewardValue
        require(rewardValue > 0) { "reward code is invalid" }

        promoCodeRedemptionMapper.insert(
            PromoCodeRedemption().apply {
                promoCodeId = promoCode.id
                this.userId = userId
                this.rewardValue = rewardValue
            },
        )
        creditService.credit(
            userId = userId,
            amount = rewardValue,
            type = "PROMO_CODE",
            referenceType = "PROMO_CODE",
            referenceId = promoCode.id,
            description = "redeemed promo code ${promoCode.code}",
        )

        return RewardRedeemResultDto(
            type = "PROMO_CODE",
            code = promoCode.code,
            creditedAmount = rewardValue,
            balanceAfter = creditService.getBalance(userId),
            inviterReward = null,
            inviteeReward = null,
            message = "promo code redeemed",
        )
    }

    @Transactional
    fun redeemInviteCode(userId: Long, normalizedCode: String): RewardRedeemResultDto {
        val invitee = requireUser(userId)
        require(invitee.inviteCode != normalizedCode) { "cannot redeem your own invite code" }

        val inviter = userService.findByInviteCode(normalizedCode)
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

    private fun validatePromoCode(promoCode: PromoCode) {
        require(promoCode.isActive) { "promo code is inactive" }
        require(promoCode.rewardType == "CREDIT_FIXED") { "unsupported reward type" }

        val now = LocalDateTime.now()
        if (promoCode.startsAt != null) {
            require(!now.isBefore(promoCode.startsAt)) { "promo code is not active yet" }
        }
        if (promoCode.expiresAt != null) {
            require(!now.isAfter(promoCode.expiresAt)) { "promo code has expired" }
        }
    }

    private fun requireUser(userId: Long) =
        userService.findById(userId) ?: throw IllegalArgumentException("user not found")

    private fun normalizeCode(code: String) = code.trim().uppercase()

    companion object {
        private const val INVITE_PREFIX = "INV-"
    }
}
