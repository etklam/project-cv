package me.hker.module.reward.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("invite_code_redemptions")
class InviteCodeRedemption {
    @TableId(type = IdType.AUTO)
    var id: Long? = null
    var inviterUserId: Long? = null
    var inviteeUserId: Long? = null
    var inviteCode: String = ""
    var inviterReward: Int = 0
    var inviteeReward: Int = 0
    var createdAt: LocalDateTime? = null
}
