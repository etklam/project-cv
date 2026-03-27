package me.hker.module.credit

import me.hker.common.CurrentUserResolver
import me.hker.common.R
import me.hker.module.credit.dto.CreditBalanceResponse
import me.hker.module.credit.dto.CreditTransactionPageResponse
import me.hker.module.credit.service.CreditService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me/credits")
class CreditController(
    private val creditService: CreditService,
    private val currentUserResolver: CurrentUserResolver,
) {
    @GetMapping("/balance")
    fun balance(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
    ): R<CreditBalanceResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        return R.ok(CreditBalanceResponse(balance = creditService.getBalance(resolvedUserId)))
    }

    @GetMapping("/transactions")
    fun transactions(
        @RequestHeader(name = "X-User-Id", required = false) userId: Long?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): R<CreditTransactionPageResponse> {
        val resolvedUserId = currentUserResolver.resolve(userId)
        val result = creditService.getTransactions(resolvedUserId, page, size)
        return R.ok(
            CreditTransactionPageResponse(
                transactions = result.records,
                page = page,
                size = size,
                total = result.total,
            ),
        )
    }
}
