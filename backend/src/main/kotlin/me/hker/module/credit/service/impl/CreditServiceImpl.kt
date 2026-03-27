package me.hker.module.credit.service.impl

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.module.credit.entity.CreditTransaction
import me.hker.module.credit.mapper.CreditTransactionMapper
import me.hker.module.credit.service.CreditService
import me.hker.module.user.mapper.UserMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreditServiceImpl(
    private val creditTransactionMapper: CreditTransactionMapper,
    private val userMapper: UserMapper,
) : CreditService {
    override fun hasEnoughCredits(userId: Long, amount: Int): Boolean = getBalance(userId) >= amount

    @Transactional
    override fun deduct(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    ) {
        require(amount > 0) { "amount must be positive" }
        val nextBalance = applyBalanceDelta(userId, -amount)
        val transaction = CreditTransaction().apply {
            this.userId = userId
            this.amount = -amount
            this.balanceAfter = nextBalance
            this.type = type
            this.referenceType = referenceType
            this.referenceId = referenceId
            this.description = description
        }
        creditTransactionMapper.insert(transaction)
    }

    @Transactional
    override fun credit(
        userId: Long,
        amount: Int,
        type: String,
        referenceType: String?,
        referenceId: Long?,
        description: String?,
    ) {
        require(amount > 0) { "amount must be positive" }
        val nextBalance = applyBalanceDelta(userId, amount)
        val transaction = CreditTransaction().apply {
            this.userId = userId
            this.amount = amount
            this.balanceAfter = nextBalance
            this.type = type
            this.referenceType = referenceType
            this.referenceId = referenceId
            this.description = description
        }
        creditTransactionMapper.insert(transaction)
    }

    override fun getBalance(userId: Long): Int = userMapper.selectCreditBalance(userId) ?: 0

    override fun getTransactions(userId: Long, page: Int, size: Int): IPage<CreditTransaction> {
        return creditTransactionMapper.selectPage(
            Page<CreditTransaction>(page.toLong(), size.toLong()),
            QueryWrapper<CreditTransaction>()
                .eq("user_id", userId)
                .orderByDesc("id"),
        )
    }

    private fun applyBalanceDelta(userId: Long, delta: Int): Int {
        val nextBalance = userMapper.adjustCreditBalanceReturning(userId, delta)
        if (nextBalance == null) {
            val currentBalance = userMapper.selectCreditBalance(userId)
            if (currentBalance == null) {
                throw IllegalArgumentException("user not found")
            }
            if (delta < 0 && currentBalance + delta < 0) {
                throw IllegalArgumentException("insufficient credits")
            }
            throw IllegalStateException("credit balance update failed")
        }

        return nextBalance
    }
}
