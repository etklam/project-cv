package me.hker.module.reward.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("promo_code_redemptions")
class PromoCodeRedemption {
    @TableId(type = IdType.AUTO)
    var id: Long? = null
    var promoCodeId: Long? = null
    var userId: Long? = null
    var rewardValue: Int = 0
    var createdAt: LocalDateTime? = null
}
