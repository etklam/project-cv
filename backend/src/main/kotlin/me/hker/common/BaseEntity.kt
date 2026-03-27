package me.hker.common

import com.baomidou.mybatisplus.annotation.FieldFill
import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableField
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableLogic
import java.time.LocalDateTime

open class BaseEntity {
    @TableId(type = IdType.AUTO)
    var id: Long? = null

    @TableField(fill = FieldFill.INSERT)
    var createdAt: LocalDateTime? = null

    @TableField(fill = FieldFill.INSERT_UPDATE)
    var updatedAt: LocalDateTime? = null

    @TableLogic(value = "false", delval = "true")
    var isDeleted: Boolean = false
}
