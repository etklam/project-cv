package me.hker.module.template.entity

import com.baomidou.mybatisplus.annotation.TableName
import me.hker.common.BaseEntity

@TableName("templates")
class Template : BaseEntity() {
    var componentKey: String = ""
    var displayNameI18n: String = "{}"
    var descriptionI18n: String? = null
    var previewImage: String? = null
    var creditCost: Int = 0
    var isActive: Boolean = true
    var sortOrder: Int = 0
}
