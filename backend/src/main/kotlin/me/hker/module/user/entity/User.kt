package me.hker.module.user.entity

import com.baomidou.mybatisplus.annotation.TableName
import me.hker.common.BaseEntity

@TableName("users")
class User : BaseEntity() {
    var email: String = ""
    var passwordHash: String = ""
    var username: String? = null
    var displayName: String = ""
    var avatarPath: String? = null
    var onboardingDraft: String? = null
    var onboardingStatus: String = "STEP_1"
    var locale: String = "zh-TW"
    var creditBalance: Int = 0
    var inviteCode: String = ""
}
