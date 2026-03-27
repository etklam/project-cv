package me.hker.module.credit.dto

import me.hker.module.credit.entity.CreditTransaction

data class CreditBalanceResponse(
    val balance: Int,
)

data class CreditTransactionPageResponse(
    val transactions: List<CreditTransaction>,
    val page: Int,
    val size: Int,
    val total: Long,
)
