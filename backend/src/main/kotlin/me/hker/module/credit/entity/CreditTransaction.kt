package me.hker.module.credit.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.time.LocalDateTime

@TableName("credit_transactions")
class CreditTransaction {
    @TableId(type = IdType.AUTO)
    var id: Long? = null
    var userId: Long? = null
    var amount: Int = 0
    var balanceAfter: Int = 0
    var type: String = ""
    var referenceType: String? = null
    var referenceId: Long? = null
    var description: String? = null
    var createdAt: LocalDateTime? = null
}
