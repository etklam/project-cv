package me.hker.module.admin.service

import me.hker.module.admin.dto.PaginatedTransactionResponse

interface AdminCreditService {
    fun listTransactions(page: Int, size: Int, userId: Long?): PaginatedTransactionResponse
}
