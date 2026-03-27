package me.hker.module.credit.service.impl

import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.module.credit.entity.CreditTransaction
import me.hker.module.credit.mapper.CreditTransactionMapper
import me.hker.module.credit.service.CreditService
import me.hker.module.user.entity.User
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
        val user = userMapper.selectById(userId) ?: throw IllegalArgumentException("user not found")
        val currentBalance = user.creditBalance
        require(currentBalance >= amount) { "insufficient credits" }
        val nextBalance = currentBalance - amount
        val transaction = CreditTransaction().apply {
            this.userId = userId
            this.amount = -amount
            this.balanceAfter = nextBalance
            this.type = type
            this.referenceType = referenceType
            this.referenceId = referenceId
            this.description = description
        }
        userMapper.update(
            null,
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User>()
                .eq("id", userId)
                .set("credit_balance", nextBalance),
        )
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
        val user = userMapper.selectById(userId) ?: throw IllegalArgumentException("user not found")
        val currentBalance = user.creditBalance
        val nextBalance = currentBalance + amount
        val transaction = CreditTransaction().apply {
            this.userId = userId
            this.amount = amount
            this.balanceAfter = nextBalance
            this.type = type
            this.referenceType = referenceType
            this.referenceId = referenceId
            this.description = description
        }
        userMapper.update(
            null,
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User>()
                .eq("id", userId)
                .set("credit_balance", nextBalance),
        )
        creditTransactionMapper.insert(transaction)
    }

    override fun getBalance(userId: Long): Int = userMapper.selectById(userId)?.creditBalance ?: 0

    override fun getTransactions(userId: Long, page: Int, size: Int): IPage<CreditTransaction> {
        return creditTransactionMapper.selectPage(
            Page<CreditTransaction>(page.toLong(), size.toLong()),
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CreditTransaction>()
                .eq("user_id", userId)
                .orderByDesc("id"),
        )
    }
}
