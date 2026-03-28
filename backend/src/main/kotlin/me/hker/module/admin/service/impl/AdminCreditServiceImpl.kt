package me.hker.module.admin.service.impl

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper
import com.baomidou.mybatisplus.core.metadata.IPage
import com.baomidou.mybatisplus.extension.plugins.pagination.Page
import me.hker.module.admin.dto.AdminCreditTransactionDto
import me.hker.module.admin.dto.PaginatedTransactionResponse
import me.hker.module.admin.service.AdminCreditService
import me.hker.module.credit.entity.CreditTransaction
import me.hker.module.credit.mapper.CreditTransactionMapper
import me.hker.module.user.mapper.UserMapper
import org.springframework.stereotype.Service

@Service
class AdminCreditServiceImpl(
    private val creditTransactionMapper: CreditTransactionMapper,
    private val userMapper: UserMapper,
) : AdminCreditService {

    override fun listTransactions(page: Int, size: Int, userId: Long?): PaginatedTransactionResponse {
        val query = QueryWrapper<CreditTransaction>()

        userId?.let { query.eq("user_id", it) }

        query.orderByDesc("created_at")

        val transactionPage: IPage<CreditTransaction> = creditTransactionMapper.selectPage(
            Page(page.toLong(), size.toLong()),
            query
        )

        // Fetch user info for each transaction
        val transactions = transactionPage.records.map { transaction ->
            val user = userMapper.selectById(transaction.userId)
            AdminCreditTransactionDto(
                id = transaction.id ?: 0L,
                userId = transaction.userId ?: 0L,
                userEmail = user?.email,
                userName = user?.displayName,
                amount = transaction.amount,
                balanceAfter = transaction.balanceAfter,
                type = transaction.type,
                referenceType = transaction.referenceType,
                referenceId = transaction.referenceId,
                description = transaction.description,
                createdAt = transaction.createdAt ?: java.time.LocalDateTime.now(),
            )
        }

        return PaginatedTransactionResponse(
            transactions = transactions,
            total = transactionPage.total,
            page = page,
            size = size,
        )
    }
}
