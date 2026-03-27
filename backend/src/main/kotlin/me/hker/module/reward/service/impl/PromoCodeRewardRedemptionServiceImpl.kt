package me.hker.module.reward.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import me.hker.module.credit.service.CreditService
import me.hker.module.reward.dto.RewardRedeemResultDto
import me.hker.module.reward.entity.PromoCode
import me.hker.module.reward.entity.PromoCodeRedemption
import me.hker.module.reward.mapper.PromoCodeMapper
import me.hker.module.reward.mapper.PromoCodeRedemptionMapper
import me.hker.module.reward.service.PromoCodeRewardRedemptionService
import me.hker.module.user.service.UserService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PromoCodeRewardRedemptionServiceImpl(
    private val userService: UserService,
    private val creditService: CreditService,
    private val promoCodeMapper: PromoCodeMapper,
    private val promoCodeRedemptionMapper: PromoCodeRedemptionMapper,
) : PromoCodeRewardRedemptionService {
    @Transactional
    override fun redeem(userId: Long, normalizedCode: String): RewardRedeemResultDto {
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
}
