package me.hker.module.reward.entity

import com.baomidou.mybatisplus.annotation.TableName
import me.hker.common.BaseEntity
import java.time.LocalDateTime

@TableName("promo_codes")
class PromoCode : BaseEntity() {
    var code: String = ""
    var campaignKey: String? = null
    var rewardType: String = "CREDIT_FIXED"
    var rewardValue: Int = 0
    var maxRedemptions: Int? = null
    var startsAt: LocalDateTime? = null
    var expiresAt: LocalDateTime? = null
    var isActive: Boolean = true
}
