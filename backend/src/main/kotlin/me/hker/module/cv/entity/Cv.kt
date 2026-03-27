package me.hker.module.cv.entity

import com.baomidou.mybatisplus.annotation.TableName
import me.hker.common.BaseEntity

@TableName("cvs")
class Cv : BaseEntity() {
    var userId: Long? = null
    var title: String = ""
    var templateKey: String = ""
    var isPublic: Boolean = false
    var slug: String? = null
}
