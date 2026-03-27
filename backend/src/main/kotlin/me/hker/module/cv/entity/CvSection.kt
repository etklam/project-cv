package me.hker.module.cv.entity

import com.baomidou.mybatisplus.annotation.TableName
import me.hker.common.BaseEntity

@TableName("cv_sections")
class CvSection : BaseEntity() {
    var cvId: Long? = null
    var sectionType: String = ""
    var sortOrder: Int = 0
    var title: String? = null
    var content: String = "{}"
}
