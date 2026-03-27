package me.hker.module.credit.service

import com.baomidou.mybatisplus.core.metadata.IPage
import me.hker.module.credit.entity.CreditTransaction
import org.springframework.transaction.annotation.Transactional

interface CreditService {
    fun hasEnoughCredits(userId: Long, amount: Int): Boolean

    @Transactional
    fun deduct(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String? = null,
        referenceId: Long? = null,
        description: String? = null,
    )

    @Transactional
    fun credit(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String? = null,
        referenceId: Long? = null,
        description: String? = null,
    )

    fun getBalance(userId: Long): Int
    fun getTransactions(userId: Long, page: Int, size: Int): IPage<CreditTransaction>
}
